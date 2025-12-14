import React, { useState } from 'react';
import { StreamingMethod, TripType, Airport } from '../types';
import AirportAutocomplete from './AirportAutocomplete';

interface FlightSearchFormProps {
  onSearch: (
    from: string,
    to: string,
    tripType: TripType,
    departureDate: string,
    returnDate: string | undefined,
    pax: number,
    method: StreamingMethod
  ) => void;
  isSearching: boolean;
}

const FlightSearchForm: React.FC<FlightSearchFormProps> = ({ onSearch, isSearching }) => {
  // Get today's date in YYYY-MM-DD format for min date
  const today = new Date().toISOString().split('T')[0];

  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [fromAirport, setFromAirport] = useState<Airport | null>(null);
  const [toAirport, setToAirport] = useState<Airport | null>(null);
  const [tripType, setTripType] = useState<TripType>('one-way');
  const [departureDate, setDepartureDate] = useState(today);
  const [returnDate, setReturnDate] = useState('');
  const [pax, setPax] = useState(1);
  const [streamingMethod, setStreamingMethod] = useState<StreamingMethod>('websocket');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (from.trim() && to.trim() && departureDate) {
      const returnDateValue = tripType === 'round-trip' ? returnDate : undefined;
      onSearch(
        from.trim(),
        to.trim(),
        tripType,
        departureDate,
        returnDateValue,
        pax,
        streamingMethod
      );
    }
  };

  return (
    <div className="search-form">
      <form onSubmit={handleSubmit}>
        <div className="form-row">
          <div className="form-group">
            <AirportAutocomplete
              id="from"
              label="From:"
              value={from}
              onChange={(code, airport) => {
                setFrom(code);
                setFromAirport(airport);
              }}
              placeholder="Search departure airport..."
              disabled={isSearching}
              required
            />
          </div>
          <div className="form-group">
            <AirportAutocomplete
              id="to"
              label="To:"
              value={to}
              onChange={(code, airport) => {
                setTo(code);
                setToAirport(airport);
              }}
              placeholder="Search destination airport..."
              disabled={isSearching}
              required
              origin={from || undefined}
            />
          </div>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label htmlFor="trip-type">Trip Type:</label>
            <select
              id="trip-type"
              value={tripType}
              onChange={(e) => setTripType(e.target.value as TripType)}
              disabled={isSearching}
            >
              <option value="one-way">One Way</option>
              <option value="round-trip">Round Trip</option>
            </select>
          </div>
          <div className="form-group">
            <label htmlFor="departure-date">Departure Date:</label>
            <input
              type="date"
              id="departure-date"
              value={departureDate}
              onChange={(e) => setDepartureDate(e.target.value)}
              min={today}
              disabled={isSearching}
              required
            />
          </div>
          {tripType === 'round-trip' && (
            <div className="form-group">
              <label htmlFor="return-date">Return Date:</label>
              <input
                type="date"
                id="return-date"
                value={returnDate}
                onChange={(e) => setReturnDate(e.target.value)}
                min={departureDate || today}
                disabled={isSearching}
                required
              />
            </div>
          )}
          <div className="form-group">
            <label htmlFor="pax">Passengers (PAX):</label>
            <input
              type="number"
              id="pax"
              value={pax}
              onChange={(e) => setPax(Math.max(1, parseInt(e.target.value) || 1))}
              min="1"
              max="9"
              disabled={isSearching}
              required
            />
          </div>
        </div>
        
        <div className="form-row">
          <div className="form-group">
            <label htmlFor="streaming-method">Streaming Method:</label>
            <select
              id="streaming-method"
              value={streamingMethod}
              onChange={(e) => setStreamingMethod(e.target.value as StreamingMethod)}
              disabled={isSearching}
            >
              <option value="websocket">WebSocket</option>
              <option value="longpoll">Long Polling</option>
              <option value="sse">Server-Sent Events (SSE)</option>
            </select>
            <div className="method-description">
              {streamingMethod === 'websocket' && (
                <span>Full-duplex real-time communication</span>
              )}
              {streamingMethod === 'longpoll' && (
                <span>HTTP-based polling with automatic reconnection</span>
              )}
              {streamingMethod === 'sse' && (
                <span>One-way server streaming with automatic reconnection</span>
              )}
            </div>
          </div>
        </div>

        <button type="submit" disabled={isSearching || !from.trim() || !to.trim() || !departureDate || (tripType === 'round-trip' && !returnDate)}>
          {isSearching ? 'Searching...' : 'Search Flights'}
        </button>
      </form>
    </div>
  );
};

export default FlightSearchForm; 