package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ReservationOrder extends BaseOrder {
    @JsonProperty("BookingStatus")
    private String bookingStatus;

    @JsonProperty("CancellationPolicy")
    private ReservationCancellationPolicy cancellationPolicy;

    @JsonProperty("SpecialRequests")
    private List<String> specialRequests;

    @JsonProperty("EmergencyContact")
    private EmergencyContact emergencyContact;

    @JsonProperty("Items")
    private List<ReservationItem> items;
}
