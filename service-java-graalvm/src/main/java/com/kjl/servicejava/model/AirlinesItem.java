package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class AirlinesItem extends BaseItem {
    @JsonProperty("PNRNumber")
    private String pnrNumber;

    @JsonProperty("JourneyType")
    private String journeyType;

    @JsonProperty("PassengerInfo")
    private List<PassengerInfo> passengerInfo;

    @JsonProperty("FlightInfo")
    private List<FlightInfo> passengerFlightInfo;

    @JsonProperty("PassengerSegmentInfo")
    private List<PassengerSegmentInfo> passengerSegmentInfo;

    @JsonProperty("PassengerPricing")
    private List<PassengerPricing> passengerPricing;

    @JsonProperty("PriceInfo")
    private List<PriceInfo> priceInfo;
}
