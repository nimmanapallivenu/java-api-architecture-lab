# Module 02: REST API with Reactive WebClient

A REST API implementation demonstrating **async non-blocking I/O** for downstream service calls using Spring MVC with WebClient.

## 🎯 Key Difference from Module 01

| Aspect | Module 01 | Module 02 |
|--------|-----------|----------|
| **Framework** | Spring MVC | Spring MVC |
| **HTTP Calls** | Blocking (RestTemplate or sync WebClient) | Non-blocking (WebClient) |
| **Threading** | 1 thread per request (all waiting) | Thread pool + non-blocking I/O |
| **Concurrency** | ~200 concurrent users | ~1000+ concurrent users |
| **Downstream Calls** | Block the request thread | Async/non-blocking |
| **Notifications** | Synchronous | Fire-and-forget async |
| **Response Time** | Slow (waits for all services) | Fast (returns while services process) |

## 🚀 What This Module Demonstrates

### 1. **WebClient vs RestTemplate**

**Module 01 (RestTemplate - Blocking)**:
```java
// Thread is BLOCKED waiting for response
String response = restTemplate.getForObject(url, String.class);
```

**Module 02 (WebClient - Non-blocking)**:
```java
// Thread is NOT blocked, returns immediately
webClient.get()
    .uri(url)
    .retrieve()
    .bodyToMono(String.class)
    .subscribe(response -> handleResponse(response));
```

### 2. **Async Processing with CompletableFuture**

```java
// Returns CompletableFuture that completes when response arrives
public CompletableFuture<PaymentDto> processPaymentAsync(Long orderId, PaymentDto payment) {
    return webClient.post()
            .uri(paymentServiceUrl + "/api/process-payment")
            .bodyValue(payment)
            .retrieve()
            .bodyToMono(PaymentDto.class)
            .toFuture();  // Convert Mono to CompletableFuture
}
```

### 3. **Fire-and-Forget Notifications**

```java
// Sends notification WITHOUT waiting for response
public void sendNotificationAsync(String email, String message) {
    webClient.post()
            .uri(notificationServiceUrl + "/api/send")
            .bodyValue(notification)
            .retrieve()
            .toBodilessEntity()
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();  // Fire and forget
}
```

### 4. **@Async Background Tasks**

```java
@Async
protected void processPaymentAsync(OrderEntity order, PaymentEntity payment) {
    // Runs in separate thread pool, doesn't block main request
    paymentServiceClient.processPaymentAsync(...)
        .thenAccept(result -> updateOrder(result));
}
```

## 📊 Architecture

```
┌─────────────────────┐
│   HTTP Request      │
└──────────┬──────────┘
           │
┌──────────▼──────────────────────┐
│    @RestController               │  ← Still blocking
│  (Fast response - ~50ms)         │
│  - Validates input               │
│  - Creates order                 │
│  - Returns immediately           │
└──────────┬──────────────────────┘
           │
┌──────────▼──────────────────────┐
│    @Service (Async)              │  ← Now async!
│  - Calls WebClient               │
│  - Uses CompletableFuture        │
│  - Doesn't block request thread  │
└──────────┬──────────────────────┘
           │
┌──────────┴──────────────────────┐
│      Non-blocking I/O             │  ← The key!
│  ┌────────────────┐              │
│  │ Reactor/Netty  │              │  Event-loop based
│  │ Event Loop     │              │  No threads wasted
│  └────────────────┘              │
└──────────┬──────────────────────┘
           │
┌──────────▼──────────────────────┐
│   External Services              │
│  - Payment Service               │
│  - Notification Service          │
│  - Inventory Service             │
└──────────────────────────────────┘
```

## 🔄 Request Flow (Module 02)

```
Time 0ms:   Request arrives
            ↓
            Process order (10ms)
            ↓
Time 10ms:  Call WebClient.post() (non-blocking setup)
            ↓
            Return response to client ✓ (< 50ms)
            ↓
            Meanwhile, WebClient waits for services (async)
            ↓
Time 500ms: Payment service responds
            ↓
            Update in background
            ↓
Time 600ms: Send notification
```

**Key**: Response returned at ~50ms. Background tasks continue.

## 📚 API Endpoints

Same as Module 01, but with async processing:

```
✅ GET    /api/customers                     # List all
✅ GET    /api/customers/{id}                # Get by ID
✅ GET    /api/customers/by-email/{email}    # Get by email
✅ POST   /api/customers                     # Create
✅ PUT    /api/customers/{id}                # Update
✅ DELETE /api/customers/{id}                # Delete

✅ GET    /api/orders                        # List all
✅ GET    /api/orders/{id}                   # Get by ID
✅ GET    /api/orders/number/{orderNumber}   # Get by number
✅ GET    /api/orders/customer/{customerId}  # Get by customer
✅ GET    /api/orders/status/{status}        # Filter by status
✅ POST   /api/orders                        # Create
✅ PUT    /api/orders/{id}                   # Update
✅ POST   /api/orders/{id}/cancel            # Cancel
✅ DELETE /api/orders/{id}                   # Delete
✅ POST   /api/orders/{id}/payment           # Process payment (ASYNC!)
```

## 🛠️ Key Components

### 1. **WebClient Configuration** (`WebClientConfig.java`)

```java
@Bean
public WebClient webClient() {
    return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .filter(logRequestFilter())
            .filter(logResponseFilter())
            .build();
}
```

**Why WebClient?**
- Built on Reactor/Project Reactor
- Non-blocking HTTP client
- Uses Netty under the hood
- Better for high concurrency
- Automatic request/response logging

### 2. **Async Service Clients** (`PaymentServiceClient.java`)

```java
public CompletableFuture<PaymentDto> processPaymentAsync(Long orderId, PaymentDto payment) {
    return webClient.post()
            .retrieve()
            .bodyToMono(PaymentDto.class)
            .subscribeOn(Schedulers.boundedElastic())
            .toFuture();  // ← Returns CompletableFuture
}
```

### 3. **Background Task Execution** (`OrderService.java`)

```java
@Async
protected void processPaymentAsync(OrderEntity order, PaymentEntity payment) {
    paymentServiceClient.processPaymentAsync(...)
        .thenAccept(result -> {
            // Update database
            payment.setStatus(result.getStatus());
            paymentRepository.save(payment);
        })
        .exceptionally(ex -> {
            // Handle errors
            log.error("Payment failed", ex);
            return null;
        });
}
```

### 4. **Fire-and-Forget Notifications** (`NotificationServiceClient.java`)

```java
public void sendNotificationAsync(String email, String subject, String message) {
    webClient.post()
            .uri(notificationServiceUrl + "/api/send")
            .bodyValue(notification)
            .retrieve()
            .toBodilessEntity()
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();  // Fire and forget - no waiting
}
```

## 🎯 When to Use Module 02 Pattern

✅ **Use when:**
- Your API calls external services (payment, notification, inventory)
- You want fast response times
- You need to handle 500+ concurrent users
- You want better thread utilization
- You can tolerate eventual consistency (async updates)

❌ **Don't use when:**
- All operations must be synchronous
- You need immediate payment confirmation
- Your services are very fast (< 10ms)
- You have simple CRUD-only operations

## 📈 Performance Comparison

### Scenario: 100 concurrent users, each makes API call that calls 2 external services

**Module 01 (Blocking)**:
```
Thread pool: 200 threads
Active requests: 100
Waiting on services: ~100 threads tied up
Available for new requests: ~100 threads
Time per request: 500ms+ (waits for all services)
Throughput: ~200 RPS
```

**Module 02 (WebClient Async)**:
```
Thread pool: 20 threads (configured)
Active requests: 100 (async)
Waiting on services: 0 threads blocked
Available for new requests: ~20 potential
Time per request: ~50-100ms (returns while services process)
Throughput: ~2000+ RPS
```

## 🔧 Configuration

### `application.yml`

```yaml
# Task executor for @Async
spring:
  task:
    execution:
      pool:
        core-size: 10        # Minimum threads
        max-size: 20         # Maximum threads
        queue-capacity: 100  # Queue for pending tasks

# External service timeouts
external:
  services:
    payment-service:
      url: http://localhost:9090
      timeout: 5000ms       # 5 second timeout
```

## 🚀 Running Module 02

### Build
```bash
cd 02-rest-reactive-webclient
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

### Test Payment Endpoint (Async)
```bash
# This returns immediately (< 50ms)
curl -X POST http://localhost:8080/api/orders/1/payment \
  -H "Content-Type: application/json" \
  -d '{
    "paymentMethod": "CREDIT_CARD",
    "cardToken": "tok_123456"
  }'

# Response comes back in ~50ms
# But payment processing continues in background
# Check order status later to see if payment completed
```

## 💡 Key Learnings

1. **Non-blocking I/O** - WebClient doesn't block threads
2. **Reactor/Netty** - Event-loop based, not thread-per-request
3. **CompletableFuture** - Bridge between blocking and async code
4. **@Async** - Spring's annotation for async method execution
5. **Schedulers** - Control which thread performs work
6. **Fire-and-forget** - Useful for notifications (don't block request)
7. **Eventual consistency** - Updates happen after response

## 📊 Reactor Concepts Used

```
Mono<T>           → Single value or error
  └─ toFuture()   → Convert to CompletableFuture

Flux<T>           → Multiple values

subscribeOn()     → Which thread executes
publishOn()       → Which thread receives results

doOnError()       → Handle errors
onErrorResume()   → Fallback on error
```

## 🔄 Comparison Code Snippets

### Module 01 (Blocking)
```java
public PaymentDto processPayment(Long orderId, PaymentRequest request) {
    // This blocks:
    PaymentDto result = restTemplate.postForObject(url, request, PaymentDto.class);
    
    // This blocks:
    notificationService.sendEmail(...);
    
    return result;  // Takes 500ms
}
```

### Module 02 (Async)
```java
public PaymentDto processPayment(Long orderId, PaymentRequest request) {
    // Creates payment record (quick)
    PaymentEntity payment = paymentRepository.save(...);
    
    // Start async processing (doesn't block)
    processPaymentAsync(order, payment, request);
    
    return mapper.toDto(payment);  // Returns in ~50ms
}

@Async
protected void processPaymentAsync(OrderEntity order, PaymentEntity payment, PaymentRequest request) {
    // This runs in background, doesn't block request
    paymentServiceClient.processPaymentAsync(...)
        .thenAccept(result -> {
            // Update payment when done
            payment.setStatus(result.getStatus());
            paymentRepository.save(payment);
            
            // Send notification (also async, fire-and-forget)
            notificationServiceClient.sendNotificationAsync(...);
        });
}
```

## ⚠️ Important Notes

1. **Database Consistency**: Order record created immediately, payment status updated later
2. **Error Handling**: Failed payments don't fail the order creation
3. **Idempotency**: Ensure payment service is idempotent (can retry safely)
4. **Monitoring**: Monitor background task queue and thread pool usage
5. **Testing**: Test async behavior with appropriate delays/waits

## 📚 Further Reading

- [Spring WebClient Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-client)
- [Reactor Project Documentation](https://projectreactor.io/docs)
- [CompletableFuture Guide](https://www.baeldung.com/java-completablefuture)
- [Spring @Async](https://spring.io/guides/gs/async-method/)

---

**Next Module**: [03-webflux-api](../03-webflux-api) - Fully reactive with WebFlux and Reactor

