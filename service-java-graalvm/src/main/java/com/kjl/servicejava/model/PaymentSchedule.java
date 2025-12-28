package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaymentSchedule {
    @JsonProperty("Type")
    private String type;

    @JsonProperty("AdvancePayment")
    private Double advancePayment;

    @JsonProperty("RemainingPayment")
    private Double remainingPayment;
}
