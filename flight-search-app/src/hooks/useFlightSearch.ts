import { useState, useEffect, useCallback, useRef } from 'react';
import { apiService } from '../services/api';
import { StreamingService } from '../services/streamingService';
import { FlightResult, StreamingMessage, SearchState, StreamingMethod, TripType } from '../types';

export const useFlightSearch = () => {
  const [state, setState] = useState<SearchState>({
    isSearching: false,
    queryId: null,
    results: [],
    error: null,
    progress: 0,
    isStreaming: false,
    receivedFlights: 0,
    totalExpected: 0,
    streamingMethod: 'websocket',
  });

  const streamingServiceRef = useRef<StreamingService | null>(null);

  // Initialize streaming service
  useEffect(() => {
    streamingServiceRef.current = new StreamingService({
      onMessage: (data: StreamingMessage) => {
        console.log('Streaming message received:', data);
        
        // Handle flight result FIRST (before progress updates)
        if (data.source && data.airline) {
          const flightResult: FlightResult = {
            source: data.source,
            airline: data.airline,
            flight_number: data.flight_number || '',
            departure_time: data.departure_time || '',
            price: data.price || 0,
            from: data.from || '',
            to: data.to || '',
            departure_date: data.departure_date,
            return_date: data.return_date,
            trip_type: data.trip_type,
            pax: data.pax,
            timestamp: data.timestamp || new Date().toISOString(),
            seat_class: data.seat_class || '',
            affiliate_link: data.affiliate_link || '',
            booking_url: data.booking_url || '',
          };

          setState(prev => ({
            ...prev,
            results: [...prev.results, flightResult],
            receivedFlights: (data.received_flights || prev.receivedFlights || 0),
          }));
        }

        // Handle progress updates (including those that come with flight data)
        if (data.progress !== undefined || data.status !== undefined) {
          const progress = data.progress || 0;
          const status = data.status || 'searching';
          const receivedFlights = data.received_flights || 0;
          const totalExpected = data.total_expected || 0;
          
          setState(prev => ({
            ...prev,
            progress: progress,
            isSearching: status === 'searching',
            isStreaming: status === 'searching',
            receivedFlights: receivedFlights,
            totalExpected: totalExpected,
          }));
          
          // If search is complete, update final state
          if (status === 'completed' || progress >= 100) {
            setState(prev => ({
              ...prev,
              isSearching: false,
              isStreaming: false,
              progress: 100,
            }));
          }
        }

        // Handle cancelled status
        if (data.type === 'cancelled' || data.status === 'cancelled') {
          setState(prev => ({
            ...prev,
            isSearching: false,
            isStreaming: false,
            progress: 100,
          }));
        }

        // Handle timeout status
        if (data.type === 'timeout' || data.status === 'timeout') {
          setState(prev => ({
            ...prev,
            isSearching: false,
            isStreaming: false,
            progress: 0,
            error: 'Search timeout - please try again',
          }));
        }
      },
      onError: (error: string) => {
        console.error('Streaming error:', error);
        setState(prev => ({
          ...prev,
          error: error,
          isSearching: false,
          isStreaming: false,
        }));
      },
      onClose: () => {
        console.log('Streaming connection closed');
        setState(prev => ({
          ...prev,
          isSearching: false,
          isStreaming: false,
          progress: 100,
        }));
      },
    });

    return () => {
      if (streamingServiceRef.current) {
        streamingServiceRef.current.disconnect();
      }
    };
  }, []);

  const searchFlights = useCallback(async (
    from: string,
    to: string,
    tripType: TripType,
    departureDate: string,
    returnDate: string | undefined,
    pax: number,
    method: StreamingMethod = 'websocket'
  ) => {
    try {
      setState(prev => ({
        ...prev,
        isSearching: true,
        results: [],
        error: null,
        progress: 0,
        receivedFlights: 0,
        totalExpected: 0,
        streamingMethod: method,
      }));

      const response = await apiService.searchFlights({
        from,
        to,
        tripType,
        departureDate,
        returnDate,
        pax,
      });
      
      setState(prev => ({
        ...prev,
        queryId: response.query_id,
      }));

      // Start streaming with specified method
      if (streamingServiceRef.current) {
        streamingServiceRef.current.startStreaming(method, response.query_id);
      }

    } catch (error) {
      console.error('Search error:', error);
      setState(prev => ({
        ...prev,
        error: 'Failed to start search',
        isSearching: false,
        progress: 0,
      }));
    }
  }, []);

  const cancelSearch = useCallback(async () => {
    if (!state.queryId) return;

    try {
      await apiService.cancelSearch(state.queryId);
      
      if (streamingServiceRef.current) {
        streamingServiceRef.current.disconnect();
      }

      setState(prev => ({
        ...prev,
        isSearching: false,
        isStreaming: false,
        progress: 100,
      }));
    } catch (error) {
      console.error('Cancel error:', error);
      setState(prev => ({
        ...prev,
        error: 'Failed to cancel search',
      }));
    }
  }, [state.queryId]);

  const resetSearch = useCallback(() => {
    if (streamingServiceRef.current) {
      streamingServiceRef.current.disconnect();
    }
    
    setState({
      isSearching: false,
      queryId: null,
      results: [],
      error: null,
      progress: 0,
      isStreaming: false,
      receivedFlights: 0,
      totalExpected: 0,
      streamingMethod: 'websocket',
    });
  }, []);

  const getCurrentStreamingMethod = useCallback(() => {
    return streamingServiceRef.current?.getCurrentMethod() || 'websocket';
  }, []);

  return {
    ...state,
    searchFlights,
    cancelSearch,
    resetSearch,
    getCurrentStreamingMethod,
  };
}; 