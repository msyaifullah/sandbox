package main

import (
	"context"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"math"
	"math/rand"
	"net/http"
	"strings"
	"sync"
	"time"

	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"github.com/gorilla/websocket"
	"github.com/redis/go-redis/v9"
)

var (
	rdb            *redis.Client
	cancelRegistry sync.Map // map[queryID]context.CancelFunc
	upgrader       = websocket.Upgrader{CheckOrigin: func(r *http.Request) bool { return true }}
	// In-memory storage for flight results
	flightResults = make(map[string][]map[string]interface{})
	resultsMutex  sync.RWMutex
	// Track active pubsub listeners
	pubsubListeners sync.Map // map[queryID]context.CancelFunc
)

func main() {
	// Initialize Redis client
	rdb = redis.NewClient(&redis.Options{
		Addr:     "localhost:6379",
		Password: "changeMe123",
	})

	r := gin.Default()

	// Configure CORS middleware
	config := cors.DefaultConfig()
	config.AllowOrigins = []string{"http://localhost:3000", "http://localhost:3001", "http://127.0.0.1:3000", "http://127.0.0.1:3001"}
	config.AllowMethods = []string{"GET", "POST", "PUT", "DELETE", "OPTIONS"}
	config.AllowHeaders = []string{"Origin", "Content-Type", "Accept", "Authorization", "X-Requested-With"}
	config.AllowCredentials = true
	r.Use(cors.New(config))

	// Invoice validation endpoint
	r.POST("/api/invoice/validate", func(c *gin.Context) {
		// auth := c.GetHeader("Authorization")
		// if !strings.HasPrefix(auth, "Bearer ") {
		// 	c.JSON(http.StatusUnauthorized, gin.H{"error": "missing token"})
		// 	return
		// }

		// token := strings.TrimPrefix(auth, "Bearer ")
		// _, err := verifyToken(token)
		// if err != nil {
		// 	c.JSON(http.StatusUnauthorized, gin.H{"error": "invalid token"})
		// 	return
		// }

		body, err := io.ReadAll(c.Request.Body)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "failed to read request body"})
			return
		}

		// Try to parse as different order types
		response := validateInvoice(body)
		c.JSON(http.StatusOK, response)
	})

	// Flight search endpoints
	r.GET("/api/search", SearchHandler)
	r.POST("/api/search/cancel", CancelHandler)
	r.GET("/ws/result/stream", WebSocketHandler)

	// Long polling endpoint for results
	r.GET("/api/result/longpoll", LongPollHandler)

	// Server-Sent Events endpoint for results
	r.GET("/api/result/sse", SSEHandler)

	// Legacy endpoints
	r.GET("/api/protected", func(c *gin.Context) {
		auth := c.GetHeader("Authorization")
		if !strings.HasPrefix(auth, "Bearer ") {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "missing token"})
			return
		}

		token := strings.TrimPrefix(auth, "Bearer ")
		claims, err := verifyToken(token)
		if err != nil {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "invalid token"})
			return
		}

		c.JSON(http.StatusOK, gin.H{
			"message": "Hello from Service B",
			"by":      claims.Subject,
		})
	})

	r.GET("/call-a", func(c *gin.Context) {
		token, err := createJWT()
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to sign"})
			return
		}

		req, _ := http.NewRequest("POST", "http://localhost:3000/api/data", nil)
		req.Header.Set("Authorization", "Bearer "+token)

		client := &http.Client{}
		resp, err := client.Do(req)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
			return
		}
		defer resp.Body.Close()

		fmt.Println("Token:", token)
		body, _ := io.ReadAll(resp.Body)
		fmt.Println("Response Body:", string(body))
		c.Data(resp.StatusCode, "application/json", body)
	})

	r.Run(":3001")
}

// === Flight Search REST Endpoints ===
func SearchHandler(c *gin.Context) {
	from := c.Query("from")
	to := c.Query("to")
	tripType := c.Query("trip_type")
	departureDate := c.Query("departure_date")
	returnDate := c.Query("return_date")
	paxStr := c.Query("pax")

	// Parse PAX (default to 1 if not provided or invalid)
	pax := 1
	if paxStr != "" {
		if parsed, err := fmt.Sscanf(paxStr, "%d", &pax); err != nil || parsed != 1 || pax < 1 {
			pax = 1
		}
	}

	queryID := generateQueryID(from, to, tripType, departureDate, returnDate, pax)

	ctx, cancel := context.WithCancel(context.Background())
	cancelRegistry.Store(queryID, cancel)

	go func() {
		defer cancelRegistry.Delete(queryID)
		// Delay 10 seconds before running aggregator for testing
		// time.Sleep(10 * time.Second)
		runAggregator(ctx, queryID, from, to, tripType, departureDate, returnDate, pax)
	}()

	// Start background listener that writes pubsub messages to Redis
	go startPubsubListener(ctx, queryID)

	c.JSON(http.StatusOK, gin.H{
		"query_id": queryID,
		"ws_url":   fmt.Sprintf("ws://localhost:3001/ws/result/stream?query_id=%s", queryID),
	})
}

func CancelHandler(c *gin.Context) {
	queryID := c.Query("query_id")

	if val, ok := cancelRegistry.Load(queryID); ok {
		cancel := val.(context.CancelFunc)
		cancel()
		cancelRegistry.Delete(queryID)

		// Cancel pubsub listener
		if listenerCancel, ok := pubsubListeners.Load(queryID); ok {
			listenerCancel.(context.CancelFunc)()
			pubsubListeners.Delete(queryID)
		}

		// Clean up Redis data
		rdb.Del(context.Background(), "search_result:"+queryID)
		rdb.Del(context.Background(), "flight_results:"+queryID)
		rdb.Del(context.Background(), "flight_count:"+queryID)

		// Publish cancellation (for WebSocket/SSE)
		rdb.Publish(context.Background(), "flight:"+queryID, `{"type":"cancelled"}`)

		// Write cancellation to Redis for long polling
		rdb.RPush(context.Background(), "flight_results:"+queryID, `{"type":"cancelled"}`)
		rdb.Expire(context.Background(), "flight_results:"+queryID, 30*time.Minute)

		c.JSON(http.StatusOK, gin.H{"status": "cancelled"})
	} else {
		c.JSON(http.StatusNotFound, gin.H{"error": "query not found or already finished"})
	}
}

// === WebSocket Streaming ===
func WebSocketHandler(c *gin.Context) {
	queryID := c.Query("query_id")
	conn, err := upgrader.Upgrade(c.Writer, c.Request, nil)
	if err != nil {
		log.Println("WebSocket upgrade failed:", err)
		return
	}
	defer conn.Close()

	sub := rdb.Subscribe(context.Background(), "flight:"+queryID)
	ch := sub.Channel()

	// Track progress
	receivedFlights := 0
	expectedFlights := 24 // 8-10 flights per source, average ~8

	// Send initial progress
	initialProgress := map[string]interface{}{
		"type":     "progress",
		"progress": 0,
		"status":   "searching",
		"message":  "Starting flight search...",
	}
	initialData, _ := json.Marshal(initialProgress)
	conn.WriteMessage(websocket.TextMessage, initialData)

	for msg := range ch {
		var flightData map[string]interface{}
		if err := json.Unmarshal([]byte(msg.Payload), &flightData); err != nil {
			log.Println("Failed to parse flight data:", err)
			continue
		}

		// Check if this is a cancellation message
		if flightData["type"] == "cancelled" {
			cancelMsg := map[string]interface{}{
				"type":     "cancelled",
				"progress": 0,
				"status":   "cancelled",
				"message":  "Search was cancelled",
			}
			cancelData, _ := json.Marshal(cancelMsg)
			conn.WriteMessage(websocket.TextMessage, cancelData)
			return
		}

		// Increment received flights
		receivedFlights++

		// Calculate progress based on received flights vs expected
		progress := int((float64(receivedFlights) / float64(expectedFlights)) * 100)
		if progress > 100 {
			progress = 100
		}

		// Add progress info to flight data
		flightData["progress"] = progress
		flightData["received_flights"] = receivedFlights
		flightData["total_expected"] = expectedFlights
		flightData["status"] = "searching"

		// Check if we've received enough flights to consider search complete
		if receivedFlights >= expectedFlights {
			flightData["status"] = "completed"
			flightData["progress"] = 99
			flightData["message"] = "Search completed"
		}

		// Send flight data with progress
		flightDataBytes, _ := json.Marshal(flightData)
		err := conn.WriteMessage(websocket.TextMessage, flightDataBytes)
		if err != nil {
			log.Println("WebSocket write error:", err)
			break
		}

		// If search is completed, send final completion message
		if flightData["status"] == "completed" {
			completionMsg := map[string]interface{}{
				"type":          "completed",
				"progress":      100,
				"status":        "completed",
				"message":       "All flights found",
				"total_flights": receivedFlights,
			}
			completionData, _ := json.Marshal(completionMsg)
			conn.WriteMessage(websocket.TextMessage, completionData)
			break
		}
	}
}

// === Background Pubsub Listener ===
// This listener subscribes to pubsub and writes all messages to Redis list
func startPubsubListener(ctx context.Context, queryID string) {
	// Create a context for this listener
	listenerCtx, cancel := context.WithCancel(ctx)
	pubsubListeners.Store(queryID, cancel)
	defer pubsubListeners.Delete(queryID)

	// Subscribe to Redis pubsub channel
	sub := rdb.Subscribe(listenerCtx, "flight:"+queryID)
	defer sub.Close()
	ch := sub.Channel()

	// Initialize Redis list for this query
	resultsKey := "flight_results:" + queryID
	countKey := "flight_count:" + queryID

	log.Printf("Started pubsub listener for query %s", queryID)

	for {
		select {
		case <-listenerCtx.Done():
			log.Printf("Pubsub listener cancelled for query %s", queryID)
			return
		case msg, ok := <-ch:
			if !ok {
				log.Printf("Pubsub channel closed for query %s", queryID)
				return
			}

			// Write message to Redis list
			rdb.RPush(listenerCtx, resultsKey, msg.Payload)
			rdb.Expire(listenerCtx, resultsKey, 30*time.Minute)

			// Increment count
			rdb.Incr(listenerCtx, countKey)
			rdb.Expire(listenerCtx, countKey, 30*time.Minute)

			log.Printf("Wrote pubsub message to Redis for query %s", queryID)
		}
	}
}

// === Long Polling Handler ===
func LongPollHandler(c *gin.Context) {
	queryID := c.Query("query_id")
	if queryID == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "query_id is required"})
		return
	}

	// Get last_seen_index from query parameter (default to 0)
	lastSeenIndex := 0
	if lastSeenStr := c.Query("last_seen_index"); lastSeenStr != "" {
		if idx, err := fmt.Sscanf(lastSeenStr, "%d", &lastSeenIndex); err != nil || idx != 1 {
			lastSeenIndex = 0
		}
	}

	// Set headers for long polling
	c.Header("Cache-Control", "no-cache")
	c.Header("Connection", "keep-alive")
	c.Header("Content-Type", "application/json")

	resultsKey := "flight_results:" + queryID
	countKey := "flight_count:" + queryID
	ctx := context.Background()

	// Get total count of flights
	totalCount := 0
	if countStr, err := rdb.Get(ctx, countKey).Result(); err == nil {
		if _, err := fmt.Sscanf(countStr, "%d", &totalCount); err != nil {
			log.Println("Failed to parse flight count:", err)
		}
	}

	// Check if there are new results
	if lastSeenIndex < totalCount {
		// Get new results from Redis list (from lastSeenIndex to end)
		results, err := rdb.LRange(ctx, resultsKey, int64(lastSeenIndex), -1).Result()
		if err != nil {
			log.Printf("Failed to read results from Redis: %v", err)
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to read results"})
			return
		}

		// Parse and return the first new result (or batch if needed)
		if len(results) > 0 {
			var flightData map[string]interface{}
			if err := json.Unmarshal([]byte(results[0]), &flightData); err != nil {
				log.Println("Failed to parse flight data:", err)
				c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to parse flight data"})
				return
			}

			// Check if this is a cancellation message
			if flightData["type"] == "cancelled" {
				cancelMsg := map[string]interface{}{
					"type":            "cancelled",
					"progress":        0,
					"status":          "cancelled",
					"message":         "Search was cancelled",
					"last_seen_index": lastSeenIndex + 1,
				}
				c.JSON(http.StatusOK, cancelMsg)
				return
			}

			// Check if this is a completion message
			if flightData["type"] == "completed" {
				completionMsg := map[string]interface{}{
					"type":            "completed",
					"progress":        100,
					"status":          "completed",
					"message":         "All flights found",
					"total_flights":   flightData["total_flights"],
					"last_seen_index": lastSeenIndex + 1,
				}
				c.JSON(http.StatusOK, completionMsg)
				return
			}

			// Calculate progress
			expectedFlights := 24 // 8-10 flights per source, average ~8
			progress := int((float64(totalCount) / float64(expectedFlights)) * 100)
			if progress > 100 {
				progress = 100
			}

			// Add progress info to flight data
			flightData["progress"] = progress
			flightData["received_flights"] = totalCount
			flightData["total_expected"] = expectedFlights
			flightData["status"] = "searching"
			flightData["last_seen_index"] = lastSeenIndex + 1

			// Check if search is complete
			if totalCount >= expectedFlights {
				flightData["status"] = "completed"
				flightData["progress"] = 100
			}

			c.JSON(http.StatusOK, flightData)
			return
		}
	}

	// No new results yet - wait with timeout
	timeout := time.After(5 * time.Minute)           // 5 minute timeout for long polling
	ticker := time.NewTicker(500 * time.Millisecond) // Check every 500ms
	defer ticker.Stop()

	for {
		select {
		case <-ticker.C:
			// Check again for new results
			if countStr, err := rdb.Get(ctx, countKey).Result(); err == nil {
				var currentCount int
				if _, err := fmt.Sscanf(countStr, "%d", &currentCount); err == nil {
					if currentCount > lastSeenIndex {
						// New results available, get them
						results, err := rdb.LRange(ctx, resultsKey, int64(lastSeenIndex), -1).Result()
						if err == nil && len(results) > 0 {
							var flightData map[string]interface{}
							if err := json.Unmarshal([]byte(results[0]), &flightData); err == nil {
								expectedFlights := 24
								progress := int((float64(currentCount) / float64(expectedFlights)) * 100)
								if progress > 100 {
									progress = 100
								}

								flightData["progress"] = progress
								flightData["received_flights"] = currentCount
								flightData["total_expected"] = expectedFlights
								flightData["status"] = "searching"
								flightData["last_seen_index"] = lastSeenIndex + 1

								if currentCount >= expectedFlights {
									flightData["status"] = "completed"
									flightData["progress"] = 100
								}

								c.JSON(http.StatusOK, flightData)
								return
							}
						}
					}
				}
			}

		case <-timeout:
			// Timeout reached - send current progress and indicate client should continue polling
			expectedFlights := 24
			progress := int((float64(totalCount) / float64(expectedFlights)) * 100)
			timeoutMsg := map[string]interface{}{
				"type":             "timeout",
				"progress":         progress,
				"status":           "searching",
				"message":          "Search still in progress. Please continue polling.",
				"received_flights": totalCount,
				"total_expected":   expectedFlights,
				"should_continue":  true,
				"timeout_seconds":  300, // 5 minutes
				"last_seen_index":  lastSeenIndex,
			}
			c.JSON(http.StatusOK, timeoutMsg)
			return
		}
	}
}

// === Server-Sent Events Handler ===
func SSEHandler(c *gin.Context) {
	queryID := c.Query("query_id")
	if queryID == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "query_id is required"})
		return
	}

	// Set headers for SSE
	c.Header("Content-Type", "text/event-stream")
	c.Header("Cache-Control", "no-cache")
	c.Header("Connection", "keep-alive")
	c.Header("Access-Control-Allow-Origin", "*")
	c.Header("Access-Control-Allow-Headers", "Cache-Control")

	// Subscribe to Redis channel
	sub := rdb.Subscribe(context.Background(), "flight:"+queryID)
	defer sub.Close()
	ch := sub.Channel()

	// Track progress
	receivedFlights := 0
	expectedFlights := 24 // 8-10 flights per source, average ~8

	// Send initial progress
	initialProgress := map[string]interface{}{
		"type":     "progress",
		"progress": 0,
		"status":   "searching",
		"message":  "Starting flight search...",
	}
	initialData, _ := json.Marshal(initialProgress)
	c.SSEvent("progress", string(initialData))

	// Create a context that cancels when the client disconnects
	ctx, cancel := context.WithCancel(c.Request.Context())
	defer cancel()

	// Wait for messages
	for {
		select {
		case msg := <-ch:
			var flightData map[string]interface{}
			if err := json.Unmarshal([]byte(msg.Payload), &flightData); err != nil {
				log.Println("Failed to parse flight data:", err)
				continue
			}

			// Check if this is a cancellation message
			if flightData["type"] == "cancelled" {
				cancelMsg := map[string]interface{}{
					"type":     "cancelled",
					"progress": 0,
					"status":   "cancelled",
					"message":  "Search was cancelled",
				}
				cancelData, _ := json.Marshal(cancelMsg)
				c.SSEvent("cancelled", string(cancelData))
				return
			}

			// Increment received flights
			receivedFlights++

			// Calculate progress based on received flights vs expected
			progress := int((float64(receivedFlights) / float64(expectedFlights)) * 100)
			if progress > 100 {
				progress = 100
			}

			// Add progress info to flight data
			flightData["progress"] = progress
			flightData["received_flights"] = receivedFlights
			flightData["total_expected"] = expectedFlights
			flightData["status"] = "searching"

			// Check if we've received enough flights to consider search complete
			if receivedFlights >= expectedFlights {
				flightData["status"] = "completed"
				flightData["progress"] = 99
				flightData["message"] = "Search completed"
			}

			// Send flight data with progress
			flightDataBytes, _ := json.Marshal(flightData)
			c.SSEvent("flight", string(flightDataBytes))

			// If search is completed, send final completion message
			if flightData["status"] == "completed" {
				completionMsg := map[string]interface{}{
					"type":          "completed",
					"progress":      100,
					"status":        "completed",
					"message":       "All flights found",
					"total_flights": receivedFlights,
				}
				completionData, _ := json.Marshal(completionMsg)
				c.SSEvent("completed", string(completionData))
				return
			}

		case <-ctx.Done():
			// Client disconnected or context cancelled
			log.Println("SSE client disconnected")
			return

		case <-time.After(30 * time.Second):
			// Timeout reached
			timeoutMsg := map[string]interface{}{
				"type":     "timeout",
				"progress": int((float64(receivedFlights) / float64(expectedFlights)) * 100),
				"status":   "timeout",
				"message":  "Search timeout - please try again",
			}
			timeoutData, _ := json.Marshal(timeoutMsg)
			c.SSEvent("timeout", string(timeoutData))
			return
		}
	}
}

// === Aggregator: Simulate API Calls ===
func runAggregator(ctx context.Context, queryID, from, to, tripType, departureDate, returnDate string, pax int) {
	sources := []string{"kiwi", "trip", "12go"}

	// Flight times from morning to night
	departureTimes := []string{"06:00", "08:30", "10:15", "12:00", "14:30", "16:45", "18:20", "20:00", "22:30"}

	// Airlines for each source
	airlines := map[string][]string{
		"kiwi": {"Lion Air", "Garuda", "AirAsia", "Batik Air", "Citilink"},
		"trip": {"Singapore Airlines", "Malaysia Airlines", "Thai Airways", "Vietnam Airlines", "Philippine Airlines"},
		"12go": {"Cebu Pacific", "Jetstar", "Tiger Air", "Scoot", "AirAsia"},
	}

	// Common flights that multiple sources might offer
	commonFlights := []map[string]interface{}{
		{
			"airline":        "AirAsia",
			"flight_number":  "AK123",
			"departure_time": "10:15",
			"base_price":     750000,
		},
		{
			"airline":        "Garuda",
			"flight_number":  "GA456",
			"departure_time": "14:30",
			"base_price":     1200000,
		},
		{
			"airline":        "Lion Air",
			"flight_number":  "JT789",
			"departure_time": "08:30",
			"base_price":     650000,
		},
	}

	// Track total flights sent
	totalFlightsSent := 0
	expectedTotalFlights := 0

	// Calculate expected total flights (3 sources, 8-10 flights each)
	expectedTotalFlights = len(sources) * (8 + rand.Intn(3)) // Random between 8-10 flights per source

	// Use a mutex to safely increment the counter
	var mu sync.Mutex

	for _, source := range sources {
		go func(sourceName string) {
			// Simulate multiple flights per source (8-10 flights)
			numFlights := 8 + rand.Intn(3) // Random between 8-10 flights

			for i := 0; i < numFlights; i++ {
				select {
				case <-ctx.Done():
					log.Println("Cancelled fetching:", sourceName)
					return
				default:
					// Random delay between 100-800 milliseconds
					delay := time.Duration(10+rand.Intn(700)) * time.Millisecond
					time.Sleep(delay)

					var result map[string]interface{}

					// 30% chance to offer a common flight (same flight across multiple sources)
					if rand.Float32() < 0.3 && i < len(commonFlights) {
						commonFlight := commonFlights[i]

						// Add price variation for the same flight across different sources
						priceVariation := rand.Intn(100000) - 50000 // ±50k variation
						price := commonFlight["base_price"].(int) + priceVariation
						if price < 500000 {
							price = 500000
						}

						// Adjust price based on PAX
						totalPrice := price * pax

						result = map[string]interface{}{
							"source":         sourceName,
							"airline":        commonFlight["airline"],
							"flight_number":  commonFlight["flight_number"],
							"departure_time": commonFlight["departure_time"],
							"price":          totalPrice,
							"from":           from,
							"to":             to,
							"departure_date": departureDate,
							"return_date":    returnDate,
							"trip_type":      tripType,
							"pax":            pax,
							"timestamp":      time.Now().Format("2006-01-02 15:04:05"),
							"seat_class":     []string{"Economy", "Business", "Premium Economy"}[rand.Intn(3)],
							"affiliate_link": fmt.Sprintf("https://%s.com/affiliate?source=flight_search&from=%s&to=%s&flight=%s&price=%d&pax=%d&ref=YOUR_AFFILIATE_ID", sourceName, from, to, commonFlight["flight_number"], totalPrice, pax),
							"booking_url":    fmt.Sprintf("https://%s.com/flights/%s-%s/%s?departure_date=%s&pax=%d", sourceName, from, to, commonFlight["flight_number"], departureDate, pax),
							"is_common":      true, // Flag to indicate this is a common flight
						}

						log.Printf("Common flight from %s: %s - %s at %s (Price: %d)", sourceName, commonFlight["airline"], commonFlight["flight_number"], commonFlight["departure_time"], price)
					} else {
						// Random departure time
						departureTime := departureTimes[rand.Intn(len(departureTimes))]

						// Random airline from the source
						airline := airlines[sourceName][rand.Intn(len(airlines[sourceName]))]

						// Random price between 500k-2.5M IDR
						basePrice := 500000 + rand.Intn(2000000)

						// Random flight number
						flightNumber := fmt.Sprintf("%s%d", strings.ToUpper(sourceName[:2]), 100+rand.Intn(900))

						// Adjust price based on PAX
						totalPrice := basePrice * pax

						result = map[string]interface{}{
							"source":         sourceName,
							"airline":        airline,
							"flight_number":  flightNumber,
							"departure_time": departureTime,
							"price":          totalPrice,
							"from":           from,
							"to":             to,
							"departure_date": departureDate,
							"return_date":    returnDate,
							"trip_type":      tripType,
							"pax":            pax,
							"timestamp":      time.Now().Format("2006-01-02 15:04:05"),
							"seat_class":     []string{"Economy", "Business", "Premium Economy"}[rand.Intn(3)],
							"affiliate_link": fmt.Sprintf("https://%s.com/affiliate?source=flight_search&from=%s&to=%s&flight=%s&price=%d&pax=%d&ref=YOUR_AFFILIATE_ID", sourceName, from, to, flightNumber, totalPrice, pax),
							"booking_url":    fmt.Sprintf("https://%s.com/flights/%s-%s/%s?departure_date=%s&pax=%d", sourceName, from, to, flightNumber, departureDate, pax),
							"is_common":      false,
						}
					}

					data, _ := json.Marshal(result)
					rdb.Publish(ctx, "flight:"+queryID, data)
					rdb.Set(ctx, "search_result:"+queryID+":"+sourceName+":"+fmt.Sprintf("%d", i), data, 30*time.Minute)

					// Increment total flights sent
					mu.Lock()
					totalFlightsSent++
					currentTotal := totalFlightsSent
					mu.Unlock()

					log.Printf("Flight result from %s: %s - %s at %s (Total: %d/%d)", sourceName, result["airline"], result["flight_number"], result["departure_time"], currentTotal, expectedTotalFlights)

					// Check if all flights have been sent
					if currentTotal >= expectedTotalFlights {
						// Send completion message
						log.Printf("Sending completion message for query %s (Total: %d/%d)", queryID, currentTotal, expectedTotalFlights)
						completionMsg := map[string]interface{}{
							"type":          "completed",
							"progress":      100,
							"status":        "completed",
							"message":       "All flights found",
							"total_flights": currentTotal,
						}
						completionData, _ := json.Marshal(completionMsg)
						rdb.Publish(ctx, "flight:"+queryID, completionData)
					}
				}
			}
		}(source)
	}
}

// === Helper ===
func generateQueryID(from, to, tripType, departureDate, returnDate string, pax int) string {
	h := sha256.New()
	h.Write([]byte(fmt.Sprintf("%s:%s:%s:%s:%s:%d:%d", from, to, tripType, departureDate, returnDate, pax, time.Now().UnixNano())))
	return hex.EncodeToString(h.Sum(nil))
}

func validateInvoice(body []byte) InvoiceValidationResponse {
	response := InvoiceValidationResponse{
		IsValid:  true,
		Errors:   []string{},
		Warnings: []string{},
	}

	// Parse the wrapper structure first
	var wrapper map[string]interface{}
	if err := json.Unmarshal(body, &wrapper); err != nil {
		response.IsValid = false
		response.Errors = append(response.Errors, "Invalid JSON format")
		return response
	}

	// Check for ProductOrder
	if productData, exists := wrapper["ProductOrder"]; exists {
		productJSON, _ := json.Marshal(productData)
		var productOrder ProductOrder
		if err := json.Unmarshal(productJSON, &productOrder); err == nil {
			response.OrderType = "Product"
			response.OrderID = productOrder.TransactionDetails.OrderID
			validateProductOrder(&productOrder, &response)
			return response
		}
	}

	// Check for ServiceOrder
	if serviceData, exists := wrapper["ServiceOrder"]; exists {
		serviceJSON, _ := json.Marshal(serviceData)
		var serviceOrder ServiceOrder
		if err := json.Unmarshal(serviceJSON, &serviceOrder); err == nil {
			response.OrderType = "Service"
			response.OrderID = serviceOrder.TransactionDetails.OrderID
			validateServiceOrder(&serviceOrder, &response)
			return response
		}
	}

	// Check for ReservationOrder
	if reservationData, exists := wrapper["ReservationOrder"]; exists {
		reservationJSON, _ := json.Marshal(reservationData)
		var reservationOrder ReservationOrder
		if err := json.Unmarshal(reservationJSON, &reservationOrder); err == nil {
			response.OrderType = "Reservation"
			response.OrderID = reservationOrder.TransactionDetails.OrderID
			validateReservationOrder(&reservationOrder, &response)
			return response
		}
	}

	// Check for AirlinesOrder
	if airlinesData, exists := wrapper["AirlinesOrder"]; exists {
		airlinesJSON, _ := json.Marshal(airlinesData)
		var airlinesOrder AirlinesOrder
		if err := json.Unmarshal(airlinesJSON, &airlinesOrder); err == nil {
			response.OrderType = "Airline"
			response.OrderID = airlinesOrder.TransactionDetails.OrderID
			validateAirlinesOrder(&airlinesOrder, &response)
			return response
		}
	}

	response.IsValid = false
	response.Errors = append(response.Errors, "Invalid order format - could not parse as any known order type")
	return response
}

func validateProductOrder(order *ProductOrder, response *InvoiceValidationResponse) {
	calculatedTotal := 0.0
	totalTax := 0.0
	totalDiscounts := 0.0
	subtotalBeforeTaxAndDiscounts := 0.0
	subtotalAfterDiscounts := 0.0
	subtotalAfterTax := 0.0

	// Validate each item
	for i, item := range order.Items {
		// Calculate item breakdown
		basePrice := item.BasePrice * float64(item.Qty)
		itemDiscounts := 0.0
		itemAddons := 0.0

		// Calculate item-level discounts and addons
		for _, priceInfo := range item.PriceInfo {
			if priceInfo.Amount < 0 {
				itemDiscounts += math.Abs(priceInfo.Amount)
			} else {
				itemAddons += priceInfo.Amount
			}
		}

		subtotalBeforeTaxAndDiscounts += basePrice + itemAddons
		subtotalAfterDiscounts += basePrice + itemAddons - itemDiscounts
		subtotalAfterTax += basePrice + itemAddons - itemDiscounts + item.Tax

		// Calculate final item total
		itemTotal := basePrice + itemAddons - itemDiscounts + item.Tax + item.ShippingDetails.Cost

		// Create item breakdown
		itemCalc := ItemCalculation{
			ItemName:                      item.Name,
			ItemIndex:                     i + 1,
			BasePrice:                     item.BasePrice,
			Quantity:                      item.Qty,
			SubtotalBeforeTaxAndDiscounts: basePrice + itemAddons,
			Discounts:                     itemDiscounts,
			SubtotalAfterDiscounts:        basePrice + itemAddons - itemDiscounts,
			Tax:                           item.Tax,
			Shipping:                      item.ShippingDetails.Cost,
			Addons:                        itemAddons,
			FinalTotal:                    itemTotal,
			DeclaredTotal:                 item.TotalPrice,
			IsValid:                       math.Abs(itemTotal-item.TotalPrice) <= 0.01,
		}
		response.Summary.ItemBreakdown = append(response.Summary.ItemBreakdown, itemCalc)

		// Validate item total
		if !itemCalc.IsValid {
			response.Errors = append(response.Errors,
				fmt.Sprintf("Item %d (%s): Calculated total %.2f does not match declared total %.2f",
					i+1, item.Name, itemTotal, item.TotalPrice))
			response.IsValid = false
		}

		totalTax += item.Tax
		totalDiscounts += itemDiscounts
		calculatedTotal += itemTotal
	}

	// Add order-level discounts
	for _, discount := range order.TransactionDetails.OrderLevelDiscounts {
		calculatedTotal += discount.Amount
		totalDiscounts += math.Abs(discount.Amount)
		subtotalAfterDiscounts += discount.Amount
	}

	// Validate order summary if present
	if order.OrderSummary != nil {
		if math.Abs(order.OrderSummary.Subtotal-calculatedTotal) > 0.01 {
			response.Warnings = append(response.Warnings,
				fmt.Sprintf("Order summary subtotal %.2f does not match calculated total %.2f",
					order.OrderSummary.Subtotal, calculatedTotal))
		}
	}

	// Validate gross amount
	if math.Abs(calculatedTotal-order.TransactionDetails.GrossAmt) > 0.01 {
		response.Errors = append(response.Errors,
			fmt.Sprintf("Calculated total %.2f does not match declared gross amount %.2f",
				calculatedTotal, order.TransactionDetails.GrossAmt))
		response.IsValid = false
	}

	response.Summary.CalculatedTotal = calculatedTotal
	response.Summary.DeclaredTotal = order.TransactionDetails.GrossAmt
	response.Summary.TotalTax = totalTax
	response.Summary.TotalDiscounts = totalDiscounts
	response.Summary.Currency = order.TransactionDetails.Currency
	response.Summary.SubtotalBeforeTaxAndDiscounts = subtotalBeforeTaxAndDiscounts
	response.Summary.SubtotalAfterDiscounts = subtotalAfterDiscounts
	response.Summary.SubtotalAfterTax = subtotalAfterTax
}

func validateServiceOrder(order *ServiceOrder, response *InvoiceValidationResponse) {
	calculatedTotal := 0.0
	totalTax := 0.0
	totalDiscounts := 0.0
	subtotalBeforeTaxAndDiscounts := 0.0
	subtotalAfterDiscounts := 0.0
	subtotalAfterTax := 0.0

	// Validate each service item
	for i, item := range order.Items {
		// Calculate item breakdown
		basePrice := item.BasePrice
		itemAddons := 0.0

		// Add addon costs
		for _, priceInfo := range item.PriceInfo {
			itemAddons += priceInfo.Amount
		}

		subtotalBeforeTaxAndDiscounts += basePrice + itemAddons

		// Apply discount
		discountAmount := 0.0
		if item.Discount.Value > 0 {
			if item.Discount.Type == "Percentage" {
				discountAmount = (basePrice + itemAddons) * (item.Discount.Value / 100)
			} else if item.Discount.Type == "Fixed" {
				discountAmount = item.Discount.Value
			}
		}

		subtotalAfterDiscounts += basePrice + itemAddons - discountAmount
		subtotalAfterTax += basePrice + itemAddons - discountAmount + item.Tax

		// Calculate final item total
		itemTotal := basePrice + itemAddons - discountAmount + item.Tax

		// Create item breakdown
		itemCalc := ItemCalculation{
			ItemName:                      item.Name,
			ItemIndex:                     i + 1,
			BasePrice:                     item.BasePrice,
			Quantity:                      1,
			SubtotalBeforeTaxAndDiscounts: basePrice + itemAddons,
			Discounts:                     discountAmount,
			SubtotalAfterDiscounts:        basePrice + itemAddons - discountAmount,
			Tax:                           item.Tax,
			Addons:                        itemAddons,
			FinalTotal:                    itemTotal,
			DeclaredTotal:                 item.TotalPrice,
			IsValid:                       math.Abs(itemTotal-item.TotalPrice) <= 0.01,
		}
		response.Summary.ItemBreakdown = append(response.Summary.ItemBreakdown, itemCalc)

		// Validate item total
		if !itemCalc.IsValid {
			response.Errors = append(response.Errors,
				fmt.Sprintf("Service %d (%s): Calculated total %.2f does not match declared total %.2f",
					i+1, item.Name, itemTotal, item.TotalPrice))
			response.IsValid = false
		}

		totalTax += item.Tax
		totalDiscounts += discountAmount
		calculatedTotal += itemTotal
	}

	// Validate gross amount
	if math.Abs(calculatedTotal-order.TransactionDetails.GrossAmt) > 0.01 {
		response.Errors = append(response.Errors,
			fmt.Sprintf("Calculated total %.2f does not match declared gross amount %.2f",
				calculatedTotal, order.TransactionDetails.GrossAmt))
		response.IsValid = false
	}

	response.Summary.CalculatedTotal = calculatedTotal
	response.Summary.DeclaredTotal = order.TransactionDetails.GrossAmt
	response.Summary.TotalTax = totalTax
	response.Summary.TotalDiscounts = totalDiscounts
	response.Summary.Currency = order.TransactionDetails.Currency
	response.Summary.SubtotalBeforeTaxAndDiscounts = subtotalBeforeTaxAndDiscounts
	response.Summary.SubtotalAfterDiscounts = subtotalAfterDiscounts
	response.Summary.SubtotalAfterTax = subtotalAfterTax
}

func validateReservationOrder(order *ReservationOrder, response *InvoiceValidationResponse) {
	calculatedTotal := 0.0
	totalTax := 0.0
	totalDiscounts := 0.0
	subtotalBeforeTaxAndDiscounts := 0.0
	subtotalAfterDiscounts := 0.0
	subtotalAfterTax := 0.0

	// Validate each reservation item
	for i, item := range order.Items {
		// Calculate item breakdown
		basePrice := item.BasePrice
		itemAddons := 0.0

		// Add additional costs
		for _, priceInfo := range item.PriceInfo {
			itemAddons += priceInfo.Amount
		}

		subtotalBeforeTaxAndDiscounts += basePrice + itemAddons
		subtotalAfterDiscounts += basePrice + itemAddons
		subtotalAfterTax += basePrice + itemAddons + item.Tax

		// Calculate final item total
		itemTotal := basePrice + itemAddons + item.Tax

		// Create item breakdown
		itemCalc := ItemCalculation{
			ItemName:                      item.MerchantName,
			ItemIndex:                     i + 1,
			BasePrice:                     item.BasePrice,
			Quantity:                      1,
			SubtotalBeforeTaxAndDiscounts: basePrice + itemAddons,
			Discounts:                     0,
			SubtotalAfterDiscounts:        basePrice + itemAddons,
			Tax:                           item.Tax,
			Addons:                        itemAddons,
			FinalTotal:                    itemTotal,
			DeclaredTotal:                 item.TotalPrice,
			IsValid:                       math.Abs(itemTotal-item.TotalPrice) <= 0.01,
		}
		response.Summary.ItemBreakdown = append(response.Summary.ItemBreakdown, itemCalc)

		// Validate item total
		if !itemCalc.IsValid {
			response.Errors = append(response.Errors,
				fmt.Sprintf("Reservation %d (%s): Calculated total %.2f does not match declared total %.2f",
					i+1, item.MerchantName, itemTotal, item.TotalPrice))
			response.IsValid = false
		}

		totalTax += item.Tax
		calculatedTotal += itemTotal
	}

	// Validate gross amount
	if math.Abs(calculatedTotal-order.TransactionDetails.GrossAmt) > 0.01 {
		response.Errors = append(response.Errors,
			fmt.Sprintf("Calculated total %.2f does not match declared gross amount %.2f",
				calculatedTotal, order.TransactionDetails.GrossAmt))
		response.IsValid = false
	}

	response.Summary.CalculatedTotal = calculatedTotal
	response.Summary.DeclaredTotal = order.TransactionDetails.GrossAmt
	response.Summary.TotalTax = totalTax
	response.Summary.TotalDiscounts = totalDiscounts
	response.Summary.Currency = order.TransactionDetails.Currency
	response.Summary.SubtotalBeforeTaxAndDiscounts = subtotalBeforeTaxAndDiscounts
	response.Summary.SubtotalAfterDiscounts = subtotalAfterDiscounts
	response.Summary.SubtotalAfterTax = subtotalAfterTax
}

func validateAirlinesOrder(order *AirlinesOrder, response *InvoiceValidationResponse) {
	calculatedTotal := 0.0
	totalTax := 0.0
	totalDiscounts := 0.0
	subtotalBeforeTaxAndDiscounts := 0.0
	subtotalAfterDiscounts := 0.0
	subtotalAfterTax := 0.0

	// Validate each airline item
	for i, item := range order.Items {
		// Calculate item breakdown
		basePrice := item.BasePrice
		itemAddons := 0.0

		// Add additional costs
		for _, priceInfo := range item.PriceInfo {
			itemAddons += priceInfo.Amount
		}

		subtotalBeforeTaxAndDiscounts += basePrice + itemAddons
		subtotalAfterDiscounts += basePrice + itemAddons
		subtotalAfterTax += basePrice + itemAddons + item.Tax

		// Calculate final item total
		itemTotal := basePrice + itemAddons + item.Tax

		// Create item breakdown
		itemCalc := ItemCalculation{
			ItemName:                      fmt.Sprintf("Flight (PNR: %s)", item.PNRNumber),
			ItemIndex:                     i + 1,
			BasePrice:                     item.BasePrice,
			Quantity:                      1,
			SubtotalBeforeTaxAndDiscounts: basePrice + itemAddons,
			Discounts:                     0,
			SubtotalAfterDiscounts:        basePrice + itemAddons,
			Tax:                           item.Tax,
			Addons:                        itemAddons,
			FinalTotal:                    itemTotal,
			DeclaredTotal:                 item.TotalPrice,
			IsValid:                       math.Abs(itemTotal-item.TotalPrice) <= 0.01,
		}
		response.Summary.ItemBreakdown = append(response.Summary.ItemBreakdown, itemCalc)

		// Validate item total
		if !itemCalc.IsValid {
			response.Errors = append(response.Errors,
				fmt.Sprintf("Flight %d (PNR: %s): Calculated total %.2f does not match declared total %.2f",
					i+1, item.PNRNumber, itemTotal, item.TotalPrice))
			response.IsValid = false
		}

		totalTax += item.Tax
		calculatedTotal += itemTotal
	}

	// Validate gross amount
	if math.Abs(calculatedTotal-order.TransactionDetails.GrossAmt) > 0.01 {
		response.Errors = append(response.Errors,
			fmt.Sprintf("Calculated total %.2f does not match declared gross amount %.2f",
				calculatedTotal, order.TransactionDetails.GrossAmt))
		response.IsValid = false
	}

	response.Summary.CalculatedTotal = calculatedTotal
	response.Summary.DeclaredTotal = order.TransactionDetails.GrossAmt
	response.Summary.TotalTax = totalTax
	response.Summary.TotalDiscounts = totalDiscounts
	response.Summary.Currency = order.TransactionDetails.Currency
	response.Summary.SubtotalBeforeTaxAndDiscounts = subtotalBeforeTaxAndDiscounts
	response.Summary.SubtotalAfterDiscounts = subtotalAfterDiscounts
	response.Summary.SubtotalAfterTax = subtotalAfterTax
}
