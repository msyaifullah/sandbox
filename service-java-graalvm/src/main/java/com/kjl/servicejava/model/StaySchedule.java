package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StaySchedule {
    @JsonProperty("CheckInDateTime")
    private String checkInDateTime;

    @JsonProperty("CheckOutDateTime")
    private String checkOutDateTime;

    @JsonProperty("Duration")
    private Integer duration;
}
