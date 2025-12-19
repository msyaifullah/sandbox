export type TripType = 'one-way' | 'round-trip';

export interface FlightSearchRequest {
  from: string;
  to: string;
  tripType: TripType;
  departureDate: string;
  returnDate?: string;
  pax: number;
}

export interface FlightSearchResponse {
  query_id: string;
  ws_url: string;
}

export interface FlightResult {
  source: string;
  airline: string;
  flight_number: string;
  departure_time: string;
  price: number;
  from: string;
  to: string;
  departure_date?: string;
  return_date?: string;
  trip_type?: string;
  pax?: number;
  timestamp: string;
  seat_class: string;
  affiliate_link: string;
  booking_url: string;
}

export interface CancelResponse {
  status: string;
}

export interface SearchState {
  isSearching: boolean;
  queryId: string | null;
  results: FlightResult[];
  error: string | null;
  progress: number;
  isStreaming: boolean;
  receivedFlights: number;
  totalExpected: number;
  streamingMethod: 'websocket' | 'longpoll' | 'sse';
}

export interface WebSocketMessage {
  type?: string;
  message?: string;
  progress?: number;
  status?: string;
  source?: string;
  airline?: string;
  flight_number?: string;
  departure_time?: string;
  price?: number;
  from?: string;
  to?: string;
  departure_date?: string;
  return_date?: string;
  trip_type?: string;
  pax?: number;
  timestamp?: string;
  seat_class?: string;
  affiliate_link?: string;
  booking_url?: string;
  received_flights?: number;
  total_expected?: number;
}

export interface StreamingMessage {
  type?: string;
  message?: string;
  progress?: number;
  status?: string;
  source?: string;
  airline?: string;
  flight_number?: string;
  departure_time?: string;
  price?: number;
  from?: string;
  to?: string;
  departure_date?: string;
  return_date?: string;
  trip_type?: string;
  pax?: number;
  timestamp?: string;
  seat_class?: string;
  affiliate_link?: string;
  booking_url?: string;
  received_flights?: number;
  total_expected?: number;
  last_seen_index?: number;
}

export type StreamingMethod = 'websocket' | 'longpoll' | 'sse';

export interface Airport {
  code: string;
  name: string;
  city: string;
  country: string;
  country_code: string;
  is_major: boolean;
  region: string;
}

export interface AirportSearchResponse {
  total: number;
  airports?: Airport[];
  destinations?: Airport[];
  origin?: string;
  keyword?: string;
  message?: string;
} 