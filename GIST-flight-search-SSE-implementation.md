# Gist: Flight Search App + SSE Implementation (Go)

Portable reference for implementing the same pattern in another Cursor project.

---

## Part 1: Flight Search App (Frontend) — Gist

### Overview
- **Stack**: React + TypeScript
- **Purpose**: Real-time flight search with three streaming methods: WebSocket, Long Polling, **SSE**
- **Flow**: User submits search → backend returns `query_id` → client opens stream (e.g. SSE) with `query_id` → server pushes progress + flight results until `completed` / `cancelled` / `timeout`

### Key Files & Roles
| File | Role |
|------|------|
| `src/services/api.ts` | HTTP: search, cancel, **getSSEUrl(queryId)** |
| `src/services/streamingService.ts` | **SSE via native EventSource**; also WebSocket + long poll |
| `src/hooks/useFlightSearch.ts` | State + start search → start streaming by method |
| `src/types/index.ts` | `StreamingMessage`, `FlightResult`, `StreamingMethod` ('sse' \| 'websocket' \| 'longpoll') |

### API Contract (used by app)
- **Start search**: `GET /api/search?from=&to=&trip_type=&departure_date=&return_date=&pax=` → `{ query_id, ws_url }`
- **Cancel**: `POST /api/search/cancel?query_id=`
- **SSE stream**: `GET /api/result/sse?query_id=<query_id>` → `text/event-stream`

### SSE Event Types (client must handle)
- `progress` — progress/status update (JSON body)
- `flight` — one flight result (JSON body)
- `completed` — search done (JSON body) → close EventSource
- `cancelled` — search cancelled (JSON body) → close EventSource
- `timeout` — search timeout (JSON body) → close EventSource

### Frontend SSE Snippet (React/TS) — Copy-Paste Ready

**1. API: SSE URL**
```ts
const API_BASE_URL = 'http://localhost:3001'; // or env

getSSEUrl(queryId: string): string {
  return `${API_BASE_URL}/api/result/sse?query_id=${queryId}`;
}
```

**2. Streaming message type (minimal)**
```ts
export interface StreamingMessage {
  type?: string;
  progress?: number;
  status?: string;
  message?: string;
  source?: string;
  airline?: string;
  flight_number?: string;
  departure_time?: string;
  price?: number;
  from?: string;
  to?: string;
  departure_date?: string;
  return_date?: string;
  received_flights?: number;
  total_expected?: number;
  // ... extend with your payload fields
}
```

**3. Connect SSE and forward to one callback**
```ts
// eventSource: EventSource | null
connectSSE(queryId: string, onMessage: (data: StreamingMessage) => void, onError: (err: string) => void, onClose: () => void) {
  const sseUrl = getSSEUrl(queryId);
  const eventSource = new EventSource(sseUrl);

  eventSource.onopen = () => console.log('SSE connected');

  const handleEvent = (event: MessageEvent) => {
    try {
      const data: StreamingMessage = JSON.parse(event.data);
      onMessage(data);
    } catch (e) {
      onError('Failed to parse message');
    }
  };

  eventSource.onmessage = handleEvent;
  eventSource.addEventListener('progress', handleEvent);
  eventSource.addEventListener('flight', handleEvent);
  eventSource.addEventListener('completed', (event) => {
    try {
      onMessage(JSON.parse(event.data));
      eventSource.close();
      onClose();
    } catch (_) {}
  });
  eventSource.addEventListener('cancelled', (event) => {
    try {
      onMessage(JSON.parse(event.data));
      eventSource.close();
      onClose();
    } catch (_) {}
  });
  eventSource.addEventListener('timeout', (event) => {
    try {
      onMessage(JSON.parse(event.data));
      eventSource.close();
      onClose();
    } catch (_) {}
  });
  eventSource.onerror = () => {
    onError('SSE connection failed');
  };

  return () => {
    eventSource.close();
  };
}
```

**4. Usage in hook**
- Call `searchFlights(...)` → get `query_id` from response.
- Then call `connectSSE(query_id, onMessage, onError, onClose)` and update state from `onMessage` (e.g. append flights, set progress, set `isSearching = false` on `completed`/`cancelled`/`timeout`).

---

## Part 2: Service-B (Go/Gin) — SSE Implementation

### Overview
- **Stack**: Go, Gin, Redis (pub/sub), `github.com/gin-contrib/sse` (via Gin’s `c.SSEvent`)
- **Flow**: Client GET `/api/result/sse?query_id=...` → handler subscribes to Redis channel `flight:<query_id>` → loop: read from channel or context done or timeout → send SSE events with `c.SSEvent(eventType, jsonString)`.

### Dependency
Gin already pulls in SSE support (no extra import needed for `c.SSEvent`):
```go
// go.mod
require (
	github.com/gin-gonic/gin v1.10.1
	// gin-contrib/sse is indirect dependency of gin
)
```

### Route Registration
```go
r := gin.Default()
// ...
r.GET("/api/result/sse", SSEHandler)
```

### SSE Handler — Full Implementation (copy-paste oriented)

```go
// SSEHandler handles Server-Sent Events for flight search results.
// Expects query_id as query parameter. Subscribes to Redis channel "flight:<query_id>"
// and streams progress, flight, completed, cancelled, timeout events.
func SSEHandler(c *gin.Context) {
	queryID := c.Query("query_id")
	if queryID == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "query_id is required"})
		return
	}

	// Required SSE headers
	c.Header("Content-Type", "text/event-stream")
	c.Header("Cache-Control", "no-cache")
	c.Header("Connection", "keep-alive")
	c.Header("Access-Control-Allow-Origin", "*")
	c.Header("Access-Control-Allow-Headers", "Cache-Control")

	// Subscribe to Redis channel (replace rdb with your *redis.Client)
	sub := rdb.Subscribe(context.Background(), "flight:"+queryID)
	defer sub.Close()
	ch := sub.Channel()

	receivedFlights := 0
	expectedFlights := 24

	// Optional: send initial progress
	initialProgress := map[string]interface{}{
		"type":     "progress",
		"progress": 0,
		"status":   "searching",
		"message":  "Starting flight search...",
	}
	initialData, _ := json.Marshal(initialProgress)
	c.SSEvent("progress", string(initialData))

	ctx, cancel := context.WithCancel(c.Request.Context())
	defer cancel()

	for {
		select {
		case msg := <-ch:
			var flightData map[string]interface{}
			if err := json.Unmarshal([]byte(msg.Payload), &flightData); err != nil {
				log.Println("Failed to parse flight data:", err)
				continue
			}

			if flightData["type"] == "cancelled" {
				cancelMsg := map[string]interface{}{
					"type": "cancelled", "progress": 0, "status": "cancelled", "message": "Search was cancelled",
				}
				cancelData, _ := json.Marshal(cancelMsg)
				c.SSEvent("cancelled", string(cancelData))
				return
			}

			receivedFlights++
			progress := int((float64(receivedFlights) / float64(expectedFlights)) * 100)
			if progress > 100 {
				progress = 100
			}

			flightData["progress"] = progress
			flightData["received_flights"] = receivedFlights
			flightData["total_expected"] = expectedFlights
			flightData["status"] = "searching"

			if receivedFlights >= expectedFlights {
				flightData["status"] = "completed"
				flightData["progress"] = 99
				flightData["message"] = "Search completed"
			}

			flightDataBytes, _ := json.Marshal(flightData)
			c.SSEvent("flight", string(flightDataBytes))

			if flightData["status"] == "completed" {
				completionMsg := map[string]interface{}{
					"type": "completed", "progress": 100, "status": "completed",
					"message": "All flights found", "total_flights": receivedFlights,
				}
				completionData, _ := json.Marshal(completionMsg)
				c.SSEvent("completed", string(completionData))
				return
			}

		case <-ctx.Done():
			log.Println("SSE client disconnected")
			return

		case <-time.After(30 * time.Second):
			timeoutMsg := map[string]interface{}{
				"type": "timeout", "progress": int((float64(receivedFlights) / float64(expectedFlights)) * 100),
				"status": "timeout", "message": "Search timeout - please try again",
			}
			timeoutData, _ := json.Marshal(timeoutMsg)
			c.SSEvent("timeout", string(timeoutData))
			return
		}
	}
}
```

### Event Types Sent by Server (for client reference)
| Event       | When |
|------------|------|
| `progress` | Initial + optional intermediate progress (JSON: type, progress, status, message, received_flights, total_expected) |
| `flight`   | Each flight + progress metadata (JSON object) |
| `completed`| When search is done (JSON: type, progress, status, message, total_flights) |
| `cancelled`| When search is cancelled (JSON: type, status, message) |
| `timeout`  | After 30s with no completion (JSON: type, status, message) |

### Backend Checklist for Your New Project
1. Gin route: `GET /api/result/sse` → `SSEHandler`.
2. Redis: subscribe to a channel per request (e.g. `flight:<query_id>`); publish from your search/aggregator when results or control messages (cancelled/timeout) are ready.
3. Use `c.SSEvent(eventType, jsonString)` only; Gin handles SSE framing.
4. Detect client disconnect via `c.Request.Context()` (e.g. `ctx.Done()`) and exit the loop so the handler returns.
5. Optional: timeout (e.g. 30s) with `time.After(...)` in the same `select` to send `timeout` and return.

---

## Quick Reference: Same Contract in Both Projects

- **SSE URL**: `GET /api/result/sse?query_id=<id>`  
- **Headers**: `Content-Type: text/event-stream`, `Cache-Control: no-cache`, `Connection: keep-alive`  
- **Events**: `progress`, `flight`, `completed`, `cancelled`, `timeout` — each event body is JSON.  
- **Client**: Use native `EventSource(url)`, listen to `onmessage` and `addEventListener('progress'|'flight'|'completed'|'cancelled'|'timeout')`, parse `event.data` as JSON, close on completed/cancelled/timeout.

Use this gist in your other Cursor project to replicate the flight-search app behavior and the Go SSE implementation.
