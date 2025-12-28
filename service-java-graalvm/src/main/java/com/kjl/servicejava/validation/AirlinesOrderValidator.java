package com.kjl.servicejava.validation;

import com.kjl.servicejava.model.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AirlinesOrderValidator extends AbstractOrderValidator {

    @Override
    public String getOrderType() {
        return "Airline";
    }

    @Override
    protected void validateOrderItems(BaseOrder order, InvoiceValidationResponse response, ValidationContext context) {
        AirlinesOrder airlinesOrder = (AirlinesOrder) order;
        context.setCurrency(airlinesOrder.getTransactionDetails().getCurrency());
        context.setDeclaredTotal(airlinesOrder.getTransactionDetails().getGrossAmt());

        List<AirlinesItem> items = airlinesOrder.getItems();
        for (int i = 0; i < items.size(); i++) {
            AirlinesItem item = items.get(i);
            validateAirlinesItem(item, i + 1, response, context);
        }
    }

    private void validateAirlinesItem(AirlinesItem item, int index, InvoiceValidationResponse response, ValidationContext context) {
        double basePrice = item.getBasePrice();
        PriceCalculation priceCalc = calculatePriceInfo(item.getPriceInfo());
        double itemAddons = priceCalc.getAddons();

        double subtotalBeforeTaxAndDiscounts = basePrice + itemAddons;
        double subtotalAfterDiscounts = subtotalBeforeTaxAndDiscounts;
        double tax = item.getTax() != null ? item.getTax() : 0.0;
        double subtotalAfterTax = subtotalAfterDiscounts + tax;
        double itemTotal = subtotalAfterTax;

        String itemName = String.format("Flight (PNR: %s)", item.getPnrNumber());
        ItemCalculation itemCalc = createItemCalculation(
            itemName, index, item.getBasePrice(), 1,
            subtotalBeforeTaxAndDiscounts, 0.0,
            subtotalAfterDiscounts, tax, 0.0,
            itemAddons, itemTotal, item.getTotalPrice()
        );
        context.addItemCalculation(itemCalc);

        if (!itemCalc.getIsValid()) {
            response.getErrors().add(String.format(
                "Flight %d (PNR: %s): Calculated total %.2f does not match declared total %.2f",
                index, item.getPnrNumber(), itemTotal, item.getTotalPrice()));
            response.setIsValid(false);
        }

        context.addToCalculatedTotal(itemTotal);
        context.addToTotalTax(tax);
        context.addToSubtotalBeforeTaxAndDiscounts(subtotalBeforeTaxAndDiscounts);
        context.addToSubtotalAfterDiscounts(subtotalAfterDiscounts);
        context.addToSubtotalAfterTax(subtotalAfterTax);
    }
}
