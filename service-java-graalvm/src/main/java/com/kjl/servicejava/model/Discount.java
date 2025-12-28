package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Discount {
    @JsonProperty("Type")
    private String type;

    @JsonProperty("Value")
    private Double value;

    @JsonProperty("Reason")
    private String reason;
}
