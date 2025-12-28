package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PassengerInfo {
    @JsonProperty("PassengerId")
    private String passengerId;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("TicketNumber")
    private String ticketNumber;

    @JsonProperty("PassengerType")
    private String passengerType;

    @JsonProperty("DateOfBirth")
    private String dateOfBirth;
}
