# Coupon / Promo Code Redemption Engine — Backend

A Spring Boot REST backend for creating promo codes, redeeming them safely under concurrent requests, and viewing redemption statistics.

The service is designed for the Greenfield Coupon / Promo Code Redemption Engine assignment and keeps the promo-code logic isolated from the caller/client boundary.

## 1. Project Overview

The backend provides three REST APIs:

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/promo-codes` | Create a promo code |
| `POST` | `/promo-codes/{code}/redeem` | Redeem a promo code for a user/order |
| `GET` | `/promo-codes/{code}` | Get redemption statistics |

The implementation is expected to guarantee the following under concurrent requests:

- Total successful redemptions must never exceed `maxRedemptions`.
- A user can redeem the same promo code at most once.
- Redemption count updates must be atomic.
- The API should behave correctly when many redemption requests arrive at the same time.

## 2. Technology Stack

Based on the current `pom.xml`:

- Java 25
- Spring Boot `4.1.1`
- Spring MVC
- Maven
- H2 Database
- SpringDoc OpenAPI / Swagger UI
- Spring Boot Test
- Spring Boot Maven Plugin

### Maven coordinates

```text
Group ID:       com.assignment
Artifact ID:    coupon-promo-enginee
Version:        0.0.1-SNAPSHOT
```

## 3. Prerequisites

Install the following:

- JDK 25
- Maven 3.9+ recommended
- Git

Verify Java:

```bash
java -version
```

Expected major version:

```text
25
```

Verify Maven:

```bash
mvn -version
```

## 4. Running the Application

From the backend project directory:

```bash
mvn clean install
```

Start the application:

```bash
mvn spring-boot:run
```

By default, the Spring Boot application runs on:

```text
http://localhost:8080
```

## 5. Start the Project with Maven

Open a terminal in the backend project root, where `pom.xml` is located.

### Step 1 — Clean and build

```bash
mvn clean install
```

This will:

- Clean the previous `target` directory.
- Compile the Java source code.
- Compile the test source code.
- Run the available tests.
- Package the application.

### Step 2 — Start Spring Boot

```bash
mvn spring-boot:run
```

After successful startup, the backend is available at:

```text
http://localhost:8080
```

### Alternative — Run the packaged JAR

After:

```bash
mvn clean package
```

run the generated JAR from the `target` directory:

```bash
java -jar target/coupon-promo-enginee-0.0.1-SNAPSHOT.jar
```

> The exact JAR filename can be checked inside the `target/` directory after the Maven build.

### Step 3 — Verify the application

Open Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

## 7. Swagger / OpenAPI

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

Swagger can be used to create promo codes, redeem them, and inspect the current redemption state.

## 7. API Documentation

### 6.1 Create Promo Code

**Endpoint**

```http
POST /promo-codes
```

**Request body**

The current Swagger definition exposes:

```json
{
  "code": "SUMMER25",
  "maxRedemptions": 10,
  "currentRedemptions": 5,
  "active": true
}
```

**Example cURL**

```bash
curl -X POST "http://localhost:8080/promo-codes" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "SUMMER25",
    "maxRedemptions": 10,
    "currentRedemptions": 5,
    "active": true
  }'
```

**Observed response**

```http
HTTP/1.1 201 Created
```

Example:

```json
{
  "active": true,
  "code": "SUMMER25",
  "currentRedemptions": 0,
  "maxRedemptions": 10
}
```

> Note: The current API accepts `currentRedemptions` in the request example, but the observed create response initializes `currentRedemptions` to `0`. The actual implementation should therefore be treated as the source of truth for this behavior.

---

### 6.2 Redeem Promo Code

**Endpoint**

```http
POST /promo-codes/{code}/redeem
```

**Path parameter**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `code` | String | Yes | Promo code to redeem |

The assignment requires the redemption request to identify:

- `userId`
- `orderId`

The current Swagger/OpenAPI definition exposes the request body as a generic JSON object:

```json
{
  "additionalProp1": "string",
  "additionalProp2": "string",
  "additionalProp3": "string"
}
```

Therefore, the exact request DTO/schema should be verified against the implementation.

**Example request based on the assignment requirement:**

```json
{
  "userId": "user-1001",
  "orderId": "order-5001"
}
```

**Example cURL**

```bash
curl -X POST "http://localhost:8080/promo-codes/SUMMER25/redeem" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-1001",
    "orderId": "order-5001"
  }'
```

The current Swagger response schema is shown as:

```json
{}
```

so the exact successful response payload should be verified against the running implementation.

### Redemption rules

A successful redemption must satisfy both:

1. The promo code is active and has remaining capacity.
2. The same user has not already redeemed the same promo code.

Under concurrent requests, the capacity check and increment must happen atomically.

---

### 6.3 Get Promo Code Statistics

**Endpoint**

```http
GET /promo-codes/{code}
```

**Path parameter**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `code` | String | Yes | Promo code |

**Example cURL**

```bash
curl -X GET "http://localhost:8080/promo-codes/SUMMER25"
```

**Example response**

```json
{
  "remainingRedemptions": 10,
  "maxRedemptions": 10,
  "currentRedemptions": 0,
  "code": "SUMMER25",
  "active": true
}
```

The endpoint provides the information required by the frontend to display the current redemption state.

## 8. Concurrency and Atomicity

Concurrency correctness is a primary requirement of this assignment.

For example, if:

```text
maxRedemptions = 10
```

and 100 requests arrive concurrently, the service must not allow more than 10 successful redemptions.

The critical operation is conceptually:

```text
Check remaining capacity
        ↓
Check whether user already redeemed
        ↓
Create redemption
        ↓
Increment currentRedemptions
```

These operations must be protected as one atomic unit for a given promo code.

### Required guarantees

```text
successfulRedemptions <= maxRedemptions
```

and:

```text
same user + same promo code => maximum 1 successful redemption
```

The implementation should use an appropriate concurrency-control mechanism, such as:

- Per-promo locking
- Atomic state updates
- Thread-safe collections combined with synchronization
- Database transaction/locking if persistent storage is used

A global application-wide lock should be avoided where a finer-grained per-promo mechanism is sufficient.

## 9. Concurrent Test

The backend should include a targeted concurrency test that:

1. Creates a promo code with a small redemption limit.
2. Starts substantially more concurrent redemption attempts than the limit.
3. Uses multiple users/requests.
4. Waits for all concurrent operations to complete.
5. Verifies that the final redemption count never exceeds the configured limit.
6. Verifies that the final successful count reaches the limit when enough valid unique users are supplied.

Example test scenario:

```text
maxRedemptions = 10
concurrent requests = 100
unique users >= 10
```

Expected final state:

```text
currentRedemptions = 10
remainingRedemptions = 0
```

The important assertion is that the count must never become:

```text
11, 12, 13, ...
```

## 10. Logical Service Boundary

The assignment asks for two logical services:

```text
Cart / Checkout Service
          |
          | HTTP
          v
Promo Code Service
```

Even when both components run in the same application/process for this exercise, the code should keep the boundary clear.

### Current conceptual flow

```text
Frontend / Checkout
       |
       | POST /promo-codes/{code}/redeem
       v
Promo Code Service
       |
       +-- Validate promo code
       +-- Validate user eligibility
       +-- Perform atomic redemption
       +-- Return result
```

If these services were deployed independently, the HTTP call would cross a real network boundary and would need additional handling for:

- Timeouts
- Retry policies
- Idempotency
- Duplicate requests
- Service failures
- Observability
- Circuit breaking

## 11. Idempotency and Retry Considerations

A network retry can create a duplicate request.

For example:

```text
Checkout Service
      |
      | Redeem request
      v
Promo Service
      |
      | Redemption succeeds
      v
Network response lost
      |
      | Retry
      v
Promo Service receives request again
```

For a production implementation, an idempotency key should be associated with the redemption operation, for example:

```text
Idempotency-Key: <unique-request-id>
```

The promo service could persist the result for that key and return the same result when the same request is retried.

The current assignment does not require a production-grade distributed idempotency implementation, but this is an important consideration when the logical services become physically separate.

## 12. Data Storage

The current project includes H2 as a runtime dependency.

The assignment can be implemented with an in-memory approach for the exercise, while H2 can provide a lightweight relational database option.

For a production deployment, persistent storage should be used so that promo codes and redemptions survive application restarts.

A relational model could contain:

### Promo code

```text
promo_code
-----------
code
max_redemptions
current_redemptions
active
```

### Redemption

```text
redemption
-----------
id
code
user_id
order_id
redeemed_at
```

A unique constraint such as:

```text
(code, user_id)
```

would provide an additional database-level guarantee that a user cannot redeem the same promo code more than once.

## 13. Error Handling

The service should return appropriate HTTP responses for invalid operations, such as:

- Promo code does not exist
- Promo code is inactive
- Redemption limit has been reached
- User has already redeemed the promo code
- Invalid request data

The exact response status codes and payloads should follow the current implementation.

## 14. Testing with Maven

### Run all tests

From the project root:

```bash
mvn test
```

This compiles the test sources and executes all tests under:

```text
src/test/java
```

The current project contains:

```text
src/test/java
└── com.assignment.promoengine
    └── PromoCodeServiceApplicationTests.java
```

### Run the current test class only

```bash
mvn test -Dtest=PromoCodeServiceApplicationTests
```

### Run a specific test method

If a test method needs to be executed individually:

```bash
mvn test -Dtest=PromoCodeServiceApplicationTests#testMethodName
```

Replace `testMethodName` with the actual JUnit test method name.

### Clean and test

To remove previous build output and run the complete test suite:

```bash
mvn clean test
```

### Build the project and run tests

```bash
mvn clean install
```

`mvn clean install` performs the Maven build lifecycle, including compilation and tests, and creates the packaged application under:

```text
target/
```

### Run a future concurrency test

The assignment requires a targeted concurrent-redemption test. If a class named `PromoCodeConcurrencyTest` is added to the project, it can be executed with:

```bash
mvn test -Dtest=PromoCodeConcurrencyTest
```

The concurrency test is particularly important because correctness under concurrent load is one of the main evaluation criteria.


## 15. Project Structure

The current backend project is organized as follows:

```text
promo-code-service
│
├── src/main/java
│   └── com.assignment.promoengine
│       ├── PromoCodeServiceApplication.java
│       │
│       ├── config
│       │   └── CorsConfig.java
│       │
│       ├── controller
│       │   └── PromoCodeController.java
│       │
│       ├── model
│       │   ├── PromoCode.java
│       │   └── Redemption.java
│       │
│       ├── repository
│       │   └── PromoCodeRepository.java
│       │
│       └── service
│           ├── CheckoutClientService.java
│           └── PromoCodeService.java
│
├── src/main/resources
│
└── src/test/java
    └── com.assignment.promoengine
        └── PromoCodeServiceApplicationTests.java
```

### Layer responsibilities

```text
PromoCodeController
        ↓
PromoCodeService
        ↓
PromoCodeRepository
```

The project also contains:

- `CheckoutClientService` — represents the logical checkout/cart caller boundary.
- `CorsConfig` — contains CORS configuration.
- `PromoCode` — promo-code domain model.
- `Redemption` — redemption domain model.
- `PromoCodeServiceApplicationTests` — Spring Boot application test.


This keeps REST concerns separate from concurrency and business rules.

## 16. Design Decisions

### Thread-safe redemption

Redemption is treated as a critical section for each promo code so concurrent requests cannot oversubscribe the available capacity.

### User-level uniqueness

The redemption operation checks whether the same user has already redeemed the same promo code.

### REST separation

The API exposes independent endpoints for:

- Creating a promo code
- Redeeming a promo code
- Reading promo-code statistics

### Logical microservice boundary

The promo functionality is treated as a service that can be called by a checkout/cart component over HTTP.

### Pragmatic scope

The assignment intentionally avoids unnecessary production infrastructure such as:

- Authentication
- Full payment integration
- CI/CD
- Complex distributed deployment
- Polished UI requirements
- Production-grade distributed locking

## 17. More-Time Improvements

With additional implementation time, the following improvements could be considered:

1. PostgreSQL or another production database.
2. Database transactions and row-level locking.
3. Unique database constraint on `(promo_code, user_id)`.
4. Distributed locking using Redis/Redisson if required by the deployment model.
5. Idempotency-key persistence for retry-safe redemption.
6. Resilience4j timeout/retry/circuit-breaker handling between logical services.
7. Structured error response model.
8. Integration and end-to-end tests.
9. Metrics and tracing for redemption requests.
10. WebSocket/SSE updates for real-time frontend redemption counts.
11. Rate limiting for high-volume redemption traffic.
12. Authentication/authorization for promo-code administration.

## 18. Assignment Scope

### Included

- Promo-code creation
- Promo-code redemption
- Redemption statistics
- Concurrent redemption handling
- Duplicate-user protection
- REST APIs
- Swagger/OpenAPI documentation
- Targeted concurrency testing

### Out of Scope

- Authentication
- Real payment processing
- Real cart/checkout implementation
- CI/CD
- Production deployment infrastructure
- Polished UI
- Full distributed-service deployment

## 19. Quick API Flow

### Create

```text
POST /promo-codes
        |
        v
Promo code created
```

### Redeem

```text
POST /promo-codes/{code}/redeem
        |
        v
Validate code
        |
        v
Check user redemption
        |
        v
Atomically reserve capacity
        |
        v
Create redemption
```

### Check status

```text
GET /promo-codes/{code}
        |
        v
Current / Maximum / Remaining
```

## 20. Verification Checklist

Before submitting the assignment, verify:

- [ ] `mvn clean install` succeeds.
- [ ] Application starts successfully.
- [ ] Swagger UI opens.
- [ ] Promo code can be created.
- [ ] Promo-code statistics can be retrieved.
- [ ] Valid redemption succeeds.
- [ ] Same user cannot redeem the same code twice.
- [ ] Inactive/expired or exhausted codes cannot be redeemed.
- [ ] Concurrent redemption cannot exceed `maxRedemptions`.
- [ ] Concurrent test reaches the configured limit when enough unique users are available.
- [ ] README and test documentation are included.
- [ ] Build artifacts are excluded from the final Git repository ZIP.
- [ ] `.git` history is included if required by the assignment.

## 21. License

This project is created as an assignment submission and is not intended as a production-ready payment or promotion platform.
