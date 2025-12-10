package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.HashMap;
import java.util.Map;

@Data
public class FlightResult {
    @JsonProperty("source")
    private String source;

    @JsonProperty("airline")
    private String airline;

    @JsonProperty("flight_number")
    private String flightNumber;

    @JsonProperty("departure_time")
    private String departureTime;

    @JsonProperty("price")
    private Double price;

    @JsonProperty("from")
    private String from;

    @JsonProperty("to")
    private String to;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("seat_class")
    private String seatClass;

    @JsonProperty("affiliate_link")
    private String affiliateLink;

    @JsonProperty("booking_url")
    private String bookingUrl;

    @JsonProperty("is_common")
    private Boolean isCommon;

    @JsonProperty("type")
    private String type;

    @JsonProperty("progress")
    private Integer progress;

    @JsonProperty("received_flights")
    private Integer receivedFlights;

    @JsonProperty("total_expected")
    private Integer totalExpected;

    @JsonProperty("status")
    private String status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("total_flights")
    private Integer totalFlights;

    // Helper method to convert to Map for JSON serialization
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("source", source != null ? source : "");
        map.put("airline", airline != null ? airline : "");
        map.put("flight_number", flightNumber != null ? flightNumber : "");
        map.put("departure_time", departureTime != null ? departureTime : "");
        map.put("price", price != null ? price : 0.0);
        map.put("from", from != null ? from : "");
        map.put("to", to != null ? to : "");
        map.put("timestamp", timestamp != null ? timestamp : "");
        map.put("seat_class", seatClass != null ? seatClass : "");
        map.put("affiliate_link", affiliateLink != null ? affiliateLink : "");
        map.put("booking_url", bookingUrl != null ? bookingUrl : "");
        map.put("is_common", isCommon != null ? isCommon : false);
        map.put("type", type != null ? type : "");
        map.put("progress", progress != null ? progress : 0);
        map.put("received_flights", receivedFlights != null ? receivedFlights : 0);
        map.put("total_expected", totalExpected != null ? totalExpected : 0);
        map.put("status", status != null ? status : "");
        map.put("message", message != null ? message : "");
        map.put("total_flights", totalFlights != null ? totalFlights : 0);
        return map;
    }
}
