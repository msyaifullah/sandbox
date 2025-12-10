package com.kjl.servicejava.controller;

import com.kjl.servicejava.service.FlightSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FlightSearchController {

    @Autowired
    private FlightSearchService flightSearchService;

    @GetMapping("/search")
    public ResponseEntity<Map<String, String>> search(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam String trip_type,
            @RequestParam String departure_date,
            @RequestParam(required = false) String return_date,
            @RequestParam(defaultValue = "1") int pax) {
        String queryId = flightSearchService.generateQueryId(from, to, trip_type, departure_date, return_date, pax);
        flightSearchService.startSearch(queryId, from, to, trip_type, departure_date, return_date, pax);

        Map<String, String> response = new HashMap<>();
        response.put("query_id", queryId);
        response.put("ws_url", String.format("ws://localhost:3001/ws/result/stream?query_id=%s", queryId));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search/cancel")
    public ResponseEntity<Map<String, String>> cancel(@RequestParam String query_id) {
        flightSearchService.cancelSearch(query_id);
        Map<String, String> response = new HashMap<>();
        response.put("status", "cancelled");
        return ResponseEntity.ok(response);
    }
}
