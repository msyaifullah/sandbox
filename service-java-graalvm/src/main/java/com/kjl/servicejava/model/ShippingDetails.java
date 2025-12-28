package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ShippingDetails {
    @JsonProperty("Method")
    private String method;

    @JsonProperty("Cost")
    private Double cost;

    @JsonProperty("EstimatedDays")
    private String estimatedDays;

    @JsonProperty("TrackingNumber")
    private String trackingNumber;
}
