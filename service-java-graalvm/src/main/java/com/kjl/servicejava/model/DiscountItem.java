package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DiscountItem {
    @JsonProperty("Title")
    private String title;

    @JsonProperty("Amount")
    private Double amount;

    @JsonProperty("Currency")
    private String currency;
}
