# Design Patterns in Service Java

This document describes the design patterns implemented in the service-java project.

## 1. Strategy Pattern

**Location:** `validation/` package

**Purpose:** Encapsulates different validation algorithms for different order types.

**Implementation:**
- `OrderValidator` interface defines the contract for all validators
- `AbstractOrderValidator` provides common validation logic (Template Method)
- Concrete validators: `ProductOrderValidator`, `ServiceOrderValidator`, `ReservationOrderValidator`, `AirlinesOrderValidator`

**Benefits:**
- Easy to add new order types without modifying existing code
- Each validator can have its own validation logic
- Follows Open/Closed Principle

## 2. Factory Pattern

**Location:** `validation/OrderValidatorFactory.java`

**Purpose:** Creates appropriate validator instances based on order type.

**Implementation:**
- `OrderValidatorFactory` automatically discovers all validators via Spring DI
- Maps order types to their corresponding validators
- Provides `getValidator(String orderType)` method

**Benefits:**
- Centralized validator creation
- Decouples client code from concrete validator classes
- Easy to extend with new validators

## 3. Template Method Pattern

**Location:** `validation/AbstractOrderValidator.java`

**Purpose:** Defines the skeleton of the validation algorithm in the base class.

**Implementation:**
- `AbstractOrderValidator` defines the validation flow
- Subclasses implement specific steps (`validateOrderItems`)
- Common operations (total validation, summary population) are in base class

**Benefits:**
- Code reuse
- Consistent validation flow across all order types
- Easy to modify common behavior

## 4. Builder Pattern

**Location:** `dto/ValidationResponseBuilder.java`

**Purpose:** Constructs complex `InvoiceValidationResponse` objects step by step.

**Implementation:**
- Fluent API for building responses
- `ValidationResponseBuilder.create()` starts the builder
- Methods like `withOrderType()`, `withError()`, `withWarning()` chain together
- `build()` returns the final object

**Benefits:**
- More readable code
- Flexible object construction
- Immutable objects can be built

## 5. Repository Pattern

**Location:** `repository/RedisRepository.java`

**Purpose:** Abstracts data access logic for Redis operations.

**Implementation:**
- `RedisRepository` encapsulates all Redis operations
- Provides high-level methods: `set()`, `get()`, `delete()`, `publish()`
- Domain-specific methods: `setSearchResult()`, `getProgress()`, `setProgress()`

**Benefits:**
- Separation of concerns
- Easy to test (can mock repository)
- Can switch Redis implementation without changing business logic
- Centralized data access logic

## 6. Dependency Injection

**Location:** Throughout the application

**Purpose:** Inversion of Control for managing dependencies.

**Implementation:**
- Spring's `@Autowired` annotation
- Constructor injection (preferred)
- Field injection (where appropriate)

**Benefits:**
- Loose coupling
- Easy testing with mocks
- Centralized configuration

## 7. Exception Handling Pattern

**Location:** `exception/` package

**Purpose:** Centralized exception handling with custom exceptions.

**Implementation:**
- Custom exceptions: `OrderValidationException`, `InvalidOrderTypeException`
- `GlobalExceptionHandler` with `@RestControllerAdvice`
- Consistent error responses

**Benefits:**
- Consistent error handling
- Better error messages
- Separation of error handling logic

## 8. Service Layer Pattern

**Location:** `service/` package

**Purpose:** Business logic encapsulation.

**Implementation:**
- `InvoiceValidationService`: Orchestrates validation using Strategy and Factory
- `FlightSearchService`: Manages flight search operations
- Services use repositories for data access

**Benefits:**
- Business logic separated from controllers
- Reusable across different controllers
- Easier to test

## Architecture Overview

```
Controller Layer (REST endpoints)
    ↓
Service Layer (Business logic)
    ↓
Strategy/Factory (Validation logic)
    ↓
Repository Layer (Data access)
    ↓
Redis/External Systems
```

## Benefits of This Design

1. **Maintainability:** Clear separation of concerns
2. **Testability:** Each layer can be tested independently
3. **Extensibility:** Easy to add new order types or features
4. **Scalability:** Patterns support growth
5. **Readability:** Code is self-documenting through patterns

## Adding a New Order Type

1. Create a new validator implementing `OrderValidator` or extending `AbstractOrderValidator`
2. Annotate with `@Component` - Factory will auto-discover it
3. Implement `getOrderType()` and `validateOrderItems()` methods
4. Update `InvoiceValidationService.parseOrder()` to handle the new type

No other changes needed!
