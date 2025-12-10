package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Airport {
    @JsonProperty("AirportCode")
    private String airportCode;

    @JsonProperty("City")
    private String city;

    @JsonProperty("Terminal")
    private String terminal;

    @JsonProperty("Gate")
    private String gate;
}
