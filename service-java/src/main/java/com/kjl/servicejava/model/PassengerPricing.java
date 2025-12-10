package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class PassengerPricing {
    @JsonProperty("PassengerId")
    private String passengerId;

    @JsonProperty("BasePrice")
    private Double basePrice;

    @JsonProperty("Tax")
    private Double tax;

    @JsonProperty("TotalPrice")
    private Double totalPrice;

    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("PriceBreakdown")
    private List<PriceBreakdown> priceBreakdown;
}
