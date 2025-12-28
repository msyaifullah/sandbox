package com.kjl.servicejava.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

@Component
public class FlightWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @Autowired
    private ObjectMapper objectMapper;

    private static final int EXPECTED_FLIGHTS = 24;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String queryId = extractQueryId(session.getUri());
        if (queryId == null) {
            session.close(CloseStatus.BAD_DATA.withReason("Missing query_id parameter"));
            return;
        }

        int[] receivedFlights = {0};

        MessageListener messageListener = new MessageListener() {
            @Override
            public void onMessage(Message message, byte[] pattern) {
                try {
                    String payload = new String(message.getBody());
                    Map<String, Object> flightData = objectMapper.readValue(payload, Map.class);

                    // Check if this is a cancellation message
                    if ("cancelled".equals(flightData.get("type"))) {
                        Map<String, Object> cancelMsg = Map.of(
                            "type", "cancelled",
                            "progress", 0,
                            "status", "cancelled",
                            "message", "Search was cancelled"
                        );
                        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(cancelMsg)));
                        return;
                    }

                    receivedFlights[0]++;

                    // Calculate progress
                    int progress = (int) ((double) receivedFlights[0] / EXPECTED_FLIGHTS * 100);
                    if (progress > 100) {
                        progress = 100;
                    }

                    flightData.put("progress", progress);
                    flightData.put("received_flights", receivedFlights[0]);
                    flightData.put("total_expected", EXPECTED_FLIGHTS);
                    flightData.put("status", "searching");

                    // Check if we've received enough flights
                    if (receivedFlights[0] >= EXPECTED_FLIGHTS) {
                        flightData.put("status", "completed");
                        flightData.put("progress", 99);
                        flightData.put("message", "Search completed");
                    }

                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(flightData)));

                    // If search is completed, send final completion message
                    if ("completed".equals(flightData.get("status")) || "completed".equals(flightData.get("type"))) {
                        Map<String, Object> completionMsg = Map.of(
                            "type", "completed",
                            "progress", 100,
                            "status", "completed",
                            "message", "All flights found",
                            "total_flights", receivedFlights[0]
                        );
                        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(completionMsg)));
                    }
                } catch (IOException e) {
                    // Handle error
                }
            }
        };

        ChannelTopic topic = new ChannelTopic("flight:" + queryId);
        redisMessageListenerContainer.addMessageListener(messageListener, topic);

        // Store listener in session attributes for cleanup
        session.getAttributes().put("messageListener", messageListener);
        session.getAttributes().put("topic", topic);

        // Send initial progress
        Map<String, Object> initialProgress = Map.of(
            "type", "progress",
            "progress", 0,
            "status", "searching",
            "message", "Starting flight search..."
        );
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(initialProgress)));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        MessageListener listener = (MessageListener) session.getAttributes().get("messageListener");
        ChannelTopic topic = (ChannelTopic) session.getAttributes().get("topic");
        if (listener != null && topic != null) {
            redisMessageListenerContainer.removeMessageListener(listener);
        }
    }

    private String extractQueryId(URI uri) {
        if (uri == null || uri.getQuery() == null) {
            return null;
        }
        String query = uri.getQuery();
        String[] params = query.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && "query_id".equals(keyValue[0])) {
                return keyValue[1];
            }
        }
        return null;
    }
}
