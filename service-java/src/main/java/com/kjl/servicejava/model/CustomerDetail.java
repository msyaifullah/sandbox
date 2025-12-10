package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CustomerDetail {
    @JsonProperty("Number")
    private String number;

    @JsonProperty("FirstName")
    private String firstName;

    @JsonProperty("LastName")
    private String lastName;

    @JsonProperty("Email")
    private String email;

    @JsonProperty("Phone")
    private String phone;

    @JsonProperty("BillingAddress")
    private Address billingAddress;

    @JsonProperty("ShippingAddress")
    private Address shippingAddress;
}
