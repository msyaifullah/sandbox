package com.kjl.servicejava.validation;

import com.kjl.servicejava.model.BaseOrder;
import com.kjl.servicejava.model.InvoiceValidationResponse;

/**
 * Strategy interface for order validation.
 * Each order type implements this interface to provide its specific validation logic.
 */
public interface OrderValidator {
    /**
     * Validates an order and updates the validation response.
     *
     * @param order the order to validate
     * @param response the response object to populate with validation results
     */
    void validate(BaseOrder order, InvoiceValidationResponse response);

    /**
     * Returns the order type this validator handles.
     *
     * @return the order type name
     */
    String getOrderType();
}
