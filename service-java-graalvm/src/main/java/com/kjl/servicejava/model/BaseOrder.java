package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BaseOrder {
    @JsonProperty("GatewayType")
    private String gatewayType;

    @JsonProperty("PaymentType")
    private String paymentType;

    @JsonProperty("CustomerDetail")
    private CustomerDetail customerDetail;

    @JsonProperty("OrderType")
    private String orderType;

    @JsonProperty("Message")
    private String message;

    @JsonProperty("Locale")
    private String locale;

    @JsonProperty("TransactionDetails")
    private TransactionDetails transactionDetails;
}
