# API Styles Comparison Guide

## Side-by-Side Comparison

### 1. Architecture & Design

| Aspect | REST MVC | REST + WebClient | WebFlux | GraphQL | SOAP | gRPC | WebSocket |
|--------|----------|------------------|---------|---------|------|------|-----------|
| **Architecture** | Client-Server | Client-Server | Client-Server | Client-Server | Client-Server | RPC | Bidirectional |
| **Communication** | HTTP Request-Response | HTTP Async | HTTP Async | HTTP Post | HTTP SOAP | Binary/gRPC | Long Lived |
| **Data Format** | JSON/XML | JSON/XML | JSON | JSON | XML | Protobuf | JSON/Binary |
| **Contract** | REST conventions | REST conventions | REST conventions | GraphQL Schema | WSDL/XSD | .proto | WebSocket Protocol |
| **Best For** | CRUD APIs | Async downstream calls | High throughput | Flexible queries | Legacy systems | Service mesh | Real-time |

### 2. Request-Response Patterns

#### REST API (MVC)
```java
// Request
POST /api/orders
Content-Type: application/json

{
  "customerId": 1,
  "items": [{"productId": "P1", "quantity": 2}]
}

// Response
201 Created
{
  "id": 101,
  "orderNumber": "ORD-2024-001",
  "status": "PENDING",
  "totalAmount": 199.99
}
```

#### WebClient (Async REST)
```java
// Controller (synchronous)
@PostMapping("/orders")
public Order createOrder(@RequestBody Order order) {
    return orderService.createOrder(order);  // Blocks until result
}

// Service (asynchronous)
public Order createOrder(Order order) {
    // Call payment service async, wait for result
    Payment payment = webClient.post()
        .uri("/payments")
        .bodyValue(order)
        .retrieve()
        .bodyToMono(Payment.class)
        .block();  // Block here, not in controller
    
    // Call notification service async
    webClient.post()
        .uri("/notify")
        .send();  // Fire and forget
    
    return orderRepository.save(order);
}
```

#### WebFlux (Fully Reactive)
```java
// Controller (fully async)
@PostMapping("/orders")
public Mono<ResponseEntity<Order>> createOrder(@RequestBody Order order) {
    return orderService.createOrder(order)
        .map(o -> ResponseEntity.status(201).body(o));
}

// Service (fully reactive)
public Mono<Order> createOrder(Order order) {
    return Mono.just(order)
        .flatMap(o -> processPayment(o))
        .flatMap(o -> sendNotification(o))
        .flatMap(o -> orderRepository.save(o));
}

private Mono<Order> processPayment(Order order) {
    return webClient.post()
        .uri("/payments")
        .bodyValue(order)
        .retrieve()
        .bodyToMono(Payment.class)
        .map(p -> order);  // No blocking!
}
```

#### GraphQL
```graphql
# GraphQL Query
POST /graphql

query {
  createOrder(input: {
    customerId: 1
    items: [{productId: "P1", quantity: 2}]
  }) {
    id
    orderNumber
    status
    totalAmount
    customer {
      id
      name
      email
    }
    items {
      productId
      productName
      quantity
    }
  }
}

# Response
{
  "data": {
    "createOrder": {
      "id": "101",
      "orderNumber": "ORD-2024-001",
      "status": "PENDING",
      "totalAmount": 199.99,
      "customer": {
        "id": "1",
        "name": "John Doe",
        "email": "john@example.com"
      },
      "items": [...]
    }
  }
}
```

#### SOAP
```xml
<!-- Request -->
POST /ws/ HTTP/1.1
Content-Type: application/soap+xml

<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope"
               xmlns:ord="http://apiarchlab.com/orders">
  <soap:Body>
    <ord:CreateOrderRequest>
      <ord:customerId>1</ord:customerId>
      <ord:items>
        <ord:item>
          <ord:productId>P1</ord:productId>
          <ord:quantity>2</ord:quantity>
        </ord:item>
      </ord:items>
    </ord:CreateOrderRequest>
  </soap:Body>
</soap:Envelope>

<!-- Response -->
<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
  <soap:Body>
    <ord:CreateOrderResponse>
      <ord:order>
        <ord:id>101</ord:id>
        <ord:orderNumber>ORD-2024-001</ord:orderNumber>
        <ord:status>PENDING</ord:status>
        <ord:totalAmount>199.99</ord:totalAmount>
      </ord:order>
    </ord:CreateOrderResponse>
  </soap:Body>
</soap:Envelope>
```

#### gRPC
```protobuf
// .proto definition
service OrderService {
  rpc CreateOrder(CreateOrderRequest) returns (OrderResponse);
}

// Java stub usage
channel = ManagedChannelBuilder.forAddress("localhost", 50051)
    .usePlaintext()
    .build();

OrderServiceGrpc.OrderServiceBlockingStub stub = 
    OrderServiceGrpc.newBlockingStub(channel);

OrderResponse response = stub.createOrder(CreateOrderRequest.newBuilder()
    .setCustomerId(1)
    .addItems(OrderItem.newBuilder()
        .setProductId("P1")
        .setQuantity(2)
        .build())
    .build());
```

#### WebSocket
```javascript
// Client-side JavaScript
const socket = new WebSocket("ws://localhost:8080/ws/notifications");

socket.onmessage = (e) => {
    const notification = JSON.parse(e.data);
    console.log("Order status updated:", notification);
};

// Send message
socket.send(JSON.stringify({
    type: "CREATE_ORDER",
    customerId: 1,
    items: [{productId: "P1", quantity: 2}]
}));

// Server broadcasts updates
socket.onmessage = (e) => {
    // Subscription received
};
```

## 3. Scaling & Performance

### Request Throughput

```
Scenario: 100 concurrent customers creating orders, each with 5 sequential calls

REST (MVC):
├─ Thread per request: 100 threads active
├─ Thread pool limited to 200
└─ ~500 rps max (5 calls/user × 100 users)

REST + WebClient:
├─ Main request: 1 thread
├─ Background calls: event-loop (async)
├─ Thread pool: 12-50 threads for 100 concurrent
└─ ~2000+ rps (async bottleneck lifted)

WebFlux:
├─ Event-loop per CPU core
├─ Non-blocking entire stack
├─ Thread pool: 8-16 threads for 100 concurrent
└─ ~5000+ rps (fully reactive)

gRPC:
├─ Binary protocol (smaller payload)
├─ HTTP/2 multiplexing (multiple streams per connection)
├─ Thread pool optimized
└─ ~10000+ rps (high throughput)

GraphQL:
├─ Depends on query complexity
├─ Over-fetching eliminated
├─ ~1000-2000 rps (query dependent)
└─ Better bandwidth utilization

SOAP:
├─ XML overhead (verbose)
├─ Thread per request
├─ Limited to legacy constraints
└─ ~100-300 rps

WebSocket:
├─ Persistent connections (saves handshake)
├─ Bidirectional (less polling)
├─ ~10000+ concurrent connections
└─ Low latency (<50ms) for push notifications
```

## 4. Concurrency Models

### REST MVC (Blocking)
```
┌─────────────┐
│  Request 1  │
│  Thread: T1 │
│  Processing │
│ ╔═════════╗ │
│ ║ I/O Wait║ (blocked)
│ ╚═════════╝ │
│  Response   │
└─────────────┘

Total Time: Processing + I/O Wait = ~500ms
Thread Count: 1 per request
Maximum Concurrent: Limited by thread pool (~200)
```

### WebClient (Async Callback)
```
┌──────────────────┐
│  Request Handler │
│  Thread: T1      │
│  Processing      │
│  ┌────────────┐  │
│  │ Async Call │──┐
│  │ (no wait)  │  │
│  └────────────┘  │
│  Complete        │
└──────────────────┘
        └─────────────────┐
                    ┌─────────────┐
                    │ Background  │
                    │ Thread: T2  │
                    │ I/O Wait    │
                    │ Callback    │
                    └─────────────┘

Total Time: Max(Processing, I/O) = ~300ms
Thread Count: Multiple for async work, but reused
Maximum Concurrent: 1000+ users with ~50 threads
```

### WebFlux (Reactive Streams)
```
┌————————————————────────────┐
│ Mono/Flux Operators Chain  │
│ Non-blocking throughout    │
│ ───────────────────────    │
│ ├─ Process               │
│ ├─ Async Call 1          │
│ ├─ Async Call 2          │
│ ├─ Transform             │
│ └─ Respond               │
└————————————————————————————┘
    Event-loop orchestrates, no blocking

Total Time: Process (1ms) + I/O (100ms) = ~101ms
Thread Count: Event loop + connection threads
Maximum Concurrent: 10000+ users with 8-16 threads
```

## 5. Data Fetching Patterns

### REST (Over-fetching / Under-fetching)
```
GET /api/orders/101
Response: ✓ id, orderNumber, status, customerId, items, totalAmount
          ✗ customer details (need another call)
          ✗ payment status will need another call

// Need 3 requests:
GET /api/orders/101
GET /api/customers/1
GET /api/payments?orderId=101

Bandwidth: 3 × 1KB = 3KB
Latency: 3 × 50ms = 150ms
```

### GraphQL (Client-Driven)
```graphql
query {
  order(id: 101) {
    id
    orderNumber
    status
    customer {
      name
      email
    }
    payment {
      status
    }
  }
}

// 1 request with exactly what you need
Bandwidth: 500 bytes
Latency: 50ms
```

### gRPC (Explicit Protocol)
```protobuf
message OrderResponse {
  Order order = 1;
  Customer customer = 2;
  Payment payment = 3;
}

// Protocol buffer defines what's returned
// No over-fetching, no under-fetching
Bandwidth: Binary = 200 bytes
Latency: 50ms
```

## 6. Error Handling

### REST HTTP Status Codes
```java
200 OK
201 Created
400 Bad Request
401 Unauthorized
404 Not Found
500 Internal Server Error

// Response body
{
  "error": "Invalid order amount",
  "code": "INVALID_AMOUNT",
  "details": {...}
}
```

### GraphQL Errors
```json
{
  "data": {
    "createOrder": null
  },
  "errors": [{
    "message": "Invalid order amount",
    "extensions": {
      "code": "INVALID_AMOUNT",
      "validationErrors": [...]
    }
  }]
}
```

### gRPC Status Codes
```
OK = 0
CANCELLED = 1
UNKNOWN = 2
INVALID_ARGUMENT = 3
DEADLINE_EXCEEDED = 4
NOT_FOUND = 5
...

// Metadata in response
io.grpc.Status: INVALID_ARGUMENT
Message: Invalid order amount
```

### SOAP Faults
```xml
<soap:Fault>
  <soap:Code>
    <soap:Value>soap:Sender</soap:Value>
  </soap:Code>
  <soap:Reason>
    <soap:Text>Invalid order amount</soap:Text>
  </soap:Reason>
</soap:Fault>
```

## 7. Security Considerations

| Aspect | REST | WebFlux | GraphQL | gRPC | SOAP | WebSocket |
|--------|------|---------|---------|------|------|-----------|
| **Authentication** | JWT, OAuth | Same as REST | Query-level | mTLS, OAuth | WS-Security | Cookie, JWT |
| **AuthZ** | Route-level | Route-level | Field-level | RPC-level | Message-level | Message-level |
| **Introspection** | Swagger/OpenAPI | Same | ⚠️ Enable/disable | ❌ No | ❌ No | Manual docs |
| **CORS** | ✅ Easy | ✅ Easy | ✅ Easy | N/A | ✅ When HTTP | ✅ Origin check |
| **Rate Limiting** | ✅ IP-based | ✅ User-based | ⚠️ Complex | ✅ Connection-based | ✅ Per user | ✅ Connection |

## 8. When to Use What

### Use REST + MVC When:
- ✅ Building simple CRUD APIs
- ✅ Standard HTTP clients (browsers, mobile)
- ✅ Caching is important (HTTP caches work well)
- ✅ Large team (REST is familiar)
- ✅ Public APIs (REST is standard)

### Use REST + WebClient When:
- ✅ Main API is synchronous (REST)
- ✅ But calls external async services
- ✅ Want to reduce thread consumption
- ✅ Not ready for full Reactive

### Use WebFlux When:
- ✅ Need high concurrency (10K+ concurrent)
- ✅ Streaming responses (Server-Sent Events)
- ✅ Real-time data processing
- ✅ All downstream calls support async
- ✅ Team comfortable with reactive

### Use GraphQL When:
- ✅ Multiple mobile clients with different needs
- ✅ Over-fetching is a problem
- ✅ Complex filtering/sorting required
- ✅ API evolves frequently
- ⚠️ Beware of N+1 query problem

### Use gRPC When:
- ✅ Internal service-to-service communication
- ✅ High performance is critical
- ✅ Both client and server are Java (or other), not browser
- ✅ Bandwidth is expensive (binary protocol)
- ✅ Real-time streaming

### Use SOAP When:
- ✅ Integrating with legacy enterprise systems
- ✅ XML-based contracts required
- ✅ Complex WS-* standards needed
- ✅ Government/banking requirements
- ⚠️ New systems: avoid

### Use WebSocket When:
- ✅ Real-time notifications (chat, alerts)
- ✅ Live dashboard updates
- ✅ Collaborative editing
- ✅ Bi-directional communication
- ✅ Minimize server push latency

### Use API Gateway When:
- ✅ Multiple backend services
- ✅ Unified entry point needed
- ✅ Authentication/rate-limiting required
- ✅ API versioning/routing
- ✅ Protocol translation

## 9. Quick Decision Tree

```
Do you need real-time push from server?
├─ YES → WebSocket
└─ NO
    ├─ Is it internal service-to-service?
    │   ├─ YES → gRPC
    │   └─ NO
    │       ├─ Do you have legacy SOAP systems?
    │       │   ├─ YES → SOAP
    │       │   └─ NO
    │       │       ├─ Do you have 10K+ concurrent users?
    │       │       │   ├─ YES → WebFlux
    │       │       │   └─ NO
    │       │       │       ├─ Do you need flexible queries (GraphQL style)?
    │       │       │       │   ├─ YES → GraphQL
    │       │       │       │   └─ NO
    │       │       │       │       ├─ Does your API call external async services?
    │       │       │       │       │   ├─ YES → REST + WebClient
    │       │       │       │       │   └─ NO → REST (Simple MVC)
```

## 10. References

- [REST API Best Practices](https://restfulapi.net/)
- [Spring WebFlux Documentation](https://spring.io/projects/spring-webflux)
- [GraphQL Best Practices](https://graphql.org/learn/best-practices/)
- [gRPC Features](https://grpc.io/docs/what-is-grpc/introduction/)
- [SOAP vs REST](https://www.soapui.org/rest-testing/)
- [WebSocket Protocol RFC 6455](https://tools.ietf.org/html/rfc6455)

---

**Pro Tip**: Start with REST for simplicity, adopt WebFlux for scale, and consider GraphQL/gRPC for specialization.

