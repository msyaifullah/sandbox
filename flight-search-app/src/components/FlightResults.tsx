import React from 'react';
import { FlightResult } from '../types';

interface FlightResultsProps {
  results: FlightResult[];
  onCancel: () => void;
  isStreaming: boolean;
}

const FlightResults: React.FC<FlightResultsProps> = ({ results, onCancel, isStreaming }) => {
  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('id-ID', {
      style: 'currency',
      currency: 'IDR',
      minimumFractionDigits: 0,
    }).format(price);
  };

  const formatTime = (time: string) => {
    return time;
  };

  return (
    <div className="flight-results">
      <div className="results-header">
        <h2>Flight Results ({results.length} found)</h2>
        {isStreaming && (
          <button onClick={onCancel} className="cancel-button">
            Cancel Search
          </button>
        )}
      </div>
      
      {results.length === 0 ? (
        <div className="no-results">
          <p>No flights found yet. Please wait for results...</p>
        </div>
      ) : (
        <div className="results-grid">
          {results.map((flight, index) => (
            <div key={`${flight.source}-${flight.flight_number}-${index}`} className="flight-card">
              <div className="flight-header">
                <div className="airline-info">
                  <h3>{flight.airline}</h3>
                  <span className="flight-number">{flight.flight_number}</span>
                </div>
                <div className="source-badge">{flight.source}</div>
              </div>
              
              <div className="flight-details">
                <div className="route">
                  <div className="departure">
                    <span className="time">{formatTime(flight.departure_time)}</span>
                    <span className="city">{flight.from}</span>
                  </div>
                  <div className="flight-line">
                    <div className="line"></div>
                    <div className="plane-icon">✈</div>
                  </div>
                  <div className="arrival">
                    <span className="city">{flight.to}</span>
                  </div>
                </div>
                
                <div className="flight-info">
                  <div className="seat-class">
                    <span className="label">Class:</span>
                    <span className="value">{flight.seat_class}</span>
                  </div>
                  <div className="timestamp">
                    <span className="label">Found:</span>
                    <span className="value">{new Date(flight.timestamp).toLocaleTimeString()}</span>
                  </div>
                </div>
              </div>
              
              <div className="flight-footer">
                <div className="price">
                  <span className="amount">{formatPrice(flight.price)}</span>
                </div>
                <div className="actions">
                  <a 
                    href={flight.affiliate_link} 
                    target="_blank" 
                    rel="noopener noreferrer"
                    className="book-button"
                  >
                    Book Now
                  </a>
                  <a 
                    href={flight.booking_url} 
                    target="_blank" 
                    rel="noopener noreferrer"
                    className="details-button"
                  >
                    Details
                  </a>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default FlightResults; 