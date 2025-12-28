package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GuestCapacity {
    @JsonProperty("Adults")
    private Integer adults;

    @JsonProperty("Children")
    private Integer children;

    @JsonProperty("Infants")
    private Integer infants;
}
