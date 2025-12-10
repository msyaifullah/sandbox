package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PriceInfo {
    @JsonProperty("Title")
    private String title;

    @JsonProperty("Amount")
    private Double amount;

    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("AddonID")
    private String addonId;

    @JsonProperty("SegmentId")
    private String segmentId;

    @JsonProperty("PassengerId")
    private String passengerId;
}
