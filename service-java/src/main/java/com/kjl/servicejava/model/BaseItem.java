package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BaseItem {
    @JsonProperty("BasePrice")
    private Double basePrice;

    @JsonProperty("Tax")
    private Double tax;

    @JsonProperty("TotalPrice")
    private Double totalPrice;

    @JsonProperty("Currency")
    private String currency;
}
