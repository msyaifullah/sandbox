package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MerchantSummary {
    @JsonProperty("MerchantID")
    private String merchantId;

    @JsonProperty("MerchantName")
    private String merchantName;

    @JsonProperty("ItemCount")
    private Integer itemCount;

    @JsonProperty("Subtotal")
    private Double subtotal;

    @JsonProperty("ShippingCost")
    private Double shippingCost;

    @JsonProperty("Tax")
    private Double tax;

    @JsonProperty("Discounts")
    private Double discounts;

    @JsonProperty("TotalAmount")
    private Double totalAmount;
}
