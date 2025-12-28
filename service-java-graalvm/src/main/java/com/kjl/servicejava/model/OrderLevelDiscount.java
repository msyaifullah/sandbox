package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OrderLevelDiscount {
    @JsonProperty("Title")
    private String title;

    @JsonProperty("Amount")
    private Double amount;

    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("Type")
    private String type;

    @JsonProperty("Value")
    private Double value;
}
