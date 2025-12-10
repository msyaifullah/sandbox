package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ServiceSchedule {
    @JsonProperty("CheckInTime")
    private String checkInTime;

    @JsonProperty("CheckOutTime")
    private String checkOutTime;

    @JsonProperty("EstimatedDuration")
    private String estimatedDuration;
}
