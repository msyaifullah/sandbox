import { apiService } from './api';
import { StreamingMessage, StreamingMethod, FlightResult } from '../types';

export interface StreamingCallbacks {
  onMessage: (data: StreamingMessage) => void;
  onError: (error: string) => void;
  onClose: () => void;
}

export class StreamingService {
  private ws: WebSocket | null = null;
  private eventSource: EventSource | null = null;
  private longPollActive = false;
  private currentMethod: StreamingMethod = 'websocket';
  private lastSeenIndex: number = 0;

  constructor(private callbacks: StreamingCallbacks) {}

  // WebSocket Streaming
  connectWebSocket(queryId: string) {
    this.disconnect();
    this.currentMethod = 'websocket';

    const wsUrl = apiService.getWebSocketUrl(queryId);
    this.ws = new WebSocket(wsUrl);

    this.ws.onopen = () => {
      console.log('WebSocket connected');
    };

    this.ws.onmessage = (event) => {
      try {
        const data: StreamingMessage = JSON.parse(event.data);
        this.callbacks.onMessage(data);
      } catch (error) {
        console.error('Error parsing WebSocket message:', error);
        this.callbacks.onError('Failed to parse message');
      }
    };

    this.ws.onclose = () => {
      console.log('WebSocket disconnected');
      this.callbacks.onClose();
    };

    this.ws.onerror = (error) => {
      console.error('WebSocket error:', error);
      this.callbacks.onError('WebSocket connection failed');
    };
  }

  // Long Polling Streaming
  async startLongPolling(queryId: string) {
    this.disconnect();
    this.currentMethod = 'longpoll';
    this.longPollActive = true;
    this.lastSeenIndex = 0; // Reset when starting new search

    let retryCount = 0;
    const maxRetries = 5;

    const poll = async () => {
      if (!this.longPollActive) return;

      try {
        // Set a timeout for individual requests (4 minutes to be safe)
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 4 * 60 * 1000);

        const data = await apiService.longPoll(queryId, this.lastSeenIndex, controller.signal);
        clearTimeout(timeoutId);
        
        // Reset retry count on successful request
        retryCount = 0;
        
        // Update last_seen_index if provided in response
        if (data.last_seen_index !== undefined) {
          this.lastSeenIndex = data.last_seen_index;
        }
        
        this.callbacks.onMessage(data);

        // Continue polling unless search is complete
        if (data.type === 'completed' || data.type === 'cancelled' || data.type === 'timeout') {
          this.longPollActive = false;
          this.callbacks.onClose();
          return;
        }

        // Continue polling after a longer delay (1 second instead of 100ms)
        setTimeout(() => poll(), 1000);
      } catch (error) {
        console.error('Long polling error:', error);
        retryCount++;
        
        if (this.longPollActive && retryCount <= maxRetries) {
          // Exponential backoff: 1s, 2s, 4s, 8s, 16s
          const delay = Math.min(1000 * Math.pow(2, retryCount - 1), 16000);
          console.log(`Retrying long poll in ${delay}ms (attempt ${retryCount}/${maxRetries})`);
          setTimeout(() => poll(), delay);
        } else if (retryCount > maxRetries) {
          console.error('Max retries exceeded for long polling');
          this.callbacks.onError('Connection failed after multiple retries');
          this.longPollActive = false;
          this.callbacks.onClose();
        }
      }
    };

    poll();
  }

  // Server-Sent Events Streaming
  connectSSE(queryId: string) {
    this.disconnect();
    this.currentMethod = 'sse';

    const sseUrl = apiService.getSSEUrl(queryId);
    this.eventSource = new EventSource(sseUrl);

    this.eventSource.onopen = () => {
      console.log('SSE connected');
    };

    this.eventSource.onmessage = (event) => {
      try {
        const data: StreamingMessage = JSON.parse(event.data);
        this.callbacks.onMessage(data);
      } catch (error) {
        console.error('Error parsing SSE message:', error);
        this.callbacks.onError('Failed to parse message');
      }
    };

    this.eventSource.addEventListener('progress', (event) => {
      try {
        const data: StreamingMessage = JSON.parse(event.data);
        this.callbacks.onMessage(data);
      } catch (error) {
        console.error('Error parsing SSE progress message:', error);
      }
    });

    this.eventSource.addEventListener('flight', (event) => {
      try {
        const data: StreamingMessage = JSON.parse(event.data);
        this.callbacks.onMessage(data);
      } catch (error) {
        console.error('Error parsing SSE flight message:', error);
      }
    });

    this.eventSource.addEventListener('completed', (event) => {
      try {
        const data: StreamingMessage = JSON.parse(event.data);
        this.callbacks.onMessage(data);
        this.eventSource?.close();
      } catch (error) {
        console.error('Error parsing SSE completed message:', error);
      }
    });

    this.eventSource.addEventListener('cancelled', (event) => {
      try {
        const data: StreamingMessage = JSON.parse(event.data);
        this.callbacks.onMessage(data);
        this.eventSource?.close();
      } catch (error) {
        console.error('Error parsing SSE cancelled message:', error);
      }
    });

    this.eventSource.addEventListener('timeout', (event) => {
      try {
        const data: StreamingMessage = JSON.parse(event.data);
        this.callbacks.onMessage(data);
        this.eventSource?.close();
      } catch (error) {
        console.error('Error parsing SSE timeout message:', error);
      }
    });

    this.eventSource.onerror = (error) => {
      console.error('SSE error:', error);
      this.callbacks.onError('SSE connection failed');
    };
  }

  // Disconnect all streaming connections
  disconnect() {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }

    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
    }

    this.longPollActive = false;
    this.lastSeenIndex = 0;
  }

  // Get current streaming method
  getCurrentMethod(): StreamingMethod {
    return this.currentMethod;
  }

  // Start streaming with specified method
  startStreaming(method: StreamingMethod, queryId: string) {
    switch (method) {
      case 'websocket':
        this.connectWebSocket(queryId);
        break;
      case 'longpoll':
        this.startLongPolling(queryId);
        break;
      case 'sse':
        this.connectSSE(queryId);
        break;
      default:
        throw new Error(`Unknown streaming method: ${method}`);
    }
  }
} 