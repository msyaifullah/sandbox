package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FlightInfo {
    @JsonProperty("ConnectionId")
    private String connectionId;

    @JsonProperty("SegmentId")
    private String segmentId;

    @JsonProperty("FlightNumber")
    private String flightNumber;

    @JsonProperty("AircraftType")
    private String aircraftType;

    @JsonProperty("DepartureAirport")
    private Airport departureAirport;

    @JsonProperty("ArrivalAirport")
    private Airport arrivalAirport;

    @JsonProperty("FlightSchedule")
    private FlightSchedule flightSchedule;

    @JsonProperty("TravelClass")
    private String travelClass;
}
