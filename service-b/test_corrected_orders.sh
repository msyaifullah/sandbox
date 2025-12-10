#!/bin/bash

# Test script for corrected invoice validation
# Make sure service-b is running on port 3001

echo "Testing Corrected Invoice Validation"
echo "==================================="

# Function to test a corrected order
test_corrected_order() {
    local order_type=$1
    local order_file=$2
    local description=$3
    
    echo ""
    echo "Testing $description"
    echo "Order Type: $order_type"
    echo "File: $order_file"
    echo "----------------------------------------"
    
    # Send order for validation (no auth required since we disabled it)
    RESPONSE=$(curl -s -X POST \
        -H "Content-Type: application/json" \
        -d @"$order_file" \
        http://localhost:3001/api/invoice/validate)
    
    echo "Response:"
    echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
    
    # Check if validation passed
    IS_VALID=$(echo "$RESPONSE" | jq -r '.is_valid' 2>/dev/null)
    if [ "$IS_VALID" = "true" ]; then
        echo "✅ Validation PASSED"
    else
        echo "❌ Validation FAILED"
    fi
}

# Check if service is running
if ! curl -s http://localhost:3001/api/protected > /dev/null 2>&1; then
    echo "Error: Service B is not running on port 3001"
    echo "Please start the service first:"
    echo "cd service-b && go run ."
    exit 1
fi

# Test Corrected Product Order
test_corrected_order "Product" "../data/corrected_product_order.json" "Corrected Product Order Validation"

# Test Corrected Service Order  
test_corrected_order "Service" "../data/corrected_service_order.json" "Corrected Service Order Validation"

# Test Corrected Reservation Order
test_corrected_order "Reservation" "../data/corrected_reservation_order.json" "Corrected Reservation Order Validation"

# Test Corrected Airlines Order
test_corrected_order "Airline" "../data/corrected_airlines_order.json" "Corrected Airlines Order Validation"

echo ""
echo "All corrected order tests completed!" 