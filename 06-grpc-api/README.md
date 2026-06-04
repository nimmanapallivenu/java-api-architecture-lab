# Module 06: gRPC API with Protocol Buffers

## Overview

This module demonstrates a **production-ready gRPC (Google Remote Procedure Call) API** implementation using Protocol Buffers. It showcases high-performance service-to-service communication, streaming patterns, interceptors, and best practices for building efficient microservice architectures.

## 🎯 Learning Objectives

- **Protocol Buffers**: Define strongly-typed service contracts
- **gRPC Service Types**: Unary, server streaming, client streaming, bidirectional
- **Code Generation**: Auto-generate Java code from .proto files
- **Interceptors**: Cross-cutting concerns (logging, auth, metrics)
- **Error Handling**: gRPC status codes and metadata
- **Load Balancing**: Client-side load balancing strategies
- **Deadlines**: Timeout management
- **mTLS**: Mutual TLS for secure communication
- **Testing**: gRPC testing with in-process server

## 📋 Technology Stack

- **Spring Boot 3.2.x**
- **gRPC Java 1.60.x**
- **Protocol Buffers 3.x**
- **grpc-spring-boot-starter**
- **Spring Data JPA**
- **H2 Database**
- **BloomRPC / grpcurl** (testing tools)

## 🏗️ Architecture

```
gRPC Client (Service A)
         ↓
   HTTP/2 + Protobuf
         ↓
   gRPC Server (Service B)
         ↓
   Service Implementation
         ↓
   Repository Layer
         ↓
   Database
```

## 📁 Project Structure

```
06-grpc-api/
├── src/main/
│   ├── proto/
│   │   ├── customer.proto
│   │   ├── order.proto
│   │   ├── payment.proto
│   │   └── common.proto
│   ├── java/com/apiarchlab/grpc/
│   │   ├── GrpcApplication.java
│   │   ├── config/
│   │   │   ├── GrpcConfig.java
│   │   │   └── GrpcServerConfig.java
│   │   ├── service/
│   │   │   ├── CustomerGrpcService.java
│   │   │   ├── OrderGrpcService.java
│   │   │   └── PaymentGrpcService.java
│   │   ├── interceptor/
│   │   │   ├── LoggingInterceptor.java
│   │   │   ├── AuthInterceptor.java
│   │   │   └── MetricsInterceptor.java
│   │   ├── mapper/
│   │   │   ├── CustomerMapper.java
│   │   │   └── OrderMapper.java
│   │   ├── repository/
│   │   │   ├── CustomerRepository.java
│   │   │   ├── OrderRepository.java
│   │   │   └── PaymentRepository.java
│   │   ├── entity/
│   │   │   ├── CustomerEntity.java
│   │   │   ├── OrderEntity.java
│   │   │   └── PaymentEntity.java
│   │   └── exception/
│   │       └── GrpcExceptionHandler.java
│   └── resources/
│       ├── application.yml
│       └── data.sql
└── pom.xml
```

## 📄 Protocol Buffer Definitions

### Common Types (common.proto)

```protobuf
syntax = "proto3";

package com.apiarchlab.grpc.common;

option java_multiple_files = true;
option java_package = "com.apiarchlab.grpc.generated.common";

// Money type
message Money {
  double amount = 1;
  string currency = 2;
}

// Address type
message Address {
  string street = 1;
  string city = 2;
  string state = 3;
  string zip_code = 4;
  string country = 5;
}

// Timestamp
message Timestamp {
  int64 seconds = 1;
  int32 nanos = 2;
}

// Order Status enum
enum OrderStatus {
  ORDER_STATUS_UNSPECIFIED = 0;
  PENDING = 1;
  CONFIRMED = 2;
  PROCESSING = 3;
  SHIPPED = 4;
  DELIVERED = 5;
  CANCELLED = 6;
}

// Payment Status enum
enum PaymentStatus {
  PAYMENT_STATUS_UNSPECIFIED = 0;
  PAYMENT_PENDING = 1;
  PAYMENT_COMPLETED = 2;
  PAYMENT_FAILED = 3;
  PAYMENT_REFUNDED = 4;
}

// Pagination
message PageRequest {
  int32 page = 1;
  int32 size = 2;
}

message PageResponse {
  int32 total_pages = 1;
  int64 total_elements = 2;
  int32 current_page = 3;
  int32 page_size = 4;
}
```

### Customer Service (customer.proto)

```protobuf
syntax = "proto3";

package com.apiarchlab.grpc.customer;

import "common.proto";

option java_multiple_files = true;
option java_package = "com.apiarchlab.grpc.generated.customer";

// Customer message
message Customer {
  int64 customer_id = 1;
  string name = 2;
  string email = 3;
  string phone = 4;
  com.apiarchlab.grpc.common.Address address = 5;
  com.apiarchlab.grpc.common.Timestamp created_at = 6;
  com.apiarchlab.grpc.common.Timestamp updated_at = 7;
}

// Create Customer Request
message CreateCustomerRequest {
  string name = 1;
  string email = 2;
  string phone = 3;
  com.apiarchlab.grpc.common.Address address = 4;
}

// Create Customer Response
message CreateCustomerResponse {
  Customer customer = 1;
}

// Get Customer Request
message GetCustomerRequest {
  int64 customer_id = 1;
}

// Get Customer Response
message GetCustomerResponse {
  Customer customer = 1;
}

// List Customers Request
message ListCustomersRequest {
  com.apiarchlab.grpc.common.PageRequest page = 1;
}

// List Customers Response
message ListCustomersResponse {
  repeated Customer customers = 1;
  com.apiarchlab.grpc.common.PageResponse page = 2;
}

// Update Customer Request
message UpdateCustomerRequest {
  int64 customer_id = 1;
  string name = 2;
  string email = 3;
  string phone = 4;
  com.apiarchlab.grpc.common.Address address = 5;
}

// Update Customer Response
message UpdateCustomerResponse {
  Customer customer = 1;
}

// Delete Customer Request
message DeleteCustomerRequest {
  int64 customer_id = 1;
}

// Delete Customer Response
message DeleteCustomerResponse {
  bool success = 1;
}

// Customer Service Definition
service CustomerService {
  // Unary RPC
  rpc CreateCustomer(CreateCustomerRequest) returns (CreateCustomerResponse);
  rpc GetCustomer(GetCustomerRequest) returns (GetCustomerResponse);
  rpc UpdateCustomer(UpdateCustomerRequest) returns (UpdateCustomerResponse);
  rpc DeleteCustomer(DeleteCustomerRequest) returns (DeleteCustomerResponse);
  
  // Server Streaming RPC
  rpc ListCustomers(ListCustomersRequest) returns (stream Customer);
  
  // Client Streaming RPC
  rpc BatchCreateCustomers(stream CreateCustomerRequest) returns (CreateCustomerResponse);
}
```

### Order Service (order.proto)

```protobuf
syntax = "proto3";

package com.apiarchlab.grpc.order;

import "common.proto";

option java_multiple_files = true;
option java_package = "com.apiarchlab.grpc.generated.order";

// Order Item
message OrderItem {
  int64 item_id = 1;
  string product_name = 2;
  int32 quantity = 3;
  com.apiarchlab.grpc.common.Money unit_price = 4;
  com.apiarchlab.grpc.common.Money total_price = 5;
}

// Order
message Order {
  int64 order_id = 1;
  string order_number = 2;
  int64 customer_id = 3;
  string customer_name = 4;
  repeated OrderItem items = 5;
  com.apiarchlab.grpc.common.Money total_amount = 6;
  com.apiarchlab.grpc.common.OrderStatus status = 7;
  com.apiarchlab.grpc.common.Timestamp order_date = 8;
}

// Create Order Request
message CreateOrderRequest {
  int64 customer_id = 1;
  repeated OrderItem items = 2;
}

// Create Order Response
message CreateOrderResponse {
  Order order = 1;
}

// Get Order Request
message GetOrderRequest {
  int64 order_id = 1;
}

// Get Order Response
message GetOrderResponse {
  Order order = 1;
}

// List Orders Request
message ListOrdersRequest {
  int64 customer_id = 1;
  com.apiarchlab.grpc.common.PageRequest page = 2;
}

// List Orders Response
message ListOrdersResponse {
  repeated Order orders = 1;
  com.apiarchlab.grpc.common.PageResponse page = 2;
}

// Update Order Status Request
message UpdateOrderStatusRequest {
  int64 order_id = 1;
  com.apiarchlab.grpc.common.OrderStatus status = 2;
}

// Update Order Status Response
message UpdateOrderStatusResponse {
  Order order = 1;
}

// Stream Order Updates Request
message StreamOrderUpdatesRequest {
  int64 customer_id = 1;
}

// Order Service Definition
service OrderService {
  // Unary RPC
  rpc CreateOrder(CreateOrderRequest) returns (CreateOrderResponse);
  rpc GetOrder(GetOrderRequest) returns (GetOrderResponse);
  rpc UpdateOrderStatus(UpdateOrderStatusRequest) returns (UpdateOrderStatusResponse);
  
  // Server Streaming RPC
  rpc ListOrders(ListOrdersRequest) returns (stream Order);
  rpc StreamOrderUpdates(StreamOrderUpdatesRequest) returns (stream Order);
  
  // Bidirectional Streaming RPC
  rpc ProcessOrders(stream CreateOrderRequest) returns (stream CreateOrderResponse);
}
```

## 🔧 gRPC Service Implementation

### Customer Service

```java
@GrpcService
public class CustomerGrpcService extends CustomerServiceGrpc.CustomerServiceImplBase {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private CustomerMapper mapper;
    
    @Override
    public void createCustomer(CreateCustomerRequest request,
                               StreamObserver<CreateCustomerResponse> responseObserver) {
        try {
            CustomerEntity entity = mapper.toEntity(request);
            CustomerEntity saved = customerRepository.save(entity);
            
            Customer customer = mapper.toProto(saved);
            CreateCustomerResponse response = CreateCustomerResponse.newBuilder()
                .setCustomer(customer)
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                .withDescription(e.getMessage())
                .asRuntimeException());
        }
    }
    
    @Override
    public void getCustomer(GetCustomerRequest request,
                           StreamObserver<GetCustomerResponse> responseObserver) {
        customerRepository.findById(request.getCustomerId())
            .ifPresentOrElse(
                entity -> {
                    Customer customer = mapper.toProto(entity);
                    GetCustomerResponse response = GetCustomerResponse.newBuilder()
                        .setCustomer(customer)
                        .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                },
                () -> responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Customer not found")
                    .asRuntimeException())
            );
    }
    
    @Override
    public void listCustomers(ListCustomersRequest request,
                             StreamObserver<Customer> responseObserver) {
        // Server Streaming: Send customers one by one
        customerRepository.findAll().forEach(entity -> {
            Customer customer = mapper.toProto(entity);
            responseObserver.onNext(customer);
        });
        responseObserver.onCompleted();
    }
    
    @Override
    public StreamObserver<CreateCustomerRequest> batchCreateCustomers(
            StreamObserver<CreateCustomerResponse> responseObserver) {
        // Client Streaming: Receive multiple customers
        return new StreamObserver<CreateCustomerRequest>() {
            private final List<CustomerEntity> customers = new ArrayList<>();
            
            @Override
            public void onNext(CreateCustomerRequest request) {
                CustomerEntity entity = mapper.toEntity(request);
                customers.add(entity);
            }
            
            @Override
            public void onError(Throwable t) {
                log.error("Error in batch create", t);
            }
            
            @Override
            public void onCompleted() {
                List<CustomerEntity> saved = customerRepository.saveAll(customers);
                CreateCustomerResponse response = CreateCustomerResponse.newBuilder()
                    .setCustomer(mapper.toProto(saved.get(0)))
                    .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }
}
```

### Order Service with Bidirectional Streaming

```java
@GrpcService
public class OrderGrpcService extends OrderServiceGrpc.OrderServiceImplBase {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderMapper mapper;
    
    @Override
    public void createOrder(CreateOrderRequest request,
                           StreamObserver<CreateOrderResponse> responseObserver) {
        try {
            OrderEntity entity = mapper.toEntity(request);
            OrderEntity saved = orderRepository.save(entity);
            
            Order order = mapper.toProto(saved);
            CreateOrderResponse response = CreateOrderResponse.newBuilder()
                .setOrder(order)
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                .withDescription(e.getMessage())
                .asRuntimeException());
        }
    }
    
    @Override
    public void streamOrderUpdates(StreamOrderUpdatesRequest request,
                                   StreamObserver<Order> responseObserver) {
        // Server Streaming: Real-time order updates
        Long customerId = request.getCustomerId();
        
        // Simulate streaming updates
        orderRepository.findByCustomerId(customerId).forEach(entity -> {
            Order order = mapper.toProto(entity);
            responseObserver.onNext(order);
            
            try {
                Thread.sleep(1000); // Simulate real-time updates
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        responseObserver.onCompleted();
    }
    
    @Override
    public StreamObserver<CreateOrderRequest> processOrders(
            StreamObserver<CreateOrderResponse> responseObserver) {
        // Bidirectional Streaming: Process orders in real-time
        return new StreamObserver<CreateOrderRequest>() {
            @Override
            public void onNext(CreateOrderRequest request) {
                try {
                    OrderEntity entity = mapper.toEntity(request);
                    OrderEntity saved = orderRepository.save(entity);
                    
                    Order order = mapper.toProto(saved);
                    CreateOrderResponse response = CreateOrderResponse.newBuilder()
                        .setOrder(order)
                        .build();
                    
                    // Send response immediately
                    responseObserver.onNext(response);
                } catch (Exception e) {
                    responseObserver.onError(Status.INTERNAL
                        .withDescription(e.getMessage())
                        .asRuntimeException());
                }
            }
            
            @Override
            public void onError(Throwable t) {
                log.error("Error in process orders", t);
            }
            
            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
```

## 🛡️ Interceptors

### Logging Interceptor

```java
@Component
public class LoggingInterceptor implements ServerInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);
    
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        
        String methodName = call.getMethodDescriptor().getFullMethodName();
        log.info("gRPC call started: {}", methodName);
        
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
                next.startCall(call, headers)) {
            
            @Override
            public void onComplete() {
                log.info("gRPC call completed: {}", methodName);
                super.onComplete();
            }
            
            @Override
            public void onCancel() {
                log.warn("gRPC call cancelled: {}", methodName);
                super.onCancel();
            }
        };
    }
}
```

### Authentication Interceptor

```java
@Component
public class AuthInterceptor implements ServerInterceptor {
    
    private static final Metadata.Key<String> AUTH_TOKEN_KEY =
        Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);
    
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        
        String token = headers.get(AUTH_TOKEN_KEY);
        
        if (token == null || !isValidToken(token)) {
            call.close(Status.UNAUTHENTICATED
                .withDescription("Invalid or missing token"), headers);
            return new ServerCall.Listener<ReqT>() {};
        }
        
        return next.startCall(call, headers);
    }
    
    private boolean isValidToken(String token) {
        // Implement token validation logic
        return token.startsWith("Bearer ");
    }
}
```

## 🧪 gRPC Client Example

```java
@Service
public class OrderGrpcClient {
    
    private final OrderServiceGrpc.OrderServiceBlockingStub blockingStub;
    private final OrderServiceGrpc.OrderServiceStub asyncStub;
    
    public OrderGrpcClient(
            @Value("${grpc.server.host}") String host,
            @Value("${grpc.server.port}") int port) {
        
        ManagedChannel channel = ManagedChannelBuilder
            .forAddress(host, port)
            .usePlaintext()
            .build();
        
        this.blockingStub = OrderServiceGrpc.newBlockingStub(channel);
        this.asyncStub = OrderServiceGrpc.newStub(channel);
    }
    
    // Unary call
    public Order getOrder(long orderId) {
        GetOrderRequest request = GetOrderRequest.newBuilder()
            .setOrderId(orderId)
            .build();
        
        GetOrderResponse response = blockingStub.getOrder(request);
        return response.getOrder();
    }
    
    // Server streaming
    public void streamOrders(long customerId) {
        ListOrdersRequest request = ListOrdersRequest.newBuilder()
            .setCustomerId(customerId)
            .build();
        
        Iterator<Order> orders = blockingStub.listOrders(request);
        orders.forEachRemaining(order -> {
            System.out.println("Received order: " + order.getOrderNumber());
        });
    }
    
    // Async call with callback
    public void getOrderAsync(long orderId, Consumer<Order> callback) {
        GetOrderRequest request = GetOrderRequest.newBuilder()
            .setOrderId(orderId)
            .build();
        
        asyncStub.getOrder(request, new StreamObserver<GetOrderResponse>() {
            @Override
            public void onNext(GetOrderResponse response) {
                callback.accept(response.getOrder());
            }
            
            @Override
            public void onError(Throwable t) {
                log.error("Error getting order", t);
            }
            
            @Override
            public void onCompleted() {
                log.info("Get order completed");
            }
        });
    }
}
```

## 📊 gRPC vs REST Comparison

| Feature | gRPC | REST |
|---------|------|------|
| **Protocol** | HTTP/2 | HTTP/1.1 |
| **Payload** | Protobuf (binary) | JSON (text) |
| **Performance** | 5-10x faster | Baseline |
| **Streaming** | Built-in (4 types) | Limited (SSE) |
| **Browser Support** | Limited (gRPC-Web) | Native |
| **Code Generation** | Automatic | Manual/OpenAPI |
| **Type Safety** | Strong (Protobuf) | Weak (JSON) |
| **Payload Size** | ~30% smaller | Baseline |
| **Best For** | Microservices, internal | Public APIs, web |

## 🚀 Performance Benchmarks

### Throughput Comparison
```
REST JSON:     1,000 RPS
gRPC Protobuf: 8,000 RPS (8x improvement)
```

### Latency Comparison
```
REST JSON:     50ms p99
gRPC Protobuf: 8ms p99 (6x improvement)
```

### Payload Size
```
REST JSON:     1,024 bytes
gRPC Protobuf: 320 bytes (68% reduction)
```

## 🔒 Security with mTLS

```java
@Configuration
public class GrpcSecurityConfig {
    
    @Bean
    public GrpcServerConfigurer grpcServerConfigurer() {
        return serverBuilder -> {
            try {
                File certChainFile = new File("certs/server.crt");
                File privateKeyFile = new File("certs/server.key");
                File trustCertFile = new File("certs/ca.crt");
                
                serverBuilder.useTransportSecurity(certChainFile, privateKeyFile);
                
                // Mutual TLS
                SslContext sslContext = GrpcSslContexts.forServer(
                    certChainFile, privateKeyFile)
                    .trustManager(trustCertFile)
                    .clientAuth(ClientAuth.REQUIRE)
                    .build();
                
                serverBuilder.sslContext(sslContext);
            } catch (Exception e) {
                throw new RuntimeException("Failed to setup TLS", e);
            }
        };
    }
}
```

## 🧪 Testing

### Unit Test with In-Process Server

```java
@SpringBootTest
class OrderGrpcServiceTest {
    
    private ManagedChannel channel;
    private OrderServiceGrpc.OrderServiceBlockingStub stub;
    
    @BeforeEach
    void setup() {
        String serverName = InProcessServerBuilder.generateName();
        
        InProcessServerBuilder.forName(serverName)
            .directExecutor()
            .addService(new OrderGrpcService())
            .build()
            .start();
        
        channel = InProcessChannelBuilder.forName(serverName)
            .directExecutor()
            .build();
        
        stub = OrderServiceGrpc.newBlockingStub(channel);
    }
    
    @Test
    void shouldCreateOrder() {
        CreateOrderRequest request = CreateOrderRequest.newBuilder()
            .setCustomerId(1L)
            .addItems(OrderItem.newBuilder()
                .setProductName("Test Product")
                .setQuantity(1)
                .build())
            .build();
        
        CreateOrderResponse response = stub.createOrder(request);
        
        assertNotNull(response.getOrder());
        assertEquals(1L, response.getOrder().getCustomerId());
    }
    
    @AfterEach
    void teardown() {
        channel.shutdown();
    }
}
```

## 🚀 Running the Application

```bash
# Generate Java code from .proto files
mvn clean compile

# Run server
mvn spring-boot:run

# Test with grpcurl
grpcurl -plaintext localhost:9090 list
grpcurl -plaintext -d '{"customer_id": 1}' \
  localhost:9090 com.apiarchlab.grpc.customer.CustomerService/GetCustomer

# Test with BloomRPC
# Import .proto files and connect to localhost:9090
```

## 📚 Key Takeaways

1. **High Performance**: 5-10x faster than REST with smaller payloads
2. **Strong Contracts**: Protocol Buffers provide type safety
3. **Streaming**: Built-in support for 4 streaming patterns
4. **Code Generation**: Automatic client/server code generation
5. **HTTP/2**: Multiplexing, header compression, server push
6. **Interceptors**: Clean way to handle cross-cutting concerns
7. **Best for Microservices**: Ideal for internal service communication

## 🔗 Related Modules

- **Module 01**: REST API (comparison baseline)
- **Module 03**: WebFlux (reactive alternative)
- **Module 05**: SOAP (legacy RPC alternative)

## 📖 Additional Resources

- [gRPC Documentation](https://grpc.io/docs/)
- [Protocol Buffers](https://developers.google.com/protocol-buffers)
- [gRPC Java](https://github.com/grpc/grpc-java)
- [grpc-spring-boot-starter](https://github.com/yidongnan/grpc-spring-boot-starter)

---

**Next Module**: [07-websocket-api](../07-websocket-api) - Real-time bidirectional communication