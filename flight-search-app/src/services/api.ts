import axios from 'axios';
import { FlightSearchRequest, FlightSearchResponse, CancelResponse, StreamingMethod } from '../types';

const API_BASE_URL = 'http://localhost:3001';

export const apiService = {
  // Search for flights
  async searchFlights(request: FlightSearchRequest): Promise<FlightSearchResponse> {
    const params: any = {
      from: request.from,
      to: request.to,
      trip_type: request.tripType,
      departure_date: request.departureDate,
      pax: request.pax,
    };
    
    if (request.returnDate) {
      params.return_date = request.returnDate;
    }
    
    const response = await axios.get(`${API_BASE_URL}/api/search`, {
      params,
    });
    return response.data;
  },

  // Cancel search
  async cancelSearch(queryId: string): Promise<CancelResponse> {
    const response = await axios.post(`${API_BASE_URL}/api/search/cancel`, null, {
      params: {
        query_id: queryId,
      },
    });
    return response.data;
  },

  // Get WebSocket URL for streaming
  getWebSocketUrl(queryId: string): string {
    return `ws://localhost:3001/ws/result/stream?query_id=${queryId}`;
  },

  // Get Long Polling URL
  getLongPollUrl(queryId: string): string {
    return `${API_BASE_URL}/api/result/longpoll?query_id=${queryId}`;
  },

  // Get SSE URL
  getSSEUrl(queryId: string): string {
    return `${API_BASE_URL}/api/result/sse?query_id=${queryId}`;
  },

  // Long Polling request
  async longPoll(queryId: string, lastSeenIndex: number = 0, signal?: AbortSignal): Promise<any> {
    const response = await axios.get(this.getLongPollUrl(queryId), {
      params: {
        last_seen_index: lastSeenIndex,
      },
      signal,
    });
    return response.data;
  },
}; 