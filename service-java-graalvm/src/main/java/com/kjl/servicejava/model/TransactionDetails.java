package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class TransactionDetails {
    @JsonProperty("OrderID")
    private String orderId;

    @JsonProperty("GrossAmt")
    private Double grossAmt;

    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("OrderLevelDiscounts")
    private List<OrderLevelDiscount> orderLevelDiscounts;
}
