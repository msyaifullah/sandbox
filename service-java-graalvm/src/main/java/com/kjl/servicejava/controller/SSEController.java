package com.kjl.servicejava.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/result")
public class SSEController {

    @Autowired
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @Autowired
    private ObjectMapper objectMapper;

    private static final int EXPECTED_FLIGHTS = 24;

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sse(@RequestParam String query_id) {
        SseEmitter emitter = new SseEmitter(300000L); // 5 minute timeout

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
                        emitter.send(SseEmitter.event().name("cancelled").data(objectMapper.writeValueAsString(cancelMsg)));
                        emitter.complete();
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

                    emitter.send(SseEmitter.event().name("flight").data(objectMapper.writeValueAsString(flightData)));

                    // If search is completed, send final completion message
                    if ("completed".equals(flightData.get("status")) || "completed".equals(flightData.get("type"))) {
                        Map<String, Object> completionMsg = Map.of(
                            "type", "completed",
                            "progress", 100,
                            "status", "completed",
                            "message", "All flights found",
                            "total_flights", receivedFlights[0]
                        );
                        emitter.send(SseEmitter.event().name("completed").data(objectMapper.writeValueAsString(completionMsg)));
                        emitter.complete();
                    }
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }
        };

        ChannelTopic topic = new ChannelTopic("flight:" + query_id);
        redisMessageListenerContainer.addMessageListener(messageListener, topic);

        // Send initial progress
        try {
            Map<String, Object> initialProgress = Map.of(
                "type", "progress",
                "progress", 0,
                "status", "searching",
                "message", "Starting flight search..."
            );
            emitter.send(SseEmitter.event().name("progress").data(objectMapper.writeValueAsString(initialProgress)));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        // Cleanup on completion
        emitter.onCompletion(() -> {
            redisMessageListenerContainer.removeMessageListener(messageListener);
        });

        emitter.onTimeout(() -> {
            redisMessageListenerContainer.removeMessageListener(messageListener);
            emitter.complete();
        });

        emitter.onError((ex) -> {
            redisMessageListenerContainer.removeMessageListener(messageListener);
        });

        return emitter;
    }
}
