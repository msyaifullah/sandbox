package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ServiceOrder extends BaseOrder {
    @JsonProperty("OrderStatus")
    private String orderStatus;

    @JsonProperty("ServiceProviders")
    private List<ServiceProvider> serviceProviders;

    @JsonProperty("Items")
    private List<ServiceItem> items;
}
