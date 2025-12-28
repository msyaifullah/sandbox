package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GuestInfo {
    @JsonProperty("GuestId")
    private String guestId;

    @JsonProperty("Name")
    private String name;
}
