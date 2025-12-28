#!/bin/bash

# Benchmark Script for service-java (JAR) vs service-java-graalvm (Native Image)
# This script measures startup time, memory usage, binary size, and performance

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get script directory (absolute path)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Configuration
JAR_SERVICE_DIR="$SCRIPT_DIR/service-java"
GRAALVM_SERVICE_DIR="$SCRIPT_DIR/service-java-graalvm"
PORT_JAR=3001
PORT_GRAALVM=3002
BASE_URL_JAR="http://localhost:${PORT_JAR}"
BASE_URL_GRAALVM="http://localhost:${PORT_GRAALVM}"
WARMUP_REQUESTS=10
BENCHMARK_REQUESTS=100
CONCURRENT_REQUESTS=10

# Results directory
RESULTS_DIR="$SCRIPT_DIR/benchmark-results"
mkdir -p "$RESULTS_DIR"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
RESULTS_FILE="$RESULTS_DIR/benchmark_${TIMESTAMP}.txt"

# Function to print section header
print_header() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}\n"
}

# Function to check if service is running
check_service() {
    local url=$1
    local max_attempts=30
    local attempt=0
    
    while [ $attempt -lt $max_attempts ]; do
        if curl -s -f "$url" > /dev/null 2>&1; then
            return 0
        fi
        attempt=$((attempt + 1))
        sleep 1
    done
    return 1
}

# Function to measure startup time
measure_startup_time() {
    local service_name=$1
    local start_cmd=$2
    local health_url=$3
    
    print_header "Measuring Startup Time: $service_name"
    
    # Kill any existing process
    pkill -f "$service_name" || true
    sleep 2
    
    # Start service and measure time
    echo "Starting $service_name..."
    start_time=$(date +%s.%N)
    eval "$start_cmd" > /tmp/${service_name}.log 2>&1 &
    local pid=$!
    
    # Wait for service to be ready
    if check_service "$health_url"; then
        end_time=$(date +%s.%N)
        startup_time=$(echo "$end_time - $start_time" | bc)
        echo -e "${GREEN}✓${NC} $service_name started in ${GREEN}${startup_time}s${NC}"
        echo "$startup_time" > "/tmp/${service_name}_startup.txt"
        echo "$pid" > "/tmp/${service_name}_pid.txt"
        return 0
    else
        echo -e "${RED}✗${NC} $service_name failed to start"
        kill $pid 2>/dev/null || true
        return 1
    fi
}

# Function to measure memory usage
measure_memory() {
    local service_name=$1
    local pid_file="/tmp/${service_name}_pid.txt"
    
    if [ ! -f "$pid_file" ]; then
        echo -e "${RED}✗${NC} PID file not found for $service_name"
        return 1
    fi
    
    local pid=$(cat "$pid_file")
    
    # Wait a bit for memory to stabilize
    sleep 5
    
    # Get memory usage (RSS in KB)
    if command -v ps > /dev/null; then
        local rss_kb=$(ps -o rss= -p "$pid" 2>/dev/null | tr -d ' ')
        if [ -n "$rss_kb" ]; then
            local rss_mb=$(echo "scale=2; $rss_kb / 1024" | bc)
            echo -e "${GREEN}✓${NC} $service_name memory: ${GREEN}${rss_mb} MB${NC} (RSS)"
            echo "$rss_mb" > "/tmp/${service_name}_memory.txt"
        fi
    fi
}

# Function to measure binary size
measure_binary_size() {
    local service_name=$1
    local binary_path=$2
    
    if [ -f "$binary_path" ]; then
        local size_bytes=$(stat -f%z "$binary_path" 2>/dev/null || stat -c%s "$binary_path" 2>/dev/null)
        local size_mb=$(echo "scale=2; $size_bytes / 1024 / 1024" | bc)
        echo -e "${GREEN}✓${NC} $service_name binary size: ${GREEN}${size_mb} MB${NC}"
        echo "$size_mb" > "/tmp/${service_name}_size.txt"
    else
        echo -e "${RED}✗${NC} Binary not found: $binary_path"
    fi
}

# Function to run load test
run_load_test() {
    local service_name=$1
    local base_url=$2
    local endpoint=$3
    
    print_header "Load Test: $service_name - $endpoint"
    
    # Warmup
    echo "Warming up ($WARMUP_REQUESTS requests)..."
    for i in $(seq 1 $WARMUP_REQUESTS); do
        curl -s "$base_url$endpoint" > /dev/null
    done
    
    # Actual benchmark
    echo "Running benchmark ($BENCHMARK_REQUESTS requests, $CONCURRENT_REQUESTS concurrent)..."
    
    if command -v ab > /dev/null; then
        ab -n $BENCHMARK_REQUESTS -c $CONCURRENT_REQUESTS "$base_url$endpoint" > "/tmp/${service_name}_ab.txt" 2>&1
        
        # Extract metrics
        local rps=$(grep "Requests per second" "/tmp/${service_name}_ab.txt" | awk '{print $4}')
        local mean_time=$(grep "Time per request.*mean" "/tmp/${service_name}_ab.txt" | head -1 | awk '{print $4}')
        local p99_time=$(grep "99%" "/tmp/${service_name}_ab.txt" | awk '{print $2}')
        
        echo -e "${GREEN}✓${NC} Requests/sec: ${GREEN}${rps}${NC}"
        echo -e "${GREEN}✓${NC} Mean latency: ${GREEN}${mean_time}ms${NC}"
        echo -e "${GREEN}✓${NC} P99 latency: ${GREEN}${p99_time}ms${NC}"
        
        echo "$rps" > "/tmp/${service_name}_rps.txt"
        echo "$mean_time" > "/tmp/${service_name}_latency.txt"
    else
        echo -e "${YELLOW}⚠${NC} Apache Bench (ab) not found. Install with: brew install httpd (macOS) or apt-get install apache2-utils (Linux)"
    fi
}

# Function to generate report
generate_report() {
    print_header "Benchmark Results Summary"
    
    {
        echo "Benchmark Results - $(date)"
        echo "=========================================="
        echo ""
        
        # Startup Time
        echo "=== STARTUP TIME ==="
        if [ -f "/tmp/jar_startup.txt" ]; then
            jar_startup=$(cat "/tmp/jar_startup.txt")
            echo "JAR: ${jar_startup}s"
        fi
        if [ -f "/tmp/native_startup.txt" ]; then
            native_startup=$(cat "/tmp/native_startup.txt")
            echo "Native: ${native_startup}s"
        fi
        if [ -f "/tmp/jar_startup.txt" ] && [ -f "/tmp/native_startup.txt" ]; then
            speedup=$(echo "scale=2; $jar_startup / $native_startup" | bc)
            echo "Speedup: ${speedup}x faster (Native)"
        fi
        echo ""
        
        # Memory Usage
        echo "=== MEMORY USAGE (RSS) ==="
        if [ -f "/tmp/jar_memory.txt" ]; then
            jar_mem=$(cat "/tmp/jar_memory.txt")
            echo "JAR: ${jar_mem} MB"
        fi
        if [ -f "/tmp/native_memory.txt" ]; then
            native_mem=$(cat "/tmp/native_memory.txt")
            echo "Native: ${native_mem} MB"
        fi
        if [ -f "/tmp/jar_memory.txt" ] && [ -f "/tmp/native_memory.txt" ]; then
            reduction=$(echo "scale=2; (($jar_mem - $native_mem) / $jar_mem) * 100" | bc)
            echo "Reduction: ${reduction}% (Native)"
        fi
        echo ""
        
        # Binary Size
        echo "=== BINARY SIZE ==="
        if [ -f "/tmp/jar_size.txt" ]; then
            jar_size=$(cat "/tmp/jar_size.txt")
            echo "JAR: ${jar_size} MB"
        fi
        if [ -f "/tmp/native_size.txt" ]; then
            native_size=$(cat "/tmp/native_size.txt")
            echo "Native: ${native_size} MB"
        fi
        echo ""
        
        # Performance
        echo "=== PERFORMANCE ==="
        if [ -f "/tmp/jar_rps.txt" ]; then
            jar_rps=$(cat "/tmp/jar_rps.txt")
            echo "JAR Requests/sec: ${jar_rps}"
        fi
        if [ -f "/tmp/native_rps.txt" ]; then
            native_rps=$(cat "/tmp/native_rps.txt")
            echo "Native Requests/sec: ${native_rps}"
        fi
        if [ -f "/tmp/jar_rps.txt" ] && [ -f "/tmp/native_rps.txt" ]; then
            improvement=$(echo "scale=2; (($native_rps - $jar_rps) / $jar_rps) * 100" | bc)
            echo "Improvement: ${improvement}% (Native)"
        fi
        echo ""
        
    } | tee "$RESULTS_FILE"
    
    echo -e "\n${GREEN}Full report saved to: ${RESULTS_FILE}${NC}"
}

# Function to cleanup
cleanup() {
    echo -e "\n${YELLOW}Cleaning up...${NC}"
    
    if [ -f "/tmp/jar_pid.txt" ]; then
        kill $(cat "/tmp/jar_pid.txt") 2>/dev/null || true
    fi
    if [ -f "/tmp/native_pid.txt" ]; then
        kill $(cat "/tmp/native_pid.txt") 2>/dev/null || true
    fi
    
    pkill -f "service-java" || true
    sleep 2
}

# Trap to cleanup on exit
trap cleanup EXIT

# Main execution
main() {
    print_header "GraalVM Native Image vs JAR Benchmark"
    
    # Check prerequisites
    if ! command -v bc > /dev/null; then
        echo -e "${RED}✗${NC} bc (calculator) not found. Install with: brew install bc (macOS)"
        exit 1
    fi
    
    # Check if services are built
    JAR_PATH="$JAR_SERVICE_DIR/target/service-java-1.0.0.jar"
    NATIVE_PATH="$GRAALVM_SERVICE_DIR/target/service-java-graalvm"
    
    if [ ! -f "$JAR_PATH" ]; then
        echo -e "${YELLOW}⚠${NC} JAR not found. Building..."
        (cd "$JAR_SERVICE_DIR" && mvn clean package -DskipTests)
    fi
    
    if [ ! -f "$NATIVE_PATH" ]; then
        echo -e "${YELLOW}⚠${NC} Native binary not found. Building..."
        echo -e "${YELLOW}This may take several minutes...${NC}"
        (cd "$GRAALVM_SERVICE_DIR" && mvn clean native:compile)
    fi
    
    # Measure binary sizes
    print_header "Binary Size Comparison"
    measure_binary_size "jar" "$JAR_PATH"
    measure_binary_size "native" "$NATIVE_PATH"
    
    # Benchmark JAR
    print_header "Benchmarking JAR Service"
    measure_startup_time "jar" "cd $JAR_SERVICE_DIR && java -jar target/service-java-1.0.0.jar --server.port=$PORT_JAR" "$BASE_URL_JAR/health"
    measure_memory "jar"
    run_load_test "jar" "$BASE_URL_JAR" "/health"
    
    # Stop JAR service
    if [ -f "/tmp/jar_pid.txt" ]; then
        kill $(cat "/tmp/jar_pid.txt") 2>/dev/null || true
        sleep 3
    fi
    
    # Benchmark Native
    print_header "Benchmarking Native Image Service"
    measure_startup_time "native" "$NATIVE_PATH --server.port=$PORT_GRAALVM" "$BASE_URL_GRAALVM/health"
    measure_memory "native"
    run_load_test "native" "$BASE_URL_GRAALVM" "/health"
    
    # Generate report
    generate_report
    
    print_header "Benchmark Complete!"
    echo -e "Results saved to: ${GREEN}${RESULTS_FILE}${NC}"
}

# Run main function
main

