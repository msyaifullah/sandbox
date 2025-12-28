package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ReservationSchedule {
    @JsonProperty("ReservationDateTime")
    private String reservationDateTime;

    @JsonProperty("StartDateTime")
    private String startDateTime;

    @JsonProperty("EndDateTime")
    private String endDateTime;

    @JsonProperty("Duration")
    private Integer duration;
}
