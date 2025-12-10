package com.kjl.servicejava.validation;

import com.kjl.servicejava.model.BaseOrder;
import com.kjl.servicejava.model.InvoiceValidationResponse;
import com.kjl.servicejava.model.ItemCalculation;
import com.kjl.servicejava.model.PriceInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for order validators implementing Template Method pattern.
 * Provides common validation logic and calculation methods.
 */
public abstract class AbstractOrderValidator implements OrderValidator {
    protected static final double TOLERANCE = 0.01;

    @Override
    public void validate(BaseOrder order, InvoiceValidationResponse response) {
        response.setOrderId(order.getTransactionDetails().getOrderId());
        response.setOrderType(getOrderType());
        
        ValidationContext context = new ValidationContext();
        validateOrderItems(order, response, context);
        validateOrderTotal(order, response, context);
        populateSummary(response, context);
    }

    /**
     * Template method for validating order items.
     * Subclasses implement specific item validation logic.
     */
    protected abstract void validateOrderItems(BaseOrder order, InvoiceValidationResponse response, ValidationContext context);

    /**
     * Validates the total order amount.
     */
    protected void validateOrderTotal(BaseOrder order, InvoiceValidationResponse response, ValidationContext context) {
        double calculatedTotal = context.getCalculatedTotal();
        double declaredTotal = order.getTransactionDetails().getGrossAmt();

        if (Math.abs(calculatedTotal - declaredTotal) > TOLERANCE) {
            response.getErrors().add(String.format(
                "Calculated total %.2f does not match declared gross amount %.2f",
                calculatedTotal, declaredTotal));
            response.setIsValid(false);
        }
    }

    /**
     * Populates the validation summary with calculated values.
     */
    protected void populateSummary(InvoiceValidationResponse response, ValidationContext context) {
        response.getSummary().setCalculatedTotal(context.getCalculatedTotal());
        response.getSummary().setDeclaredTotal(context.getDeclaredTotal());
        response.getSummary().setTotalTax(context.getTotalTax());
        response.getSummary().setTotalDiscounts(context.getTotalDiscounts());
        response.getSummary().setCurrency(context.getCurrency());
        response.getSummary().setSubtotalBeforeTaxAndDiscounts(context.getSubtotalBeforeTaxAndDiscounts());
        response.getSummary().setSubtotalAfterDiscounts(context.getSubtotalAfterDiscounts());
        response.getSummary().setSubtotalAfterTax(context.getSubtotalAfterTax());
    }

    /**
     * Calculates discounts and addons from price info.
     */
    protected PriceCalculation calculatePriceInfo(List<PriceInfo> priceInfoList) {
        double discounts = 0.0;
        double addons = 0.0;

        if (priceInfoList != null) {
            for (PriceInfo priceInfo : priceInfoList) {
                if (priceInfo.getAmount() != null) {
                    if (priceInfo.getAmount() < 0) {
                        discounts += Math.abs(priceInfo.getAmount());
                    } else {
                        addons += priceInfo.getAmount();
                    }
                }
            }
        }

        return new PriceCalculation(discounts, addons);
    }

    /**
     * Creates an item calculation object.
     */
    protected ItemCalculation createItemCalculation(
            String itemName, int index, double basePrice, int quantity,
            double subtotalBeforeTaxAndDiscounts, double discounts,
            double subtotalAfterDiscounts, double tax, double shipping,
            double addons, double finalTotal, double declaredTotal) {
        
        ItemCalculation itemCalc = new ItemCalculation();
        itemCalc.setItemName(itemName);
        itemCalc.setItemIndex(index);
        itemCalc.setBasePrice(basePrice);
        itemCalc.setQuantity(quantity);
        itemCalc.setSubtotalBeforeTaxAndDiscounts(subtotalBeforeTaxAndDiscounts);
        itemCalc.setDiscounts(discounts);
        itemCalc.setSubtotalAfterDiscounts(subtotalAfterDiscounts);
        itemCalc.setTax(tax);
        itemCalc.setShipping(shipping);
        itemCalc.setAddons(addons);
        itemCalc.setFinalTotal(finalTotal);
        itemCalc.setDeclaredTotal(declaredTotal);
        itemCalc.setIsValid(Math.abs(finalTotal - declaredTotal) <= TOLERANCE);
        return itemCalc;
    }

    /**
     * Helper class to hold validation context during validation process.
     */
    protected static class ValidationContext {
        private double calculatedTotal = 0.0;
        private double totalTax = 0.0;
        private double totalDiscounts = 0.0;
        private double subtotalBeforeTaxAndDiscounts = 0.0;
        private double subtotalAfterDiscounts = 0.0;
        private double subtotalAfterTax = 0.0;
        private String currency;
        private double declaredTotal = 0.0;
        private List<ItemCalculation> itemBreakdown = new ArrayList<>();

        // Getters and setters
        public double getCalculatedTotal() { return calculatedTotal; }
        public void addToCalculatedTotal(double amount) { this.calculatedTotal += amount; }

        public double getTotalTax() { return totalTax; }
        public void addToTotalTax(double amount) { this.totalTax += amount; }

        public double getTotalDiscounts() { return totalDiscounts; }
        public void addToTotalDiscounts(double amount) { this.totalDiscounts += amount; }

        public double getSubtotalBeforeTaxAndDiscounts() { return subtotalBeforeTaxAndDiscounts; }
        public void addToSubtotalBeforeTaxAndDiscounts(double amount) { this.subtotalBeforeTaxAndDiscounts += amount; }

        public double getSubtotalAfterDiscounts() { return subtotalAfterDiscounts; }
        public void addToSubtotalAfterDiscounts(double amount) { this.subtotalAfterDiscounts += amount; }

        public double getSubtotalAfterTax() { return subtotalAfterTax; }
        public void addToSubtotalAfterTax(double amount) { this.subtotalAfterTax += amount; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public double getDeclaredTotal() { return declaredTotal; }
        public void setDeclaredTotal(double declaredTotal) { this.declaredTotal = declaredTotal; }

        public List<ItemCalculation> getItemBreakdown() { return itemBreakdown; }
        public void addItemCalculation(ItemCalculation itemCalc) { this.itemBreakdown.add(itemCalc); }
    }

    /**
     * Helper class for price calculations.
     */
    protected static class PriceCalculation {
        private final double discounts;
        private final double addons;

        public PriceCalculation(double discounts, double addons) {
            this.discounts = discounts;
            this.addons = addons;
        }

        public double getDiscounts() { return discounts; }
        public double getAddons() { return addons; }
    }
}
