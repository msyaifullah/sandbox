# Flight Search App

A React application that demonstrates real-time flight search with multiple streaming methods.

## Features

- **Real-time Flight Search**: Search for flights with live streaming results
- **Multiple Streaming Methods**: Choose between three different streaming approaches:
  - **WebSocket**: Full-duplex real-time communication
  - **Long Polling**: HTTP-based polling with automatic reconnection
  - **Server-Sent Events (SSE)**: One-way server streaming with automatic reconnection
- **Progress Tracking**: Real-time progress updates during search
- **Flight Results**: Display flight information with booking links
- **Search Cancellation**: Cancel ongoing searches
- **Responsive Design**: Works on desktop and mobile devices

## Streaming Methods Comparison

| Feature | WebSocket | Long Polling | SSE |
|---------|-----------|--------------|-----|
| **Bidirectional** | ✅ Yes | ❌ No | ❌ No |
| **Reconnection** | Manual | Automatic | Automatic |
| **Firewall Friendly** | ❌ No | ✅ Yes | ✅ Yes |
| **Real-time** | ✅ Yes | ⚠️ Near real-time | ✅ Yes |

## Getting Started

### Prerequisites

- Node.js (v14 or higher)
- npm or yarn
- Go (for the backend service)

### Installation

1. Install dependencies:
   ```bash
   npm install
   ```

2. Start the backend service (service-b):
   ```bash
   cd ../service-b
   go run .
   ```

3. Start the React development server:
   ```bash
   npm start
   ```

4. Open [http://localhost:3000](http://localhost:3000) in your browser.

## Usage

1. **Enter Flight Details**: 
   - Enter departure city (e.g., "JAK")
   - Enter destination city (e.g., "CGK")

2. **Select Streaming Method**:
   - **WebSocket**: Best for real-time bidirectional communication
   - **Long Polling**: Good for environments with restrictive firewalls
   - **Server-Sent Events (SSE)**: Best for one-way real-time updates

3. **Start Search**: Click "Search Flights" to begin the search

4. **Monitor Progress**: Watch real-time progress updates and flight results

5. **Cancel Search**: Use the "Cancel Search" button to stop an ongoing search

## API Endpoints

The app connects to the following backend endpoints:

- `GET /api/search` - Start a flight search
- `POST /api/search/cancel` - Cancel an ongoing search
- `GET /ws/result/stream` - WebSocket streaming endpoint
- `GET /api/result/longpoll` - Long polling endpoint
- `GET /api/result/sse` - Server-Sent Events endpoint

## Project Structure

```
src/
├── components/
│   ├── FlightSearchForm.tsx    # Search form with streaming method selector
│   ├── FlightResults.tsx       # Flight results display
│   └── ProgressBar.tsx         # Progress tracking component
├── hooks/
│   └── useFlightSearch.ts      # Main search logic and state management
├── services/
│   ├── api.ts                  # API service for HTTP requests
│   └── streamingService.ts     # Streaming service for all three methods
├── types/
│   └── index.ts                # TypeScript type definitions
└── App.tsx                     # Main application component
```

## Technical Details

### Streaming Service

The `StreamingService` class handles all three streaming methods:

- **WebSocket**: Uses native WebSocket API
- **Long Polling**: Uses axios with timeout and retry logic
- **SSE**: Uses native EventSource API

### State Management

The `useFlightSearch` hook manages:
- Search state (searching, streaming, progress)
- Flight results
- Error handling
- Connection management

### Error Handling

The app handles various error scenarios:
- Network connection issues
- Search timeouts
- Invalid responses
- Connection failures

## Development

### Available Scripts

- `npm start` - Start development server
- `npm test` - Run tests
- `npm run build` - Build for production
- `npm run eject` - Eject from Create React App

### Environment Variables

Create a `.env` file in the root directory:

```
REACT_APP_API_BASE_URL=http://localhost:3001
```

## Troubleshooting

### Common Issues

1. **Backend not running**: Make sure service-b is running on port 3001
2. **CORS errors**: Ensure the backend has proper CORS configuration
3. **WebSocket connection failed**: Check if the backend supports WebSocket
4. **Long polling timeouts**: May indicate network or server issues

### Debug Mode

Enable debug logging by opening browser console and looking for:
- Connection status messages
- Streaming data logs
- Error messages

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License.
