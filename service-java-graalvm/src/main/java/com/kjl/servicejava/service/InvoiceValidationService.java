package com.kjl.servicejava.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kjl.servicejava.dto.ValidationResponseBuilder;
import com.kjl.servicejava.exception.InvalidOrderTypeException;
import com.kjl.servicejava.model.BaseOrder;
import com.kjl.servicejava.model.InvoiceValidationResponse;
import com.kjl.servicejava.validation.OrderValidator;
import com.kjl.servicejava.validation.OrderValidatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for invoice validation using Strategy and Factory patterns.
 */
@Service
public class InvoiceValidationService {
    private final ObjectMapper objectMapper;
    private final OrderValidatorFactory validatorFactory;

    @Autowired
    public InvoiceValidationService(OrderValidatorFactory validatorFactory) {
        this.objectMapper = new ObjectMapper();
        this.validatorFactory = validatorFactory;
    }

    /**
     * Validates an invoice by detecting the order type and using the appropriate validator.
     *
     * @param jsonBody the JSON body containing the order
     * @return the validation response
     */
    public InvoiceValidationResponse validateInvoice(String jsonBody) {
        try {
            Map<String, Object> wrapper = objectMapper.readValue(jsonBody, Map.class);
            
            // Detect order type and get appropriate validator
            String orderType = detectOrderType(wrapper);
            if (orderType == null) {
                return ValidationResponseBuilder.create()
                    .isValid(false)
                    .withError("Invalid order format - could not parse as any known order type")
                    .build();
            }

            // Get validator using Factory pattern
            OrderValidator validator = validatorFactory.getValidator(orderType);
            
            // Parse order
            BaseOrder order = parseOrder(wrapper, orderType);
            
            // Build response using Builder pattern
            ValidationResponseBuilder builder = ValidationResponseBuilder.create()
                .withOrderType(orderType)
                .withOrderId(order.getTransactionDetails().getOrderId());
            
            InvoiceValidationResponse response = builder.build();
            
            // Validate using Strategy pattern
            validator.validate(order, response);
            
            return response;
            
        } catch (InvalidOrderTypeException e) {
            return ValidationResponseBuilder.create()
                .isValid(false)
                .withError(e.getMessage())
                .build();
        } catch (Exception e) {
            return ValidationResponseBuilder.create()
                .isValid(false)
                .withError("Invalid JSON format: " + e.getMessage())
                .build();
        }
    }

    /**
     * Detects the order type from the JSON wrapper.
     */
    private String detectOrderType(Map<String, Object> wrapper) {
        if (wrapper.containsKey("ProductOrder")) {
            return "Product";
        }
        if (wrapper.containsKey("ServiceOrder")) {
            return "Service";
        }
        if (wrapper.containsKey("ReservationOrder")) {
            return "Reservation";
        }
        if (wrapper.containsKey("AirlinesOrder")) {
            return "Airline";
        }
        return null;
    }

    /**
     * Parses the order from the JSON wrapper based on order type.
     */
    private BaseOrder parseOrder(Map<String, Object> wrapper, String orderType) {
        switch (orderType) {
            case "Product":
                return objectMapper.convertValue(wrapper.get("ProductOrder"), 
                    com.kjl.servicejava.model.ProductOrder.class);
            case "Service":
                return objectMapper.convertValue(wrapper.get("ServiceOrder"), 
                    com.kjl.servicejava.model.ServiceOrder.class);
            case "Reservation":
                return objectMapper.convertValue(wrapper.get("ReservationOrder"), 
                    com.kjl.servicejava.model.ReservationOrder.class);
            case "Airline":
                return objectMapper.convertValue(wrapper.get("AirlinesOrder"), 
                    com.kjl.servicejava.model.AirlinesOrder.class);
            default:
                throw new InvalidOrderTypeException("Unknown order type: " + orderType);
        }
    }
}
