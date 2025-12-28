package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FlightSchedule {
    @JsonProperty("DepartureTime")
    private String departureTime;

    @JsonProperty("ArrivalTime")
    private String arrivalTime;
}
