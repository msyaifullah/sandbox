package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CancellationPolicy {
    @JsonProperty("FreeCancellationHours")
    private Integer freeCancellationHours;

    @JsonProperty("PartialRefundHours")
    private Integer partialRefundHours;

    @JsonProperty("NoRefundHours")
    private Integer noRefundHours;
}
