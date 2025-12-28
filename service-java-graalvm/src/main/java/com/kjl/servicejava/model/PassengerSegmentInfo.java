package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class PassengerSegmentInfo {
    @JsonProperty("SegmentId")
    private String segmentId;

    @JsonProperty("PassengerId")
    private String passengerId;

    @JsonProperty("Seat")
    private String seat;

    @JsonProperty("SeatType")
    private String seatType;

    @JsonProperty("ProductInfo")
    private List<ProductInfo> productInfo;
}
