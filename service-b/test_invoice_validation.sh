#!/bin/bash

# Test script for invoice validation service
# Make sure service-b is running on port 3001

echo "Testing Invoice Validation Service"
echo "=================================="

# Function to test an order
test_order() {
    local order_type=$1
    local order_file=$2
    local description=$3
    
    echo ""
    echo "Testing $description"
    echo "Order Type: $order_type"
    echo "File: $order_file"
    echo "----------------------------------------"
    
    # Get JWT token first
    TOKEN=$(curl -s -X GET http://localhost:3001/call-a | jq -r '.token' 2>/dev/null || echo "")
    
    if [ -z "$TOKEN" ]; then
        echo "Failed to get JWT token"
        return 1
    fi
    
    # Send order for validation
    RESPONSE=$(curl -s -X POST \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d @"$order_file" \
        http://localhost:3001/api/invoice/validate)
    
    echo "Response:"
    echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
}

# Check if service is running
if ! curl -s http://localhost:3001/api/protected > /dev/null 2>&1; then
    echo "Error: Service B is not running on port 3001"
    echo "Please start the service first:"
    echo "cd service-b && go run ."
    exit 1
fi

# Test Product Order
test_order "Product" "../data/product_order.json" "Product Order Validation"

# Test Service Order  
test_order "Service" "../data/service_order.json" "Service Order Validation"

# Test Reservation Order
test_order "Reservation" "../data/reservation_order.json" "Reservation Order Validation"

# Test Airlines Order
test_order "Airline" "../data/airlines_order.json" "Airlines Order Validation"

echo ""
echo "All tests completed!" 