package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PriceBreakdown {
    @JsonProperty("SegmentId")
    private String segmentId;

    @JsonProperty("Amount")
    private Double amount;

    @JsonProperty("Description")
    private String description;
}
