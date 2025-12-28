package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ValidationSummary {
    @JsonProperty("calculated_total")
    private Double calculatedTotal;

    @JsonProperty("declared_total")
    private Double declaredTotal;

    @JsonProperty("total_tax")
    private Double totalTax;

    @JsonProperty("total_discounts")
    private Double totalDiscounts;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("subtotal_before_tax_and_discounts")
    private Double subtotalBeforeTaxAndDiscounts;

    @JsonProperty("subtotal_after_discounts")
    private Double subtotalAfterDiscounts;

    @JsonProperty("subtotal_after_tax")
    private Double subtotalAfterTax;

    @JsonProperty("item_breakdown")
    private List<ItemCalculation> itemBreakdown;
}
