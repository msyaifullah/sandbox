# Enhanced Invoice Validation Features

The invoice validation system now provides detailed calculation breakdowns showing amounts before and after tax and discounts.

## New Response Structure

### Enhanced Summary Fields
```json
{
  "summary": {
    "calculated_total": 586500,
    "declared_total": 586500,
    "total_tax": 0,
    "total_discounts": 48500,
    "currency": "RP",
    "subtotal_before_tax_and_discounts": 635000,
    "subtotal_after_discounts": 586500,
    "subtotal_after_tax": 586500,
    "item_breakdown": [...]
  }
}
```

### New Fields Explained

1. **`subtotal_before_tax_and_discounts`**: Total amount before any taxes or discounts are applied
2. **`subtotal_after_discounts`**: Total amount after discounts but before taxes
3. **`subtotal_after_tax`**: Total amount after discounts and taxes
4. **`item_breakdown`**: Detailed breakdown for each item showing step-by-step calculations

## Item Breakdown Structure

Each item in the breakdown shows:

```json
{
  "item_name": "Cleaning Garden",
  "item_index": 1,
  "base_price": 200000,
  "quantity": 1,
  "subtotal_before_tax_and_discounts": 235000,
  "discounts": 23500,
  "subtotal_after_discounts": 211500,
  "tax": 0,
  "shipping": 15000,
  "addons": 35000,
  "final_total": 211500,
  "declared_total": 211500,
  "is_valid": true
}
```

### Item Breakdown Fields

- **`item_name`**: Name of the item/service
- **`item_index`**: Position in the order (1-based)
- **`base_price`**: Base price per unit
- **`quantity`**: Number of units
- **`subtotal_before_tax_and_discounts`**: Base price + addons
- **`discounts`**: Total discounts applied to this item
- **`subtotal_after_discounts`**: Amount after discounts
- **`tax`**: Tax amount for this item
- **`shipping`**: Shipping cost (for products)
- **`addons`**: Additional costs/addons
- **`final_total`**: Calculated total for this item
- **`declared_total`**: Declared total for this item
- **`is_valid`**: Whether calculated matches declared total

## Calculation Examples

### Product Order Example
```
Item: Jacket Running
Base Price: 200,000 × 1 = 200,000
Addons: +10,000 (Size M)
Subtotal Before Tax & Discounts: 210,000
Discounts: -20,000 (Discounts Raya)
Subtotal After Discounts: 190,000
Tax: +19,000
Shipping: +15,000
Final Total: 224,000
```

### Service Order Example
```
Service: Cleaning Garden
Base Price: 200,000
Addons: +35,000 (Garden Tools + Fertilizer)
Subtotal Before Tax & Discounts: 235,000
Discounts: -23,500 (10% discount)
Subtotal After Discounts: 211,500
Tax: +0
Final Total: 211,500
```

## Benefits for Developers

### ✅ **Detailed Calculation Visibility**
- See exactly how each amount is calculated
- Identify where discrepancies occur
- Understand tax and discount applications

### ✅ **Step-by-Step Breakdown**
- Before tax and discounts
- After discounts
- After tax
- Final totals

### ✅ **Item-Level Validation**
- Each item shows its own calculation
- Individual item validation status
- Clear error identification

### ✅ **Order-Level Summary**
- Overall calculation flow
- Total tax and discount amounts
- Currency support

## Usage Examples

### Testing Product Orders
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d @corrected_product_order.json \
  http://localhost:3001/api/invoice/validate
```

### Testing Service Orders
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d @corrected_service_order.json \
  http://localhost:3001/api/invoice/validate
```

## Validation Logic by Order Type

### Product Orders
- **Base Price × Quantity** for each item
- **Addons** (positive amounts in PriceInfo)
- **Item-level discounts** (negative amounts in PriceInfo)
- **Tax** addition per item
- **Shipping costs** per item
- **Order-level discounts** from TransactionDetails

### Service Orders
- **Base price** for each service
- **Addon costs** from PriceInfo
- **Percentage or Fixed discounts** from Discount object
- **Tax** addition per service

### Reservation Orders
- **Base price** for each reservation
- **Additional costs** from PriceInfo (fees, deposits, etc.)
- **Tax** addition per reservation

### Airlines Orders
- **Base price** for each flight booking
- **Additional costs** from PriceInfo (fuel surcharges, etc.)
- **Tax** addition per booking

## Error Detection

The enhanced system can now detect:

1. **Item-level calculation errors** - Specific items with wrong totals
2. **Discount calculation errors** - Incorrect discount applications
3. **Tax calculation errors** - Wrong tax amounts
4. **Order-level total errors** - Overall total mismatches
5. **Addon calculation errors** - Incorrect addon applications

## Debugging Benefits

- **Clear calculation flow** - See each step of the calculation
- **Item-specific errors** - Know exactly which item has issues
- **Detailed breakdowns** - Understand where discrepancies occur
- **Validation status per item** - See which items pass/fail validation

This enhanced validation system makes it much easier for developers to understand and debug invoice calculations across all order types. 