package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class MerchantDiscount {
    @JsonProperty("MerchantID")
    private String merchantId;

    @JsonProperty("MerchantName")
    private String merchantName;

    @JsonProperty("Discounts")
    private List<DiscountItem> discounts;
}
