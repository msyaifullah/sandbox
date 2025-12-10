package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ReservationCancellationPolicy {
    @JsonProperty("Refundable")
    private Boolean refundable;

    @JsonProperty("CancellationDeadline")
    private String cancellationDeadline;

    @JsonProperty("RefundPercentage")
    private Double refundPercentage;
}
