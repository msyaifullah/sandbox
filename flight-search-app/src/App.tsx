import React from 'react';
import './App.css';
import FlightSearchForm from './components/FlightSearchForm';
import ProgressBar from './components/ProgressBar';
import FlightResults from './components/FlightResults';
import { useFlightSearch } from './hooks/useFlightSearch';
import { StreamingMethod, TripType } from './types';

function App() {
  const {
    isSearching,
    results,
    error,
    progress,
    isStreaming,
    receivedFlights,
    totalExpected,
    streamingMethod,
    searchFlights,
    cancelSearch,
    resetSearch,
  } = useFlightSearch();

  const handleSearch = (
    from: string,
    to: string,
    tripType: TripType,
    departureDate: string,
    returnDate: string | undefined,
    pax: number,
    method: StreamingMethod
  ) => {
    searchFlights(from, to, tripType, departureDate, returnDate, pax, method);
  };

  return (
    <div className="App">
      <header className="App-header">
        <h1>✈️ Flight Search App</h1>
        <p>Search for flights with real-time streaming results</p>
      </header>

      <main className="App-main">
        <FlightSearchForm 
          onSearch={handleSearch} 
          isSearching={isSearching} 
        />

        {error && (
          <div className="error-message">
            <p>Error: {error}</p>
            <button onClick={resetSearch}>Try Again</button>
          </div>
        )}

        {(isSearching || isStreaming) && (
          <div className="streaming-info">
            <ProgressBar 
              progress={progress} 
              isStreaming={isStreaming}
              receivedFlights={receivedFlights}
              totalExpected={totalExpected}
            />
            <div className="streaming-method-indicator">
              <span>Using: </span>
              <span className={`method-badge ${streamingMethod}`}>
                {streamingMethod === 'websocket' && 'WebSocket'}
                {streamingMethod === 'longpoll' && 'Long Polling'}
                {streamingMethod === 'sse' && 'Server-Sent Events'}
              </span>
            </div>
          </div>
        )}

        {(results.length > 0 || isSearching) && (
          <FlightResults 
            results={results}
            onCancel={cancelSearch}
            isStreaming={isStreaming}
          />
        )}

        {!isSearching && !isStreaming && results.length === 0 && !error && (
          <div className="welcome-message">
            <h2>Welcome to Flight Search</h2>
            <p>Enter your departure and destination cities to start searching for flights.</p>
            <p>The search will simulate multiple flight sources and stream results in real-time.</p>
            <div className="streaming-methods-info">
              <h3>Available Streaming Methods:</h3>
              <ul>
                <li><strong>WebSocket:</strong> Full-duplex real-time communication</li>
                <li><strong>Long Polling:</strong> HTTP-based polling with automatic reconnection</li>
                <li><strong>Server-Sent Events (SSE):</strong> One-way server streaming with automatic reconnection</li>
              </ul>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

export default App;
