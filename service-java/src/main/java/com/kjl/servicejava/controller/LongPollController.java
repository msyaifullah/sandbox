package com.kjl.servicejava.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kjl.servicejava.repository.RedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/result")
public class LongPollController {

    @Autowired
    private RedisRepository redisRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final int EXPECTED_FLIGHTS = 24;

    @GetMapping("/longpoll")
    public ResponseEntity<Map<String, Object>> longPoll(
            @RequestParam String query_id,
            @RequestParam(required = false, defaultValue = "0") int last_seen_index) {
        
        // Get total count of flights from Redis
        int totalCount = redisRepository.getFlightCount(query_id);

        // Check if there are new results
        if (last_seen_index < totalCount) {
            // Get new results from Redis list (from last_seen_index to end)
            List<String> results = redisRepository.getFlightResults(query_id, last_seen_index, -1);
            
            if (results != null && !results.isEmpty()) {
                try {
                    String firstResult = results.get(0);
                    Map<String, Object> flightData = objectMapper.readValue(firstResult, Map.class);

                    // Check if this is a cancellation message
                    if ("cancelled".equals(flightData.get("type"))) {
                        Map<String, Object> cancelMsg = new HashMap<>();
                        cancelMsg.put("type", "cancelled");
                        cancelMsg.put("progress", 0);
                        cancelMsg.put("status", "cancelled");
                        cancelMsg.put("message", "Search was cancelled");
                        cancelMsg.put("last_seen_index", last_seen_index + 1);
                        return ResponseEntity.ok(cancelMsg);
                    }

                    // Check if this is a completion message
                    if ("completed".equals(flightData.get("type"))) {
                        Map<String, Object> completionMsg = new HashMap<>();
                        completionMsg.put("type", "completed");
                        completionMsg.put("progress", 100);
                        completionMsg.put("status", "completed");
                        completionMsg.put("message", "All flights found");
                        completionMsg.put("total_flights", flightData.get("total_flights"));
                        completionMsg.put("last_seen_index", last_seen_index + 1);
                        return ResponseEntity.ok(completionMsg);
                    }

                    // Calculate progress
                    int progress = (int) ((double) totalCount / EXPECTED_FLIGHTS * 100);
                    if (progress > 100) {
                        progress = 100;
                    }

                    // Add progress info to flight data
                    flightData.put("progress", progress);
                    flightData.put("received_flights", totalCount);
                    flightData.put("total_expected", EXPECTED_FLIGHTS);
                    flightData.put("status", "searching");
                    flightData.put("last_seen_index", last_seen_index + 1);

                    // Check if search is complete
                    if (totalCount >= EXPECTED_FLIGHTS) {
                        flightData.put("status", "completed");
                        flightData.put("progress", 100);
                    }

                    return ResponseEntity.ok(flightData);
                } catch (Exception e) {
                    Map<String, Object> errorMsg = new HashMap<>();
                    errorMsg.put("error", "Failed to parse flight data");
                    errorMsg.put("type", "error");
                    return ResponseEntity.status(500).body(errorMsg);
                }
            }
        }

        // No new results yet - wait with timeout
        long timeoutMillis = 5 * 60 * 1000; // 5 minutes
        long checkInterval = 500; // Check every 500ms
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            try {
                Thread.sleep(checkInterval);
                
                // Check again for new results
                int currentCount = redisRepository.getFlightCount(query_id);
                if (currentCount > last_seen_index) {
                    // New results available, get them
                    List<String> results = redisRepository.getFlightResults(query_id, last_seen_index, -1);
                    if (results != null && !results.isEmpty()) {
                        try {
                            String firstResult = results.get(0);
                            Map<String, Object> flightData = objectMapper.readValue(firstResult, Map.class);
                            
                            int progress = (int) ((double) currentCount / EXPECTED_FLIGHTS * 100);
                            if (progress > 100) {
                                progress = 100;
                            }

                            flightData.put("progress", progress);
                            flightData.put("received_flights", currentCount);
                            flightData.put("total_expected", EXPECTED_FLIGHTS);
                            flightData.put("status", "searching");
                            flightData.put("last_seen_index", last_seen_index + 1);

                            if (currentCount >= EXPECTED_FLIGHTS) {
                                flightData.put("status", "completed");
                                flightData.put("progress", 100);
                            }

                            return ResponseEntity.ok(flightData);
                        } catch (Exception e) {
                            // Continue waiting
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // Timeout reached - send current progress and indicate client should continue polling
        int progress = (int) ((double) totalCount / EXPECTED_FLIGHTS * 100);
        Map<String, Object> timeoutMsg = new HashMap<>();
        timeoutMsg.put("type", "timeout");
        timeoutMsg.put("progress", progress);
        timeoutMsg.put("status", "searching");
        timeoutMsg.put("message", "Search still in progress. Please continue polling.");
        timeoutMsg.put("received_flights", totalCount);
        timeoutMsg.put("total_expected", EXPECTED_FLIGHTS);
        timeoutMsg.put("should_continue", true);
        timeoutMsg.put("timeout_seconds", 300);
        timeoutMsg.put("last_seen_index", last_seen_index);
        return ResponseEntity.ok(timeoutMsg);
    }
}
