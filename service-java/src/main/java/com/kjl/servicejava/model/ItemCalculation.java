package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ItemCalculation {
    @JsonProperty("item_name")
    private String itemName;

    @JsonProperty("item_index")
    private Integer itemIndex;

    @JsonProperty("base_price")
    private Double basePrice;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("subtotal_before_tax_and_discounts")
    private Double subtotalBeforeTaxAndDiscounts;

    @JsonProperty("discounts")
    private Double discounts;

    @JsonProperty("subtotal_after_discounts")
    private Double subtotalAfterDiscounts;

    @JsonProperty("tax")
    private Double tax;

    @JsonProperty("shipping")
    private Double shipping;

    @JsonProperty("addons")
    private Double addons;

    @JsonProperty("final_total")
    private Double finalTotal;

    @JsonProperty("declared_total")
    private Double declaredTotal;

    @JsonProperty("is_valid")
    private Boolean isValid;
}
