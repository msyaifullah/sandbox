package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Contact {
    @JsonProperty("Email")
    private String email;

    @JsonProperty("Phone")
    private String phone;

    @JsonProperty("Address")
    private String address;
}
