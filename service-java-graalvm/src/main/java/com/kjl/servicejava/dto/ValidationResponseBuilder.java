package com.kjl.servicejava.dto;

import com.kjl.servicejava.model.InvoiceValidationResponse;
import com.kjl.servicejava.model.ValidationSummary;

import java.util.ArrayList;

/**
 * Builder pattern for creating InvoiceValidationResponse objects.
 */
public class ValidationResponseBuilder {
    private final InvoiceValidationResponse response;

    private ValidationResponseBuilder() {
        this.response = new InvoiceValidationResponse();
        this.response.setIsValid(true);
        this.response.setErrors(new ArrayList<>());
        this.response.setWarnings(new ArrayList<>());
        ValidationSummary summary = new ValidationSummary();
        summary.setItemBreakdown(new ArrayList<>());
        this.response.setSummary(summary);
    }

    public static ValidationResponseBuilder create() {
        return new ValidationResponseBuilder();
    }

    public ValidationResponseBuilder withOrderType(String orderType) {
        response.setOrderType(orderType);
        return this;
    }

    public ValidationResponseBuilder withOrderId(String orderId) {
        response.setOrderId(orderId);
        return this;
    }

    public ValidationResponseBuilder withError(String error) {
        response.getErrors().add(error);
        response.setIsValid(false);
        return this;
    }

    public ValidationResponseBuilder withWarning(String warning) {
        response.getWarnings().add(warning);
        return this;
    }

    public ValidationResponseBuilder isValid(boolean isValid) {
        response.setIsValid(isValid);
        return this;
    }

    public InvoiceValidationResponse build() {
        return response;
    }
}
