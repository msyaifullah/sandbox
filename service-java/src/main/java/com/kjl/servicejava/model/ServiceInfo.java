package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ServiceInfo {
    @JsonProperty("ServiceType")
    private String serviceType;

    @JsonProperty("Duration")
    private Integer duration;

    @JsonProperty("ServiceSchedule")
    private ServiceSchedule serviceSchedule;
}
