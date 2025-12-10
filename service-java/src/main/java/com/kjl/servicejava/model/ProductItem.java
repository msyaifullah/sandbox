package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ProductItem extends BaseItem {
    @JsonProperty("ProductNumber")
    private String productNumber;

    @JsonProperty("MerchantID")
    private String merchantId;

    @JsonProperty("MerchantName")
    private String merchantName;

    @JsonProperty("MerchantContact")
    private Contact merchantContact;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Qty")
    private Integer qty;

    @JsonProperty("Brand")
    private String brand;

    @JsonProperty("Category")
    private String category;

    @JsonProperty("ProductInfo")
    private List<ProductInfo> productInfo;

    @JsonProperty("PriceInfo")
    private List<PriceInfo> priceInfo;

    @JsonProperty("ShippingDetails")
    private ShippingDetails shippingDetails;
}
