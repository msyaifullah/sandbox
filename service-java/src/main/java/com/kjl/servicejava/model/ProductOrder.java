package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ProductOrder extends BaseOrder {
    @JsonProperty("Items")
    private List<ProductItem> items;

    @JsonProperty("OrderSummary")
    private OrderSummary orderSummary;
}
