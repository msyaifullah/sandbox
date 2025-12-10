# Flight Search App Demo

## Quick Start

1. **Start the Backend Service**
   ```bash
   # Navigate to the Go service directory
   cd ../service-b
   
   # Start the Go backend (make sure Redis is running)
   go run main.go
   ```
   The backend will start on `http://localhost:3001`

2. **Start the React App**
   ```bash
   # In the flight-search-app directory
   npm start
   ```
   The React app will start on `http://localhost:3000`

3. **Test the Application**
   - Open your browser to `http://localhost:3000`
   - Enter departure city (e.g., "Jakarta")
   - Enter destination city (e.g., "Bali")
   - Click "Search Flights"
   - Watch the progress bar and real-time results

## Demo Scenarios

### Scenario 1: Basic Flight Search
1. Enter "Jakarta" as departure
2. Enter "Bali" as destination
3. Click "Search Flights"
4. Observe the progress bar filling up
5. Watch flight results stream in real-time
6. Notice the different sources (kiwi, trip, 12go)

### Scenario 2: Cancel Search
1. Start a search as above
2. While results are streaming, click "Cancel Search"
3. Observe the search stopping immediately
4. Notice the progress bar completing

### Scenario 3: Error Handling
1. Stop the backend service
2. Try to search for flights
3. Observe the error message
4. Restart the backend and try again

## Features Demonstrated

### Real-time Streaming
- Flight results appear as they're found
- Each result shows source, airline, flight number, price
- Timestamps show when each result was found

### Progress Tracking
- Visual progress bar during search
- Percentage completion indicator
- Streaming indicator with animated pulse

### Modern UI
- Glassmorphism design effects
- Responsive grid layout
- Hover animations on flight cards
- Mobile-friendly design

### WebSocket Integration
- Real-time connection to backend
- Automatic reconnection handling
- Message parsing and state updates

## API Endpoints Tested

### GET /api/search
```bash
curl "http://localhost:3001/api/search?from=Jakarta&to=Bali"
```
Response:
```json
{
  "query_id": "abc123...",
  "ws_url": "ws://localhost:3001/ws/result/stream?query_id=abc123..."
}
```

### POST /api/search/cancel
```bash
curl -X POST "http://localhost:3001/api/search/cancel?query_id=abc123..."
```
Response:
```json
{
  "status": "cancelled"
}
```

### WebSocket Stream
```javascript
const ws = new WebSocket("ws://localhost:3001/ws/result/stream?query_id=abc123...");
ws.onmessage = (event) => {
  const flight = JSON.parse(event.data);
  console.log('New flight:', flight);
};
```

## Expected Behavior

1. **Search Initiation**: Progress bar starts at 0%
2. **Backend Processing**: 10-second delay (simulated processing)
3. **Results Streaming**: Flights appear every 2-8 seconds
4. **Progress Updates**: Bar fills as results arrive
5. **Completion**: Progress reaches 100% when all sources finish

## Troubleshooting

### Common Issues

1. **Backend Connection Failed**
   - Ensure Go service is running on port 3001
   - Check Redis is running (required for backend)

2. **WebSocket Connection Failed**
   - Verify backend is running
   - Check browser console for connection errors

3. **No Results Appearing**
   - Wait for the 10-second initial delay
   - Check backend logs for errors
   - Verify Redis is working properly

### Debug Mode

Enable console logging by opening browser dev tools:
- Network tab: Monitor API calls
- Console tab: View WebSocket messages
- Performance tab: Monitor app performance

## Performance Notes

- Search typically takes 30-60 seconds to complete
- Results stream in from 3 different sources
- Each source provides 8-10 flight options
- Total results: 24-30 flights typically

## Browser Compatibility

Tested and working on:
- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+ 