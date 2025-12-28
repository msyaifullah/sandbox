#!/bin/bash

# Advanced Load Testing Script
# Tests specific endpoints with realistic payloads

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
BASE_URL=${1:-"http://localhost:3001"}
ENDPOINT=${2:-"/health"}
REQUESTS=${3:-1000}
CONCURRENT=${4:-50}
DURATION=${5:-60}  # seconds

print_header() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}\n"
}

# Check if service is ready
wait_for_service() {
    local url=$1
    local max_attempts=30
    local attempt=0
    
    echo "Waiting for service to be ready..."
    while [ $attempt -lt $max_attempts ]; do
        if curl -s -f "$url" > /dev/null 2>&1; then
            echo -e "${GREEN}✓${NC} Service is ready"
            return 0
        fi
        attempt=$((attempt + 1))
        sleep 1
    done
    echo -e "${YELLOW}✗${NC} Service not ready"
    return 1
}

# Run load test with Apache Bench
run_ab_test() {
    local url=$1
    local requests=$2
    local concurrent=$3
    
    print_header "Apache Bench Load Test"
    echo "URL: $url"
    echo "Requests: $requests"
    echo "Concurrent: $concurrent"
    echo ""
    
    if command -v ab > /dev/null; then
        ab -n $requests -c $concurrent -g /tmp/ab_results.tsv "$url"
        echo ""
        echo -e "${GREEN}Results saved to: /tmp/ab_results.tsv${NC}"
    else
        echo -e "${YELLOW}⚠${NC} Apache Bench (ab) not found"
        echo "Install with: brew install httpd (macOS) or apt-get install apache2-utils (Linux)"
    fi
}

# Run load test with wrk (if available)
run_wrk_test() {
    local url=$1
    local duration=$2
    local threads=$3
    local connections=$4
    
    print_header "wrk Load Test"
    echo "URL: $url"
    echo "Duration: ${duration}s"
    echo "Threads: $threads"
    echo "Connections: $connections"
    echo ""
    
    if command -v wrk > /dev/null; then
        wrk -t$threads -c$connections -d${duration}s --latency "$url"
    else
        echo -e "${YELLOW}⚠${NC} wrk not found"
        echo "Install with: brew install wrk (macOS) or apt-get install wrk (Linux)"
    fi
}

# Test invoice validation endpoint
test_invoice_endpoint() {
    local base_url=$1
    
    print_header "Testing Invoice Validation Endpoint"
    
    # Sample invoice payload
    local invoice_payload='{
        "order_type": "Product",
        "order_id": "order-123",
        "customer": {
            "customer_id": "cust-001",
            "name": "John Doe",
            "email": "john@example.com",
            "phone": "+1234567890"
        },
        "items": [
            {
                "item_id": "item-1",
                "name": "Product A",
                "quantity": 2,
                "price_info": {
                    "base_price": 100000,
                    "discount": 0,
                    "tax": 11000,
                    "shipping": 15000
                }
            }
        ],
        "transaction_details": {
            "total_amount": 226000,
            "currency": "IDR"
        }
    }'
    
    echo "Testing POST /api/invoice/validate..."
    
    if command -v curl > /dev/null; then
        local response=$(curl -s -w "\n%{http_code}\n%{time_total}" \
            -X POST \
            -H "Content-Type: application/json" \
            -d "$invoice_payload" \
            "$base_url/api/invoice/validate")
        
        local http_code=$(echo "$response" | tail -2 | head -1)
        local time_total=$(echo "$response" | tail -1)
        
        if [ "$http_code" = "200" ]; then
            echo -e "${GREEN}✓${NC} Request successful (${time_total}s)"
        else
            echo -e "${YELLOW}⚠${NC} HTTP $http_code (${time_total}s)"
        fi
    fi
}

# Main execution
main() {
    local full_url="${BASE_URL}${ENDPOINT}"
    
    print_header "Load Testing: $full_url"
    
    # Wait for service
    if ! wait_for_service "$BASE_URL/health"; then
        echo "Service not available. Exiting."
        exit 1
    fi
    
    # Warmup
    echo "Warming up..."
    for i in {1..10}; do
        curl -s "$full_url" > /dev/null
    done
    
    # Run tests
    if [ "$ENDPOINT" = "/api/invoice/validate" ]; then
        test_invoice_endpoint "$BASE_URL"
    else
        run_ab_test "$full_url" "$REQUESTS" "$CONCURRENT"
        
        if command -v wrk > /dev/null; then
            echo ""
            run_wrk_test "$full_url" "$DURATION" 4 "$CONCURRENT"
        fi
    fi
    
    print_header "Load Test Complete"
}

main

