package com.kjl.servicejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class InvoiceValidationResponse {
    @JsonProperty("is_valid")
    private Boolean isValid;

    @JsonProperty("order_type")
    private String orderType;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("errors")
    private List<String> errors;

    @JsonProperty("warnings")
    private List<String> warnings;

    @JsonProperty("summary")
    private ValidationSummary summary;
}
