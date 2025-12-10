package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ReservationInfo {
    @JsonProperty("RoomType")
    private String roomType;

    @JsonProperty("TableType")
    private String tableType;

    @JsonProperty("VenueType")
    private String venueType;

    @JsonProperty("ActivityType")
    private String activityType;

    @JsonProperty("StaySchedule")
    private StaySchedule staySchedule;

    @JsonProperty("ReservationSchedule")
    private ReservationSchedule reservationSchedule;

    @JsonProperty("SeatingCapacity")
    private Integer seatingCapacity;

    @JsonProperty("VenueCapacity")
    private Integer venueCapacity;

    @JsonProperty("Location")
    private ReservationLocation location;
}
