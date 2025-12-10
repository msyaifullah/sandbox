package com.kjl.servicejava.validation;

import com.kjl.servicejava.exception.InvalidOrderTypeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory pattern for creating order validators.
 * Uses Spring's dependency injection to automatically discover all validators.
 */
@Component
public class OrderValidatorFactory {
    private final Map<String, OrderValidator> validators;

    @Autowired
    public OrderValidatorFactory(List<OrderValidator> validatorList) {
        this.validators = validatorList.stream()
            .collect(Collectors.toMap(OrderValidator::getOrderType, Function.identity()));
    }

    /**
     * Gets the appropriate validator for the given order type.
     *
     * @param orderType the order type (Product, Service, Reservation, Airline)
     * @return the validator for the order type
     * @throws InvalidOrderTypeException if no validator is found for the order type
     */
    public OrderValidator getValidator(String orderType) {
        OrderValidator validator = validators.get(orderType);
        if (validator == null) {
            throw new InvalidOrderTypeException("No validator found for order type: " + orderType);
        }
        return validator;
    }

    /**
     * Checks if a validator exists for the given order type.
     *
     * @param orderType the order type to check
     * @return true if a validator exists, false otherwise
     */
    public boolean hasValidator(String orderType) {
        return validators.containsKey(orderType);
    }
}
