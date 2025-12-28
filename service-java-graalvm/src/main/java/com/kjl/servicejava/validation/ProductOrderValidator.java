package com.kjl.servicejava.validation;

import com.kjl.servicejava.model.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductOrderValidator extends AbstractOrderValidator {

    @Override
    public String getOrderType() {
        return "Product";
    }

    @Override
    protected void validateOrderItems(BaseOrder order, InvoiceValidationResponse response, ValidationContext context) {
        ProductOrder productOrder = (ProductOrder) order;
        context.setCurrency(productOrder.getTransactionDetails().getCurrency());
        context.setDeclaredTotal(productOrder.getTransactionDetails().getGrossAmt());

        List<ProductItem> items = productOrder.getItems();
        for (int i = 0; i < items.size(); i++) {
            ProductItem item = items.get(i);
            validateProductItem(item, i + 1, response, context);
        }

        // Validate order-level discounts
        if (productOrder.getTransactionDetails().getOrderLevelDiscounts() != null) {
            for (OrderLevelDiscount discount : productOrder.getTransactionDetails().getOrderLevelDiscounts()) {
                context.addToCalculatedTotal(discount.getAmount());
                context.addToTotalDiscounts(Math.abs(discount.getAmount()));
                context.addToSubtotalAfterDiscounts(discount.getAmount());
            }
        }

        // Validate order summary if present
        if (productOrder.getOrderSummary() != null) {
            OrderSummary summary = productOrder.getOrderSummary();
            if (Math.abs(summary.getSubtotal() - context.getCalculatedTotal()) > TOLERANCE) {
                response.getWarnings().add(String.format(
                    "Order summary subtotal %.2f does not match calculated total %.2f",
                    summary.getSubtotal(), context.getCalculatedTotal()));
            }
        }
    }

    private void validateProductItem(ProductItem item, int index, InvoiceValidationResponse response, ValidationContext context) {
        double basePrice = item.getBasePrice() * item.getQty();
        PriceCalculation priceCalc = calculatePriceInfo(item.getPriceInfo());
        double itemDiscounts = priceCalc.getDiscounts();
        double itemAddons = priceCalc.getAddons();

        double subtotalBeforeTaxAndDiscounts = basePrice + itemAddons;
        double subtotalAfterDiscounts = basePrice + itemAddons - itemDiscounts;
        double tax = item.getTax() != null ? item.getTax() : 0.0;
        double subtotalAfterTax = subtotalAfterDiscounts + tax;
        double shippingCost = (item.getShippingDetails() != null && item.getShippingDetails().getCost() != null)
            ? item.getShippingDetails().getCost() : 0.0;
        double itemTotal = subtotalAfterTax + shippingCost;

        ItemCalculation itemCalc = createItemCalculation(
            item.getName(), index, item.getBasePrice(), item.getQty(),
            subtotalBeforeTaxAndDiscounts, itemDiscounts,
            subtotalAfterDiscounts, tax, shippingCost,
            itemAddons, itemTotal, item.getTotalPrice()
        );
        context.addItemCalculation(itemCalc);

        if (!itemCalc.getIsValid()) {
            response.getErrors().add(String.format(
                "Item %d (%s): Calculated total %.2f does not match declared total %.2f",
                index, item.getName(), itemTotal, item.getTotalPrice()));
            response.setIsValid(false);
        }

        context.addToCalculatedTotal(itemTotal);
        context.addToTotalTax(tax);
        context.addToTotalDiscounts(itemDiscounts);
        context.addToSubtotalBeforeTaxAndDiscounts(subtotalBeforeTaxAndDiscounts);
        context.addToSubtotalAfterDiscounts(subtotalAfterDiscounts);
        context.addToSubtotalAfterTax(subtotalAfterTax);
    }
}
