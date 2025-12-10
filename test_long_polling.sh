#!/bin/bash

# Test script for long polling
echo "Starting long polling test..."

# Start a new search
echo "1. Starting new search..."
SEARCH_RESPONSE=$(curl -s -X GET "http://localhost:3001/api/search?from=JAK&to=CGK" -H "Content-Type: application/json")
QUERY_ID=$(echo $SEARCH_RESPONSE | grep -o '"query_id":"[^"]*"' | cut -d'"' -f4)

echo "Query ID: $QUERY_ID"
echo ""

# Poll until completion
echo "2. Starting long polling..."
POLL_COUNT=0
MAX_POLLS=20

while [ $POLL_COUNT -lt $MAX_POLLS ]; do
    echo "Poll #$((POLL_COUNT + 1))..."
    
    RESPONSE=$(curl -s -X GET "http://localhost:3001/api/result/longpoll?query_id=$QUERY_ID" -H "Content-Type: application/json")
    
    # Extract key fields
    TYPE=$(echo $RESPONSE | grep -o '"type":"[^"]*"' | cut -d'"' -f4)
    PROGRESS=$(echo $RESPONSE | grep -o '"progress":[0-9]*' | cut -d':' -f2)
    STATUS=$(echo $RESPONSE | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
    MESSAGE=$(echo $RESPONSE | grep -o '"message":"[^"]*"' | cut -d'"' -f4)
    RECEIVED=$(echo $RESPONSE | grep -o '"received_flights":[0-9]*' | cut -d':' -f2)
    TOTAL=$(echo $RESPONSE | grep -o '"total_expected":[0-9]*' | cut -d':' -f2)
    
    echo "  Type: $TYPE"
    echo "  Progress: $PROGRESS%"
    echo "  Status: $STATUS"
    echo "  Message: $MESSAGE"
    echo "  Flights: $RECEIVED/$TOTAL"
    echo ""
    
    # Check if completed
    if [ "$TYPE" = "completed" ] || [ "$STATUS" = "completed" ]; then
        echo "✅ Search completed!"
        break
    fi
    
    # Check if we should continue
    if [ "$TYPE" = "timeout" ]; then
        echo "⏰ Timeout reached, continuing to poll..."
        sleep 2
    fi
    
    POLL_COUNT=$((POLL_COUNT + 1))
    sleep 1
done

if [ $POLL_COUNT -eq $MAX_POLLS ]; then
    echo "❌ Max polls reached without completion"
fi

echo "Test completed." 