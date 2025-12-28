package com.kjl.servicejava.validation;

import com.kjl.servicejava.model.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServiceOrderValidator extends AbstractOrderValidator {

    @Override
    public String getOrderType() {
        return "Service";
    }

    @Override
    protected void validateOrderItems(BaseOrder order, InvoiceValidationResponse response, ValidationContext context) {
        ServiceOrder serviceOrder = (ServiceOrder) order;
        context.setCurrency(serviceOrder.getTransactionDetails().getCurrency());
        context.setDeclaredTotal(serviceOrder.getTransactionDetails().getGrossAmt());

        List<ServiceItem> items = serviceOrder.getItems();
        for (int i = 0; i < items.size(); i++) {
            ServiceItem item = items.get(i);
            validateServiceItem(item, i + 1, response, context);
        }
    }

    private void validateServiceItem(ServiceItem item, int index, InvoiceValidationResponse response, ValidationContext context) {
        double basePrice = item.getBasePrice();
        PriceCalculation priceCalc = calculatePriceInfo(item.getPriceInfo());
        double itemAddons = priceCalc.getAddons();

        double subtotalBeforeTaxAndDiscounts = basePrice + itemAddons;

        // Apply discount
        double discountAmount = 0.0;
        if (item.getDiscount() != null && item.getDiscount().getValue() != null && item.getDiscount().getValue() > 0) {
            if ("Percentage".equals(item.getDiscount().getType())) {
                discountAmount = subtotalBeforeTaxAndDiscounts * (item.getDiscount().getValue() / 100);
            } else if ("Fixed".equals(item.getDiscount().getType())) {
                discountAmount = item.getDiscount().getValue();
            }
        }

        double subtotalAfterDiscounts = subtotalBeforeTaxAndDiscounts - discountAmount;
        double tax = item.getTax() != null ? item.getTax() : 0.0;
        double subtotalAfterTax = subtotalAfterDiscounts + tax;
        double itemTotal = subtotalAfterTax;

        ItemCalculation itemCalc = createItemCalculation(
            item.getName(), index, item.getBasePrice(), 1,
            subtotalBeforeTaxAndDiscounts, discountAmount,
            subtotalAfterDiscounts, tax, 0.0,
            itemAddons, itemTotal, item.getTotalPrice()
        );
        context.addItemCalculation(itemCalc);

        if (!itemCalc.getIsValid()) {
            response.getErrors().add(String.format(
                "Service %d (%s): Calculated total %.2f does not match declared total %.2f",
                index, item.getName(), itemTotal, item.getTotalPrice()));
            response.setIsValid(false);
        }

        context.addToCalculatedTotal(itemTotal);
        context.addToTotalTax(tax);
        context.addToTotalDiscounts(discountAmount);
        context.addToSubtotalBeforeTaxAndDiscounts(subtotalBeforeTaxAndDiscounts);
        context.addToSubtotalAfterDiscounts(subtotalAfterDiscounts);
        context.addToSubtotalAfterTax(subtotalAfterTax);
    }
}
