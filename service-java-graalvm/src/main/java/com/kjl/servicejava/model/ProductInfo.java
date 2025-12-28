package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProductInfo {
    @JsonProperty("Title")
    private String title;

    @JsonProperty("Value")
    private String value;

    @JsonProperty("Category")
    private String category;

    @JsonProperty("AddonID")
    private String addonId;

    @JsonProperty("Required")
    private Boolean required;

    @JsonProperty("Selected")
    private Boolean selected;
}
