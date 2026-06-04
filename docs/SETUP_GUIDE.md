# Module Setup Guide

## Overview

Each module is a standalone Spring Boot application that can be built and run independently. All modules share:
- **Parent POM**: For centralized dependency management
- **Shared Domain**: Common entities, enums, and DTOs
- **H2 Database**: For development and testing (can be swapped for PostgreSQL)
- **Java 17**: As the minimum version

## General Setup Instructions

### 1. Build the Parent Project (First Time Only)

```bash
cd java-api-architecture-lab
mvn clean install
```

This builds:
- Parent POM
- Shared domain module
- All modules

### 2. Build a Specific Module

```bash
mvn clean install -pl 01-rest-api-springmvc
```

Or build multiple modules:

```bash
mvn clean install -pl 01-rest-api-springmvc,02-rest-reactive-webclient
```

### 3. Run a Module

```bash
mvn spring-boot:run -pl 01-rest-api-springmvc
```

Or navigate to the module and run:

```bash
cd 01-rest-api-springmvc
mvn spring-boot:run
```

## Module-Specific Setup

### ✅ Module 01: REST API with Spring MVC

**Default Port**: 8080

```bash
mvn spring-boot:run -pl 01-rest-api-springmvc
```

**Key Dependencies**:
- Spring Boot Web (Spring MVC)
- Spring Data JPA
- H2 Database
- Jakarta Validation

**What to Implement**:
1. `@RestController` endpoints for Customer and Order
2. Service layer with business logic
3. Repository interfaces (extends `JpaRepository`)
4. Exception handlers with `@ExceptionHandler`
5. Request/Response DTOs

**Sample Endpoints**:
```
GET    /api/orders                      # List all orders
POST   /api/orders                      # Create order
GET    /api/orders/{id}                 # Get order by ID
PUT    /api/orders/{id}                 # Update order
DELETE /api/orders/{id}                 # Delete order
POST   /api/orders/{id}/payment         # Process payment
```

### ✅ Module 02: REST API with Reactive WebClient

**Default Port**: 8080 (Main API), requires downstream services

```bash
mvn spring-boot:run -pl 02-rest-reactive-webclient
```

**Key Dependencies**:
- Spring Boot Web (Spring MVC)
- Spring Boot WebFlux (for WebClient)
- Reactor Core
- Spring Data JPA

**Key Patterns**:
- `WebClient` for async HTTP calls
- `RestTemplate` for blocking calls (kept simple in MVC)
- `Mono<T>` and `Flux<T>` in service layer
- Non-blocking responses

**Implementation Notes**:
- Keep controllers synchronous (return regular objects)
- Inject `WebClient` in services
- Services return reactive types internally
- Example: Call payment service, notification service asynchronously

### ✅ Module 03: WebFlux API (Fully Reactive)

**Default Port**: 8080

```bash
mvn spring-boot:run -pl 03-webflux-api
```

**Key Dependencies**:
- Spring Boot WebFlux
- Reactor Core
- Spring Data R2DBC (Reactive database driver)
- H2 R2DBC Driver

**What to Implement**:
1. Reactive `@RestController` methods returning `Mono<T>` and `Flux<T>`
2. Reactive repository extending `ReactiveCrudRepository`
3. Reactive services with `Mono` and `Flux`
4. Reactive error handling

**Key Differences from REST/MVC**:
- Controllers return `Mono<ResponseEntity<T>>` or `Flux<T>`
- Services compose reactive operations
- Database operations are non-blocking (R2DBC)
- No thread pool limitations

**Sample Implementation**:
```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @GetMapping
    public Flux<Order> getAllOrders() {
        return orderService.findAll();
    }

    @PostMapping
    public Mono<Order> createOrder(@RequestBody Order order) {
        return orderService.save(order);
    }
}
```

### ✅ Module 04: GraphQL API

**Default Port**: 8080

```bash
mvn spring-boot:run -pl 04-graphql-api
```

**GraphiQL Interface**: http://localhost:8080/graphiql

**Key Dependencies**:
- Spring Boot GraphQL
- GraphQL Java
- Spring Data JPA

**File Structure**:
```
src/main/resources/graphql/
├── schema.graphqls          # Type definitions
├── queries.graphqls         # Query types
├── mutations.graphqls       # Mutation types
└── subscriptions.graphqls   # Subscription types (optional)
```

**What to Implement**:
1. GraphQL schema (`.graphqls` files)
2. `@Controller` with `@QueryMapping`, `@MutationMapping`
3. Resolvers for complex types
4. Error handling

**Sample Schema**:
```graphql
type Query {
    orders: [Order!]!
    order(id: ID!): Order
}

type Mutation {
    createOrder(input: CreateOrderInput!): Order!
    processPayment(orderId: ID!, amount: Float!): Payment!
}

type Order {
    id: ID!
    orderNumber: String!
    status: OrderStatus!
    items: [OrderItem!]!
    totalAmount: Float!
}
```

### ✅ Module 05: SOAP API

**Default Port**: 8080
**SOAP Endpoint**: http://localhost:8080/ws/

```bash
mvn spring-boot:run -pl 05-soap-api
```

**Key Dependencies**:
- Spring Boot Web Services (Spring-WS)
- WSDL4J
- JAXB (XML binding)

**File Structure**:
```
src/main/resources/xsd/
├── orders.xsd              # Schema definition
└── payments.xsd            # Schema definition

src/main/java/
└── com/apiarchlab/soap/
    ├── endpoint/           # SOAP endpoints
    ├── service/            # Business logic
    └── generated/          # Generated classes from XSD
```

**What to Implement**:
1. XSD schema definitions (contract-first)
2. SOAP endpoints extending `AbstractWsEndpoint`
3. Message handlers and marshallers
4. WSDL generation

**Key Concepts**:
- Contract-first approach (define XSD first)
- Generated JAXB classes from XSD
- WSDL auto-generation
- XML-based request/response

### ✅ Module 06: gRPC API

**Default Port**: 50051 (gRPC uses different port than HTTP)

```bash
mvn spring-boot:run -pl 06-grpc-api
```

**Key Dependencies**:
- gRPC Java (netty-shaded)
- Protocol Buffers
- Maven Protobuf plugin

**File Structure**:
```
src/main/proto/
├── orders.proto            # gRPC service definition
└── payments.proto          # gRPC service definition

target/generated-sources/protobuf/
├── java/                   # Generated Java stubs
└── grpc-java/             # Generated gRPC services
```

**What to Implement**:
1. `.proto` files with service definitions
2. Service implementations extending generated `*ImplBase`
3. Message definitions in proto
4. Server configuration for gRPC

**Sample Proto File**:
```protobuf
syntax = "proto3";

package com.apiarchlab.grpc;

service OrderService {
    rpc CreateOrder (CreateOrderRequest) returns (OrderResponse) {}
    rpc GetOrder (GetOrderRequest) returns (OrderResponse) {}
}

message CreateOrderRequest {
    int64 customer_id = 1;
    repeated OrderItem items = 2;
}

message OrderItem {
    string product_id = 1;
    int32 quantity = 2;
}
```

**Testing gRPC**:
- Use `grpcurl` command-line tool
- Implement integration tests with `grpc-testing`
- No GraphiQL/Swagger auto-UI support

### ✅ Module 07: WebSocket API

**Default Port**: 8080

```bash
mvn spring-boot:run -pl 07-websocket-api
```

**Key Dependencies**:
- Spring Boot WebSocket
- Spring Boot Messaging
- STOMP (Simple Text Oriented Messaging Protocol)

**File Structure**:
```
src/main/java/com/apiarchlab/websocket/
├── config/               # WebSocket configuration
├── handler/              # Message handlers
├── service/              # Business logic
└── controller/           # Message mappings
```

**What to Implement**:
1. `WebSocketConfigurer` for endpoint registration
2. STOMP message handlers with `@MessageMapping`
3. `SimpMessagingTemplate` for sending messages
4. Client-side JavaScript for WebSocket connection

**Sample Configuration**:
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/notifications").setAllowedOrigins("*");
    }
}
```

**Key Patterns**:
- Topic subscriptions: `/topic/orders/{orderId}`
- Queue messages: `/queue/notifications`
- Broadcast to all: `simpMessagingTemplate.convertAndSend()`
- Send to specific user: `convertAndSendToUser()`

### ✅ Module 08: API Gateway Integration

**Default Port**: 8000 (Gateway)
**Routes to**: Ports 8080-8087 (other modules)

```bash
mvn spring-boot:run -pl 08-api-gateway-integration
```

**Key Dependencies**:
- Spring Cloud Gateway
- Spring Cloud Service Discovery (Eureka)
- Resilience4j (Circuit Breaker)
- Micrometer (Metrics)

**Configuration** (`application.yml`):
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: rest-api
          uri: http://localhost:8080
          predicates:
            - Path=/api/rest/**
          filters:
            - StripPrefix=2
            - CircuitBreaker=rest-api
            
        - id: webflux-api
          uri: http://localhost:8081
          predicates:
            - Path=/api/webflux/**
```

**What to Implement**:
1. Route definitions (static or dynamic)
2. Custom filters for auth, logging
3. Circuit breaker configuration
4. Load balancing strategy
5. Request/response transformation

## Building & Running All Modules

### Quick Start Script

```bash
#!/bin/bash
cd java-api-architecture-lab

# Build everything
mvn clean install

# Run all modules in separate terminals
# Terminal 1
mvn spring-boot:run -pl 01-rest-api-springmvc &

# Terminal 2
mvn spring-boot:run -pl 03-webflux-api &

# Terminal 3
mvn spring-boot:run -pl 04-graphql-api &

# Terminal 4
mvn spring-boot:run -pl 08-api-gateway-integration &

wait
```

## Troubleshooting

### Issue: "Cannot find module"
**Solution**: 
```bash
mvn clean install  # Rebuild parent first
```

### Issue: Port already in use
**Solution**: 
Kill existing process or change port:
```yaml
# application.yml
server:
  port: 8081  # Change to different port
```

### Issue: H2 database file locked
**Solution**:
```bash
# Delete database files
rm -rf *.mv.db *.trace.db
mvn clean
```

### Issue: Protobuf compilation error (gRPC)
**Solution**:
```bash
# Ensure protoc compiler is installed
brew install protobuf  # macOS

# Then rebuild
mvn clean compile -pl 06-grpc-api
```

## Performance Tuning

### JVM Options

Add to `pom.xml` plugin configuration:
```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <jvmArguments>-Xmx512m -Xms256m</jvmArguments>
    </configuration>
</plugin>
```

Or set environment variable:
```bash
export JAVA_OPTS="-Xmx1g -Xms512m"
mvn spring-boot:run -pl 03-webflux-api
```

### Database Connection Pool

Configure in `application.yml`:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```

## Next Steps

1. **Start with Module 01** for REST fundamentals
2. **Explore Module 03** to understand reactive patterns
3. **Try Module 04** to learn GraphQL
4. **Use Module 08** to see how everything fits together
5. **Read the individual module READMEs** for detailed guidance

Happy learning! 🚀

