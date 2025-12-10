package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class Requirements {
    @JsonProperty("CustomerPreparation")
    private List<String> customerPreparation;

    @JsonProperty("ProviderEquipment")
    private List<String> providerEquipment;

    @JsonProperty("SpecialInstructions")
    private String specialInstructions;
}
