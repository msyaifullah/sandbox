# Service Java - Spring Boot Backend

This is a Spring Boot service with Maven that provides the same functionality as service-b (Go service). It includes invoice validation for multiple order types and flight search with real-time streaming capabilities.

## Features

### Invoice Validation
- **Product Orders**: E-commerce orders with multiple merchants, items, shipping, and discounts
- **Service Orders**: Service bookings with providers, addons, and scheduling
- **Reservation Orders**: Hotel, restaurant, venue, and activity bookings
- **Airlines Orders**: Flight bookings with passenger information and pricing

### Flight Search
- REST API for initiating and canceling searches
- WebSocket streaming for real-time flight results
- Server-Sent Events (SSE) for progressive updates
- Long polling endpoint for compatibility
- Redis pub/sub for real-time message distribution

### Authentication
- JWT token creation and verification
- RSA key-based authentication

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Redis server running on localhost:6379 (password: changeMe123)

## Configuration

### Profiles

The service supports three profiles: `dev`, `stg` (staging), and `prd` (production).

- **Development (`dev`)**: Default profile for local development
  - Redis: localhost:6379
  - CORS: Allows localhost origins
  - Logging: DEBUG level
  - JWT keys: Local files

- **Staging (`stg`)**: For staging environment
  - Redis: Configurable via environment variables
  - CORS: Staging domain origins
  - Logging: INFO level
  - JWT keys: Configurable paths

- **Production (`prd`)**: For production environment
  - Redis: Production server (via environment variables)
  - CORS: Production domain origins
  - Logging: WARN/INFO level with file logging
  - JWT keys: Secure paths
  - Compression enabled

### Setting Active Profile

**Option 1: Environment Variable**
```bash
export SPRING_PROFILES_ACTIVE=prd
mvn spring-boot:run
```

**Option 2: Command Line Argument**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=stg
```

**Option 3: JAR with Profile**
```bash
java -jar target/service-java-1.0.0.jar --spring.profiles.active=prd
```

**Option 4: System Property**
```bash
java -Dspring.profiles.active=prd -jar target/service-java-1.0.0.jar
```

### Environment Variables for Production/Staging

The staging and production profiles support environment variables:

- `REDIS_HOST`: Redis server hostname
- `REDIS_PORT`: Redis server port (default: 6379)
- `REDIS_PASSWORD`: Redis password
- `REDIS_SSL_ENABLED`: Enable SSL for Redis (default: false)
- `CORS_ORIGINS`: Comma-separated list of allowed origins
- `JWT_PRIVATE_KEY_PATH`: Path to private key file
- `JWT_PUBLIC_KEY_PATH`: Path to public key file
- `SERVER_PORT`: Server port (default: 3001)
- `LOG_FILE_PATH`: Log file path for production

### Configuration Files

- `application.properties`: Default/base configuration
- `application-dev.properties`: Development profile
- `application-stg.properties`: Staging profile
- `application-prd.properties`: Production profile

## Building and Running

### Build the project:
```bash
cd service-java
mvn clean package
```

### Run the application:

**Development (default):**
```bash
mvn spring-boot:run
```

**Staging:**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=stg
```

**Production:**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=prd
```

**Or run the JAR with profile:**
```bash
# Development
java -jar target/service-java-1.0.0.jar --spring.profiles.active=dev

# Staging
java -jar target/service-java-1.0.0.jar --spring.profiles.active=stg

# Production
java -jar target/service-java-1.0.0.jar --spring.profiles.active=prd
```

The service will start on port 3001 (or the port specified in the profile).

## API Endpoints

### Invoice Validation

#### POST /api/invoice/validate
Validates invoice data for any supported order type.

**Request Body:** JSON with one of: `ProductOrder`, `ServiceOrder`, `ReservationOrder`, or `AirlinesOrder`

**Response:**
```json
{
  "is_valid": true,
  "order_type": "Product",
  "order_id": "order-123",
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

### Flight Search

#### GET /api/search?from=CGK&to=DPS
Initiates a flight search and returns a query ID and WebSocket URL.

**Response:**
```json
{
  "query_id": "abc123...",
  "ws_url": "ws://localhost:3001/ws/result/stream?query_id=abc123..."
}
```

#### POST /api/search/cancel?query_id=abc123
Cancels an active flight search.

#### GET /api/result/sse?query_id=abc123
Server-Sent Events endpoint for streaming flight results.

#### GET /api/result/longpoll?query_id=abc123
Long polling endpoint for flight results (5 minute timeout).

#### WebSocket: ws://localhost:3001/ws/result/stream?query_id=abc123
WebSocket endpoint for real-time flight results.

### Legacy Endpoints

#### GET /api/protected
Protected endpoint requiring JWT authentication.

**Headers:**
- `Authorization: Bearer <token>`

#### GET /call-a
Creates and returns a JWT token for calling service-a.

## Project Structure

```
service-java/
├── src/
│   ├── main/
│   │   ├── java/com/kjl/servicejava/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── model/            # Data models
│   │   │   ├── service/          # Business logic
│   │   │   ├── util/             # Utility classes
│   │   │   └── ServiceJavaApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/                     # Test files
├── pom.xml
└── README.md
```

## Dependencies

- Spring Boot 3.2.0
- Spring Web (REST API)
- Spring WebSocket
- Spring Data Redis
- Jackson (JSON processing)
- JJWT (JWT handling)
- Lombok (reduces boilerplate)

## Testing

### Test Invoice Validation:
```bash
curl -X POST http://localhost:3001/api/invoice/validate \
  -H "Content-Type: application/json" \
  -d @../data/product_order.json
```

### Test Flight Search:
```bash
# Start search
curl "http://localhost:3001/api/search?from=CGK&to=DPS"

# Use the query_id from response to connect via WebSocket or SSE
```

## Notes

- The service requires Redis to be running for flight search functionality
- JWT keys (serviceB_private.pem and serviceA_public.pem) should be placed in the project root for development
- For production/staging, configure JWT key paths via environment variables
- CORS is configured per profile:
  - Development: localhost origins
  - Staging: Staging domain origins (configurable)
  - Production: Production domain origins (configurable)
- The service runs on port 3001 by default to match service-b
- Use environment variables in production/staging for sensitive configuration

## Differences from service-b (Go)

- Uses Spring Boot instead of Gin framework
- Uses Spring Data Redis instead of go-redis
- Uses JJWT instead of golang-jwt
- Uses Jackson for JSON processing
- WebSocket implementation uses Spring WebSocket instead of Gorilla WebSocket
