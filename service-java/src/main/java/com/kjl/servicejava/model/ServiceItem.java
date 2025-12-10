package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ServiceItem extends BaseItem {
    @JsonProperty("ItemID")
    private String itemId;

    @JsonProperty("MerchantName")
    private String merchantName;

    @JsonProperty("ProviderID")
    private String providerId;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Category")
    private String category;

    @JsonProperty("SubCategory")
    private String subCategory;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("Priority")
    private String priority;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("ServiceInfo")
    private List<ServiceInfo> serviceInfo;

    @JsonProperty("Location")
    private ServiceLocation location;

    @JsonProperty("ProductInfo")
    private List<ProductInfo> productInfo;

    @JsonProperty("PriceInfo")
    private List<PriceInfo> priceInfo;

    @JsonProperty("Discount")
    private Discount discount;

    @JsonProperty("PaymentSchedule")
    private PaymentSchedule paymentSchedule;

    @JsonProperty("Requirements")
    private Requirements requirements;

    @JsonProperty("CancellationPolicy")
    private CancellationPolicy cancellationPolicy;
}
