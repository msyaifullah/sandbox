# Invoice Management System - Service B

This service provides comprehensive invoice validation for multiple order types including Product Orders, Service Orders, Reservation Orders, and Airlines Orders.

## Features

### Order Type Support
- **Product Orders**: E-commerce orders with multiple merchants, items, shipping, and discounts
- **Service Orders**: Service bookings with providers, addons, and scheduling
- **Reservation Orders**: Hotel, restaurant, venue, and activity bookings
- **Airlines Orders**: Flight bookings with passenger information and pricing

### Validation Capabilities
- **Tax Validation**: Ensures tax calculations are accurate
- **Amount Validation**: Validates total amounts match calculated values
- **Discount Validation**: Verifies discount calculations and applications
- **Item-level Validation**: Checks individual item totals
- **Order-level Validation**: Validates overall order totals

## API Endpoints

### POST /api/invoice/validate
Validates invoice data for any supported order type.

**Headers:**
- `Authorization: Bearer <JWT_TOKEN>`
- `Content-Type: application/json`

**Request Body:** Any of the supported order JSON formats

**Response:**
```json
{
  "is_valid": true,
  "order_type": "Product",
  "order_id": "generated-order-id",
  "errors": [],
  "warnings": [],
  "summary": {
    "calculated_total": 667000,
    "declared_total": 667000,
    "total_tax": 53500,
    "total_discounts": 40000,
    "currency": "IDR"
  }
}
```

## Data Consistency Analysis

### Customer Data Consistency
The customer information remains consistent across all order types:
- Customer ID, name, email, phone
- Billing address details
- Shipping address (for product orders)

### Validation Logic by Order Type

#### Product Orders
- **Base Price × Quantity** for each item
- **Item-level discounts** (negative amounts in PriceInfo)
- **Tax** addition per item
- **Shipping costs** per item
- **Order-level discounts** from TransactionDetails
- **Order summary validation** (if present)

#### Service Orders
- **Base price** for each service
- **Addon costs** from PriceInfo
- **Percentage or Fixed discounts** from Discount object
- **Tax** addition per service
- **Payment schedule** validation

#### Reservation Orders
- **Base price** for each reservation
- **Additional costs** from PriceInfo (fees, deposits, etc.)
- **Tax** addition per reservation
- **Capacity validation** (guests, participants)

#### Airlines Orders
- **Base price** for each flight booking
- **Additional costs** from PriceInfo (fuel surcharges, etc.)
- **Tax** addition per booking
- **Passenger pricing** validation

## Error Handling

The system provides detailed error messages for:
- **Calculation mismatches**: When calculated totals don't match declared totals
- **Item-level errors**: Specific item calculation issues
- **Order-level errors**: Overall order validation failures
- **Format errors**: Invalid JSON or unsupported order types

## Developer Benefits

### Easy Validation
- **Automatic parsing**: System automatically detects order type
- **Comprehensive validation**: Covers all financial aspects
- **Detailed reporting**: Clear error and warning messages
- **Flexible tolerance**: 0.01 tolerance for floating-point comparisons

### Tax Validation
- Validates tax calculations at item level
- Ensures tax amounts are properly applied
- Reports total tax across all items

### Amount Validation
- Cross-checks calculated vs declared amounts
- Validates item-level totals
- Ensures order-level totals are correct

### Discount Validation
- Validates both percentage and fixed discounts
- Checks item-level and order-level discounts
- Ensures discount calculations are accurate

## Usage Examples

### Testing with Sample Data
```bash
# Start the service
go run .

# Run the test script
./test_invoice_validation.sh
```

### Manual Testing
```bash
# Get JWT token
curl -X GET http://localhost:3001/call-a

# Validate an order
curl -X POST \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d @../data/product_order.json \
  http://localhost:3001/api/invoice/validate
```

## Data Structure Benefits

### Consistent Customer Information
- Customer details remain consistent across order types
- Billing address standardization
- Shipping address handling for products

### Flexible Order Types
- Each order type has specific validation logic
- Extensible structure for new order types
- Maintains backward compatibility

### Comprehensive Validation
- Item-level and order-level validation
- Tax, discount, and amount validation
- Detailed error reporting for debugging

## Security

- **JWT Authentication**: All endpoints require valid JWT tokens
- **Input Validation**: Comprehensive JSON parsing and validation
- **Error Handling**: Secure error messages without exposing internals

## Dependencies

- `github.com/gin-gonic/gin`: HTTP framework
- `github.com/golang-jwt/jwt/v5`: JWT handling
- Standard Go libraries for JSON parsing and math operations

## Running the Service

```bash
cd service-b
go mod tidy
go run .
```

The service will start on port 3001 and be ready to validate invoices. 