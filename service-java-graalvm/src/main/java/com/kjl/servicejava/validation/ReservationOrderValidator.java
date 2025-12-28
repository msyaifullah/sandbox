package com.kjl.servicejava.validation;

import com.kjl.servicejava.model.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReservationOrderValidator extends AbstractOrderValidator {

    @Override
    public String getOrderType() {
        return "Reservation";
    }

    @Override
    protected void validateOrderItems(BaseOrder order, InvoiceValidationResponse response, ValidationContext context) {
        ReservationOrder reservationOrder = (ReservationOrder) order;
        context.setCurrency(reservationOrder.getTransactionDetails().getCurrency());
        context.setDeclaredTotal(reservationOrder.getTransactionDetails().getGrossAmt());

        List<ReservationItem> items = reservationOrder.getItems();
        for (int i = 0; i < items.size(); i++) {
            ReservationItem item = items.get(i);
            validateReservationItem(item, i + 1, response, context);
        }
    }

    private void validateReservationItem(ReservationItem item, int index, InvoiceValidationResponse response, ValidationContext context) {
        double basePrice = item.getBasePrice();
        PriceCalculation priceCalc = calculatePriceInfo(item.getPriceInfo());
        double itemAddons = priceCalc.getAddons();

        double subtotalBeforeTaxAndDiscounts = basePrice + itemAddons;
        double subtotalAfterDiscounts = subtotalBeforeTaxAndDiscounts;
        double tax = item.getTax() != null ? item.getTax() : 0.0;
        double subtotalAfterTax = subtotalAfterDiscounts + tax;
        double itemTotal = subtotalAfterTax;

        ItemCalculation itemCalc = createItemCalculation(
            item.getMerchantName(), index, item.getBasePrice(), 1,
            subtotalBeforeTaxAndDiscounts, 0.0,
            subtotalAfterDiscounts, tax, 0.0,
            itemAddons, itemTotal, item.getTotalPrice()
        );
        context.addItemCalculation(itemCalc);

        if (!itemCalc.getIsValid()) {
            response.getErrors().add(String.format(
                "Reservation %d (%s): Calculated total %.2f does not match declared total %.2f",
                index, item.getMerchantName(), itemTotal, item.getTotalPrice()));
            response.setIsValid(false);
        }

        context.addToCalculatedTotal(itemTotal);
        context.addToTotalTax(tax);
        context.addToSubtotalBeforeTaxAndDiscounts(subtotalBeforeTaxAndDiscounts);
        context.addToSubtotalAfterDiscounts(subtotalAfterDiscounts);
        context.addToSubtotalAfterTax(subtotalAfterTax);
    }
}
