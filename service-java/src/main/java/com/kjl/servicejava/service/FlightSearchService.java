package com.kjl.servicejava.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kjl.servicejava.repository.RedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for flight search operations using Repository pattern for data access.
 */
@Service
public class FlightSearchService {
    private static final int EXPECTED_FLIGHTS = 24;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Map<String, CompletableFuture<Void>> activeSearches = new ConcurrentHashMap<>();
    private final RedisRepository redisRepository;
    private final PubsubListenerService pubsubListenerService;

    @Autowired
    public FlightSearchService(RedisRepository redisRepository, PubsubListenerService pubsubListenerService) {
        this.redisRepository = redisRepository;
        this.pubsubListenerService = pubsubListenerService;
    }

    private final String[] sources = {"kiwi", "trip", "12go"};
    private final String[] departureTimes = {"06:00", "08:30", "10:15", "12:00", "14:30", "16:45", "18:20", "20:00", "22:30"};

    private final Map<String, List<String>> airlines = new HashMap<>() {{
        put("kiwi", Arrays.asList("Lion Air", "Garuda", "AirAsia", "Batik Air", "Citilink"));
        put("trip", Arrays.asList("Singapore Airlines", "Malaysia Airlines", "Thai Airways", "Vietnam Airlines", "Philippine Airlines"));
        put("12go", Arrays.asList("Cebu Pacific", "Jetstar", "Tiger Air", "Scoot", "AirAsia"));
    }};

    private final List<Map<String, Object>> commonFlights = Arrays.asList(
        new HashMap<>() {{ put("airline", "AirAsia"); put("flight_number", "AK123"); put("departure_time", "10:15"); put("base_price", 750000); }},
        new HashMap<>() {{ put("airline", "Garuda"); put("flight_number", "GA456"); put("departure_time", "14:30"); put("base_price", 1200000); }},
        new HashMap<>() {{ put("airline", "Lion Air"); put("flight_number", "JT789"); put("departure_time", "08:30"); put("base_price", 650000); }}
    );

    public String generateQueryId(String from, String to, String tripType, String departureDate, String returnDate, int pax) {
        try {
            String input = from + ":" + to + ":" + tripType + ":" + departureDate + ":" + 
                          (returnDate != null ? returnDate : "") + ":" + pax + ":" + System.currentTimeMillis();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString().replace("-", "");
        }
    }

    public void startSearch(String queryId, String from, String to, String tripType, String departureDate, String returnDate, int pax) {
        // Start background listener that writes pubsub messages to Redis
        pubsubListenerService.startListener(queryId);
        
        CompletableFuture<Void> searchFuture = CompletableFuture.runAsync(() -> {
            runAggregator(queryId, from, to, tripType, departureDate, returnDate, pax);
        }, executorService);

        activeSearches.put(queryId, searchFuture);
        searchFuture.whenComplete((result, throwable) -> {
            activeSearches.remove(queryId);
        });
    }

    public void cancelSearch(String queryId) {
        CompletableFuture<Void> future = activeSearches.remove(queryId);
        if (future != null) {
            future.cancel(true);
        }
        
        // Stop pubsub listener
        pubsubListenerService.stopListener(queryId);
        
        // Clean up Redis data
        redisRepository.delete("search_result:" + queryId);
        redisRepository.deleteFlightResults(queryId);
        
        try {
            Map<String, Object> cancelMsg = new HashMap<>();
            cancelMsg.put("type", "cancelled");
            
            // Publish cancellation (for WebSocket/SSE)
            redisRepository.publish("flight:" + queryId, objectMapper.writeValueAsString(cancelMsg));
            
            // Write cancellation to Redis for long polling
            redisRepository.pushFlightResult(queryId, objectMapper.writeValueAsString(cancelMsg));
        } catch (Exception e) {
            // Ignore
        }
    }

    private void runAggregator(String queryId, String from, String to, String tripType, String departureDate, String returnDate, int pax) {
        Random random = new Random();
        int expectedTotalFlights = sources.length * (8 + random.nextInt(3)); // 8-10 flights per source
        int[] totalFlightsSent = {0};

        List<CompletableFuture<Void>> sourceFutures = new ArrayList<>();

        for (String source : sources) {
            CompletableFuture<Void> sourceFuture = CompletableFuture.runAsync(() -> {
                int numFlights = 8 + random.nextInt(3); // Random between 8-10 flights

                for (int i = 0; i < numFlights; i++) {
                    try {
                        // Random delay between 10-710 milliseconds
                        Thread.sleep(10 + random.nextInt(700));

                        Map<String, Object> result;

                        // 30% chance to offer a common flight
                        if (random.nextDouble() < 0.3 && i < commonFlights.size()) {
                            Map<String, Object> commonFlight = commonFlights.get(i);
                            int priceVariation = random.nextInt(100000) - 50000; // ±50k variation
                            int price = ((Number) commonFlight.get("base_price")).intValue() + priceVariation;
                            if (price < 500000) {
                                price = 500000;
                            }

                            // Adjust price based on PAX
                            int totalPrice = price * pax;
                            
                            result = new HashMap<>();
                            result.put("source", source);
                            result.put("airline", commonFlight.get("airline"));
                            result.put("flight_number", commonFlight.get("flight_number"));
                            result.put("departure_time", commonFlight.get("departure_time"));
                            result.put("price", totalPrice);
                            result.put("from", from);
                            result.put("to", to);
                            result.put("departure_date", departureDate);
                            result.put("return_date", returnDate);
                            result.put("trip_type", tripType);
                            result.put("pax", pax);
                            result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                            result.put("seat_class", Arrays.asList("Economy", "Business", "Premium Economy").get(random.nextInt(3)));
                            result.put("affiliate_link", String.format("https://%s.com/affiliate?source=flight_search&from=%s&to=%s&flight=%s&price=%d&pax=%d&ref=YOUR_AFFILIATE_ID",
                                source, from, to, commonFlight.get("flight_number"), totalPrice, pax));
                            result.put("booking_url", String.format("https://%s.com/flights/%s-%s/%s?departure_date=%s&pax=%d",
                                source, from, to, commonFlight.get("flight_number"), departureDate, pax));
                            result.put("is_common", true);
                        } else {
                            String departureTime = departureTimes[random.nextInt(departureTimes.length)];
                            String airline = airlines.get(source).get(random.nextInt(airlines.get(source).size()));
                            int basePrice = 500000 + random.nextInt(2000000);
                            String flightNumber = String.format("%s%d", source.substring(0, 2).toUpperCase(), 100 + random.nextInt(900));

                            // Adjust price based on PAX
                            int totalPrice = basePrice * pax;
                            
                            result = new HashMap<>();
                            result.put("source", source);
                            result.put("airline", airline);
                            result.put("flight_number", flightNumber);
                            result.put("departure_time", departureTime);
                            result.put("price", totalPrice);
                            result.put("from", from);
                            result.put("to", to);
                            result.put("departure_date", departureDate);
                            result.put("return_date", returnDate);
                            result.put("trip_type", tripType);
                            result.put("pax", pax);
                            result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                            result.put("seat_class", Arrays.asList("Economy", "Business", "Premium Economy").get(random.nextInt(3)));
                            result.put("affiliate_link", String.format("https://%s.com/affiliate?source=flight_search&from=%s&to=%s&flight=%s&price=%d&pax=%d&ref=YOUR_AFFILIATE_ID",
                                source, from, to, flightNumber, totalPrice, pax));
                            result.put("booking_url", String.format("https://%s.com/flights/%s-%s/%s?departure_date=%s&pax=%d",
                                source, from, to, flightNumber, departureDate, pax));
                            result.put("is_common", false);
                        }

                        String resultJson = objectMapper.writeValueAsString(result);
                        redisRepository.publish("flight:" + queryId, resultJson);
                        redisRepository.setSearchResult(queryId, source, i, resultJson);

                        synchronized (totalFlightsSent) {
                            totalFlightsSent[0]++;
                            int currentTotal = totalFlightsSent[0];

                            if (currentTotal >= expectedTotalFlights) {
                                Map<String, Object> completionMsg = new HashMap<>();
                                completionMsg.put("type", "completed");
                                completionMsg.put("progress", 100);
                                completionMsg.put("status", "completed");
                                completionMsg.put("message", "All flights found");
                                completionMsg.put("total_flights", currentTotal);
                                redisRepository.publish("flight:" + queryId, objectMapper.writeValueAsString(completionMsg));
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    } catch (Exception e) {
                        // Continue on error
                    }
                }
            }, executorService);

            sourceFutures.add(sourceFuture);
        }

        // Wait for all sources to complete
        CompletableFuture.allOf(sourceFutures.toArray(new CompletableFuture[0])).join();
    }
}
