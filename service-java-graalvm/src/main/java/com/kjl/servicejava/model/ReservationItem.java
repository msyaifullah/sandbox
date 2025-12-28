package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ReservationItem extends BaseItem {
    @JsonProperty("ReservationNumber")
    private String reservationNumber;

    @JsonProperty("MerchantName")
    private String merchantName;

    @JsonProperty("ReservationType")
    private String reservationType;

    @JsonProperty("GuestInfo")
    private List<GuestInfo> guestInfo;

    @JsonProperty("ReservationInfo")
    private List<ReservationInfo> reservationInfo;

    @JsonProperty("ProductInfo")
    private List<ProductInfo> productInfo;

    @JsonProperty("PriceInfo")
    private List<PriceInfo> priceInfo;

    @JsonProperty("RoomCapacity")
    private GuestCapacity roomCapacity;

    @JsonProperty("TableCapacity")
    private GuestCapacity tableCapacity;

    @JsonProperty("ExpectedGuests")
    private GuestCapacity expectedGuests;

    @JsonProperty("ParticipantCapacity")
    private GuestCapacity participantCapacity;
}
