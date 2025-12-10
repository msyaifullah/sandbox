# Flight Search Streaming Endpoints

This service provides three different streaming approaches for real-time flight search results:

1. **WebSocket** - Full-duplex communication
2. **Long Polling** - HTTP-based polling with timeouts
3. **Server-Sent Events (SSE)** - One-way server-to-client streaming

All endpoints listen to the same Redis pubsub channel and provide the same flight data with progress updates.

## API Endpoints

### 1. Start Search
```
GET /api/search?from={origin}&to={destination}
```

**Response:**
```json
{
  "query_id": "abc123...",
  "ws_url": "ws://localhost:3001/ws/result/stream?query_id=abc123..."
}
```

### 2. WebSocket Streaming
```
GET /ws/result/stream?query_id={query_id}
```

**Features:**
- Full-duplex communication
- Real-time bidirectional messaging
- Automatic reconnection support
- Progress updates and flight results

**Message Types:**
- `progress` - Search progress updates
- `flight` - Individual flight results
- `completed` - Search completion
- `cancelled` - Search cancellation
- `timeout` - Search timeout

### 3. Long Polling
```
GET /api/result/longpoll?query_id={query_id}
```

**Features:**
- HTTP-based polling
- 30-second timeout
- 5-second heartbeat intervals
- JSON responses
- Automatic reconnection via polling

**Response Headers:**
```
Cache-Control: no-cache
Connection: keep-alive
Content-Type: application/json
```

### 4. Server-Sent Events (SSE)
```
GET /api/result/sse?query_id={query_id}
```

**Features:**
- One-way server-to-client streaming
- Automatic browser reconnection
- Event-based messaging
- 30-second timeout

**Response Headers:**
```
Content-Type: text/event-stream
Cache-Control: no-cache
Connection: keep-alive
Access-Control-Allow-Origin: *
```

## Data Format

All endpoints return the same data structure:

### Progress Update
```json
{
  "type": "progress",
  "progress": 25,
  "status": "searching",
  "message": "Starting flight search...",
  "received_flights": 6,
  "total_expected": 24
}
```

### Flight Result
```json
{
  "source": "kiwi",
  "airline": "Lion Air",
  "flight_number": "KI101",
  "departure_time": "08:30",
  "price": 1250000,
  "from": "JAK",
  "to": "CGK",
  "timestamp": "2024-01-15 10:30:45",
  "seat_class": "Economy",
  "affiliate_link": "https://www.kiwi.com/affiliate?...",
  "booking_url": "https://kiwi.com/flights/JAK-CGK/KI101",
  "progress": 25,
  "received_flights": 6,
  "total_expected": 24,
  "status": "searching"
}
```

### Completion Message
```json
{
  "type": "completed",
  "progress": 100,
  "status": "completed",
  "message": "All flights found",
  "total_flights": 24
}
```

## Usage Examples

### WebSocket (JavaScript)
```javascript
const ws = new WebSocket(`ws://localhost:3001/ws/result/stream?query_id=${queryId}`);

ws.onmessage = function(event) {
  const data = JSON.parse(event.data);
  
  switch(data.type) {
    case 'progress':
      updateProgress(data.progress, data.message);
      break;
    case 'flight':
      addFlightResult(data);
      break;
    case 'completed':
      handleSearchComplete(data);
      break;
    case 'cancelled':
      handleSearchCancelled(data);
      break;
  }
};
```

### Long Polling (JavaScript)
```javascript
async function longPoll(queryId) {
  try {
    const response = await fetch(`http://localhost:3001/api/result/longpoll?query_id=${queryId}`);
    const data = await response.json();
    
    switch(data.type) {
      case 'progress':
      case 'heartbeat':
        updateProgress(data.progress, data.message);
        break;
      case 'flight':
        addFlightResult(data);
        break;
      case 'completed':
        handleSearchComplete(data);
        return; // Stop polling
      case 'cancelled':
      case 'timeout':
        handleSearchEnd(data);
        return; // Stop polling
    }
    
    // Continue polling
    setTimeout(() => longPoll(queryId), 100);
  } catch (error) {
    console.error('Long polling error:', error);
    // Retry after delay
    setTimeout(() => longPoll(queryId), 1000);
  }
}
```

### Server-Sent Events (JavaScript)
```javascript
const eventSource = new EventSource(`http://localhost:3001/api/result/sse?query_id=${queryId}`);

eventSource.onmessage = function(event) {
  const data = JSON.parse(event.data);
  handleFlightData(data);
};

eventSource.addEventListener('progress', function(event) {
  const data = JSON.parse(event.data);
  updateProgress(data.progress, data.message);
});

eventSource.addEventListener('flight', function(event) {
  const data = JSON.parse(event.data);
  addFlightResult(data);
});

eventSource.addEventListener('completed', function(event) {
  const data = JSON.parse(event.data);
  handleSearchComplete(data);
  eventSource.close();
});

eventSource.addEventListener('cancelled', function(event) {
  const data = JSON.parse(event.data);
  handleSearchCancelled(data);
  eventSource.close();
});

eventSource.addEventListener('timeout', function(event) {
  const data = JSON.parse(event.data);
  handleSearchTimeout(data);
  eventSource.close();
});
```

## Testing

Run the test script to see all endpoints in action:

```bash
./test_streaming_endpoints.sh
```

## Comparison

| Feature | WebSocket | Long Polling | SSE |
|---------|-----------|--------------|-----|
| **Bidirectional** | ✅ Yes | ❌ No | ❌ No |
| **Reconnection** | Manual | Automatic | Automatic |
| **Browser Support** | Good | Excellent | Excellent |
| **Firewall Friendly** | ❌ No | ✅ Yes | ✅ Yes |
| **Proxy Support** | Limited | ✅ Yes | ✅ Yes |
| **Memory Usage** | Low | Medium | Low |
| **Server Load** | Low | Medium | Low |
| **Real-time** | ✅ Yes | ⚠️ Near real-time | ✅ Yes |

## Cancellation

Cancel an ongoing search:

```
POST /api/search/cancel?query_id={query_id}
```

**Response:**
```json
{
  "status": "cancelled"
}
```

## Error Handling

All endpoints handle:
- Invalid query_id
- Client disconnection
- Server timeouts (30 seconds)
- Redis connection issues
- JSON parsing errors

## Performance Considerations

1. **WebSocket**: Best for real-time bidirectional communication
2. **Long Polling**: Good for environments with restrictive firewalls
3. **SSE**: Best for one-way real-time updates with automatic reconnection

Choose based on your specific requirements and infrastructure constraints. 