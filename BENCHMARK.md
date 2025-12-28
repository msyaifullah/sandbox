# Benchmark Guide: JAR vs GraalVM Native Image

This guide explains how to benchmark and compare the performance of the regular JAR (`service-java`) and the GraalVM Native Image (`service-java-graalvm`) versions of the service.

## Metrics Measured

1. **Startup Time**: Time from process start to first successful HTTP response
2. **Memory Usage**: Resident Set Size (RSS) in MB
3. **Binary Size**: Size of the executable/JAR file
4. **Throughput**: Requests per second (RPS)
5. **Latency**: Mean and P99 response times

## Prerequisites

### Java Version

Both services now require **Java 25** (or GraalVM 25 for native image builds).

**Install GraalVM 25:**
```bash
# Using the provided script
./install-java25.sh

# Or manually with SDKMAN
source ~/.sdkman/bin/sdkman-init.sh
sdk install java 25.0.1-graalce
sdk use java 25.0.1-graalce
gu install native-image
```

**Note**: Java 25.0.1 is the latest version and is required for Spring Boot 4.0.1 native image support.

### Required Tools

1. **bc** (calculator) - For calculations
   ```bash
   # macOS
   brew install bc
   
   # Linux (Ubuntu/Debian)
   sudo apt-get install bc
   ```

2. **Apache Bench (ab)** - For load testing
   ```bash
   # macOS
   brew install httpd
   
   # Linux (Ubuntu/Debian)
   sudo apt-get install apache2-utils
   ```

3. **wrk** (optional) - Advanced load testing
   ```bash
   # macOS
   brew install wrk
   
   # Linux (Ubuntu/Debian)
   sudo apt-get install wrk
   ```

4. **curl** - Usually pre-installed

### Build Both Services

Before benchmarking, ensure both services are built:

```bash
# Build JAR service
cd service-java
mvn clean package -DskipTests
cd ..

# Build Native Image (takes 5-10 minutes)
cd service-java-graalvm
mvn clean native:compile
cd ..
```

## Running Benchmarks

### Quick Benchmark (Automated)

Run the comprehensive benchmark script that measures all metrics:

```bash
chmod +x benchmark.sh
./benchmark.sh
```

This script will:
1. Build services if needed
2. Measure binary sizes
3. Start each service and measure startup time
4. Measure memory usage
5. Run load tests
6. Generate a comparison report

**Output**: Results are saved to `benchmark-results/benchmark_YYYYMMDD_HHMMSS.txt`

### Manual Benchmarking

#### 1. Measure Startup Time

**JAR Service:**
```bash
cd service-java
time java -jar target/service-java-1.0.0.jar --server.port=3001
# Press Ctrl+C after service starts
```

**Native Image:**
```bash
cd service-java-graalvm
time ./target/service-java-graalvm --server.port=3002
# Press Ctrl+C after service starts
```

#### 2. Measure Memory Usage

Start the service, then in another terminal:

```bash
# Find the process ID
ps aux | grep service-java

# Check memory (RSS in KB, convert to MB)
ps -o pid,rss,comm -p <PID>
```

Or use the benchmark script which does this automatically.

#### 3. Measure Binary Size

```bash
# JAR size
ls -lh service-java/target/service-java-1.0.0.jar

# Native binary size
ls -lh service-java-graalvm/target/service-java-graalvm
```

#### 4. Load Testing

**Using the load test script:**
```bash
chmod +x benchmark-load-test.sh

# Test health endpoint
./benchmark-load-test.sh http://localhost:3001 /health 1000 50

# Test invoice validation
./benchmark-load-test.sh http://localhost:3001 /api/invoice/validate 100 10
```

**Using Apache Bench directly:**
```bash
# Health endpoint
ab -n 1000 -c 50 http://localhost:3001/health

# Invoice validation (requires payload file)
ab -n 100 -c 10 -p invoice-payload.json -T application/json \
   http://localhost:3001/api/invoice/validate
```

**Using wrk:**
```bash
# Health endpoint
wrk -t4 -c50 -d30s --latency http://localhost:3001/health

# With Lua script for POST requests
wrk -t4 -c50 -d30s -s invoice-test.lua http://localhost:3001/api/invoice/validate
```

## Expected Results

Based on typical GraalVM Native Image benchmarks:

### Startup Time
- **JAR**: 2-5 seconds
- **Native**: 0.05-0.2 seconds
- **Improvement**: 10-100x faster

### Memory Usage
- **JAR**: 150-300 MB (RSS)
- **Native**: 50-100 MB (RSS)
- **Improvement**: 50-70% reduction

### Binary Size
- **JAR**: ~50-80 MB
- **Native**: ~80-150 MB
- **Note**: Native includes everything, JAR requires JVM

### Throughput
- **JAR**: Baseline
- **Native**: Similar or slightly better (5-15% improvement)
- **Note**: Throughput improvements are usually modest

### Latency
- **JAR**: Baseline
- **Native**: Similar or slightly better (lower P99)

## Benchmarking Specific Endpoints

### Health Endpoint
```bash
# Simple GET request, good for baseline
ab -n 10000 -c 100 http://localhost:3001/health
```

### Invoice Validation
Create a test payload file `invoice-payload.json`:
```json
{
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
}
```

Then test:
```bash
ab -n 1000 -c 50 -p invoice-payload.json \
   -T application/json \
   http://localhost:3001/api/invoice/validate
```

### Flight Search
```bash
# Start search
curl "http://localhost:3001/api/search?from=CGK&to=DPS"

# Use the query_id from response for streaming endpoints
```

## Continuous Benchmarking

For CI/CD integration, you can run benchmarks automatically:

```bash
# Run benchmark and save results
./benchmark.sh > benchmark-output.txt 2>&1

# Extract specific metrics
grep "Speedup" benchmark-results/benchmark_*.txt
grep "Reduction" benchmark-results/benchmark_*.txt
```

## Troubleshooting

### Service Won't Start
- Check if port is already in use: `lsof -i :3001`
- Check logs: `tail -f /tmp/jar.log` or `tail -f /tmp/native.log`
- Ensure Redis is running (required for the service)

### Benchmark Script Fails
- Ensure both services are built
- Check that `bc` is installed
- Verify services can start manually first

### Memory Measurements Seem Off
- Memory usage stabilizes after a few seconds
- Run multiple measurements and average
- Consider using `jstat` for JVM heap details (JAR only)

### Load Test Results Vary
- Run multiple times and average
- Ensure no other processes are consuming resources
- Use a dedicated machine for accurate results

## Advanced Benchmarking

### CPU Profiling

**JAR (using JVM tools):**
```bash
# Start with profiling
java -jar -XX:+UnlockDiagnosticVMOptions \
     -XX:+LogCompilation \
     target/service-java-1.0.0.jar

# Use JProfiler, VisualVM, or async-profiler
```

**Native Image:**
```bash
# Use perf (Linux)
perf record -g ./target/service-java-graalvm
perf report

# Or use dtrace (macOS)
sudo dtrace -n 'profile-997 /execname == "service-java-graalvm"/ { @[ustack()] = count(); }'
```

### Memory Profiling

**JAR:**
```bash
# Heap dump
jmap -dump:format=b,file=heap.hprof <PID>

# Analyze with Eclipse MAT or VisualVM
```

**Native Image:**
```bash
# Use Valgrind (Linux)
valgrind --tool=massif ./target/service-java-graalvm

# Or use Instruments (macOS)
instruments -t "Allocations" ./target/service-java-graalvm
```

## Interpreting Results

### When to Use Native Image
- **Cold start is critical**: Serverless, containers, CLI tools
- **Memory is constrained**: Edge devices, embedded systems
- **Startup time matters**: Microservices with frequent restarts

### When to Use JAR
- **Development speed**: Faster build times
- **Dynamic features needed**: Runtime code generation, dynamic classloading
- **Compatibility concerns**: Some libraries may not work with Native Image

## Further Reading

- [GraalVM Native Image Performance](https://www.graalvm.org/latest/reference-manual/native-image/overview/NativeImagePerformance/)
- [Spring Boot Native Image](https://docs.spring.io/spring-boot/docs/current/reference/html/native-image.html)
- [Apache Bench Documentation](https://httpd.apache.org/docs/2.4/programs/ab.html)
- [wrk Documentation](https://github.com/wg/wrk)

