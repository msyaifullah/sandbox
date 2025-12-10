package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ServiceLocation {
    @JsonProperty("Address")
    private String address;

    @JsonProperty("City")
    private String city;

    @JsonProperty("Postcode")
    private String postcode;

    @JsonProperty("CountryCode")
    private String countryCode;

    @JsonProperty("Coordinates")
    private Coordinates coordinates;

    @JsonProperty("AccessNotes")
    private String accessNotes;
}
