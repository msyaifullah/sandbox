package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ServiceProvider {
    @JsonProperty("ProviderID")
    private String providerId;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Contact")
    private Contact contact;

    @JsonProperty("Rating")
    private Double rating;

    @JsonProperty("Specialization")
    private List<String> specialization;
}
