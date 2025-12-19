import React, { useState, useEffect, useRef } from 'react';
import { Airport } from '../types';

interface AirportAutocompleteProps {
  id: string;
  label: string;
  value: string;
  onChange: (airportCode: string, airport: Airport | null) => void;
  placeholder?: string;
  disabled?: boolean;
  required?: boolean;
  origin?: string; // If provided, will fetch destinations based on origin
}

const API_BASE_URL = 'http://localhost:3001';

const AirportAutocomplete: React.FC<AirportAutocompleteProps> = ({
  id,
  label,
  value,
  onChange,
  placeholder = 'Search airport...',
  disabled = false,
  required = false,
  origin,
}) => {
  const [inputValue, setInputValue] = useState('');
  const [airports, setAirports] = useState<Airport[]>([]);
  const [isOpen, setIsOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [selectedAirport, setSelectedAirport] = useState<Airport | null>(null);
  const wrapperRef = useRef<HTMLDivElement>(null);
  const debounceTimer = useRef<NodeJS.Timeout | null>(null);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (wrapperRef.current && !wrapperRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Fetch airports based on input
  const fetchAirports = async (keyword: string) => {
    setIsLoading(true);
    try {
      let url = `${API_BASE_URL}/api/airports`;
      const params = new URLSearchParams();

      if (origin) {
        params.append('origin', origin);
      }

      if (keyword) {
        params.append('key', keyword);
      }

      if (params.toString()) {
        url += `?${params.toString()}`;
      }

      const response = await fetch(url);
      const data = await response.json();

      // Handle both 'airports' (for all airports) and 'destinations' (for origin-based)
      const airportList = data.airports || data.destinations || [];
      setAirports(airportList);
    } catch (error) {
      console.error('Error fetching airports:', error);
      setAirports([]);
    } finally {
      setIsLoading(false);
    }
  };

  // Handle input change with debounce
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newValue = e.target.value;
    setInputValue(newValue);
    setIsOpen(true);

    // Clear previous selection if user is typing
    if (selectedAirport) {
      setSelectedAirport(null);
      onChange('', null);
    }

    // Debounce API calls
    if (debounceTimer.current) {
      clearTimeout(debounceTimer.current);
    }

    debounceTimer.current = setTimeout(() => {
      fetchAirports(newValue);
    }, 300);
  };

  // Handle airport selection
  const handleSelect = (airport: Airport) => {
    setSelectedAirport(airport);
    setInputValue(`${airport.code} - ${airport.city}`);
    onChange(airport.code, airport);
    setIsOpen(false);
  };

  // Load initial airports when component mounts or origin changes
  useEffect(() => {
    fetchAirports('');
  }, [origin]);

  // Update input value when external value changes
  useEffect(() => {
    if (!value && !selectedAirport) {
      setInputValue('');
    }
  }, [value, selectedAirport]);

  return (
    <div className="airport-autocomplete" ref={wrapperRef}>
      <label htmlFor={id}>{label}</label>
      <div className="autocomplete-input-wrapper">
        <input
          type="text"
          id={id}
          value={inputValue}
          onChange={handleInputChange}
          onFocus={() => {
            setIsOpen(true);
            if (airports.length === 0) {
              fetchAirports(inputValue);
            }
          }}
          placeholder={placeholder}
          disabled={disabled}
          required={required}
          autoComplete="off"
        />
        {isLoading && <span className="loading-indicator">Loading...</span>}
      </div>

      {isOpen && airports.length > 0 && (
        <ul className="autocomplete-dropdown">
          {airports.map((airport) => (
            <li
              key={airport.code}
              onClick={() => handleSelect(airport)}
              className="autocomplete-item"
            >
              <div className="airport-code">{airport.code}</div>
              <div className="airport-info">
                <div className="airport-city">{airport.city}</div>
                <div className="airport-name">{airport.name}</div>
                <div className="airport-country">{airport.country}</div>
              </div>
            </li>
          ))}
        </ul>
      )}

      {isOpen && !isLoading && airports.length === 0 && inputValue && (
        <div className="autocomplete-no-results">
          No airports found
        </div>
      )}
    </div>
  );
};

export default AirportAutocomplete;
