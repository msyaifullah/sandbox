package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class OrderSummary {
    @JsonProperty("TotalItems")
    private Integer totalItems;

    @JsonProperty("TotalMerchants")
    private Integer totalMerchants;

    @JsonProperty("Subtotal")
    private Double subtotal;

    @JsonProperty("TotalShipping")
    private Double totalShipping;

    @JsonProperty("TotalTax")
    private Double totalTax;

    @JsonProperty("TotalDiscounts")
    private Double totalDiscounts;

    @JsonProperty("GrandTotal")
    private Double grandTotal;

    @JsonProperty("MerchantDiscounts")
    private List<MerchantDiscount> merchantDiscounts;

    @JsonProperty("MerchantSummary")
    private List<MerchantSummary> merchantSummary;
}
