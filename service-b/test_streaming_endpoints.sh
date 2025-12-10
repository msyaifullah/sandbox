#!/bin/bash

# Test script for streaming endpoints (WebSocket, Long Polling, SSE)
# Make sure service-b is running on port 3001

echo "=== Flight Search Streaming Endpoints Test ==="
echo

# Start a search
echo "1. Starting a flight search..."
SEARCH_RESPONSE=$(curl -s "http://localhost:3001/api/search?from=JAK&to=CGK")
echo "Search Response: $SEARCH_RESPONSE"

# Extract query_id from response
QUERY_ID=$(echo $SEARCH_RESPONSE | grep -o '"query_id":"[^"]*"' | cut -d'"' -f4)
echo "Query ID: $QUERY_ID"
echo

if [ -z "$QUERY_ID" ]; then
    echo "Error: Could not extract query_id from response"
    exit 1
fi

echo "2. Testing Long Polling endpoint..."
echo "Long Poll URL: http://localhost:3001/api/result/longpoll?query_id=$QUERY_ID"
echo "Press Ctrl+C to stop long polling test"
echo

# Test long polling (will timeout after 30 seconds)
curl -s "http://localhost:3001/api/result/longpoll?query_id=$QUERY_ID" | jq '.'

echo
echo "3. Testing Server-Sent Events endpoint..."
echo "SSE URL: http://localhost:3001/api/result/sse?query_id=$QUERY_ID"
echo "Press Ctrl+C to stop SSE test"
echo

# Test SSE (will timeout after 30 seconds)
curl -s "http://localhost:3001/api/result/sse?query_id=$QUERY_ID"

echo
echo "4. Testing WebSocket endpoint..."
echo "WebSocket URL: ws://localhost:3001/ws/result/stream?query_id=$QUERY_ID"
echo "Use a WebSocket client to connect to the above URL"
echo

echo "=== Test completed ==="
echo "Note: Each endpoint will receive the same flight data from Redis pubsub"
echo "The data includes progress updates and flight results as they become available" 