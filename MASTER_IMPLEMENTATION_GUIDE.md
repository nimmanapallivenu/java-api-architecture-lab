# 🎓 Java Tech Lead API Architecture Lab - Master Implementation Guide

## 📊 Project Status Overview

| Module | Status | Completion | Priority |
|--------|--------|------------|----------|
| 00-shared-domain | ✅ Complete | 100% | Foundation |
| 01-rest-api-springmvc | ✅ Complete | 100% | Core |
| 02-rest-reactive-webclient | ✅ Complete | 100% | Core |
| 03-webflux-api | 🔨 In Progress | 20% | High |
| 04-graphql-api | 📋 Planned | 0% | High |
| 05-soap-api | 📋 Planned | 0% | Medium |
| 06-grpc-api | 📋 Planned | 0% | High |
| 07-websocket-api | 📋 Planned | 0% | Medium |
| 08-api-gateway-integration | 📋 Planned | 0% | Final |

---

## 🎯 Learning Roadmap

### Phase 1: Foundation (COMPLETED ✅)
- **Module 00**: Shared Domain Models
- **Module 01**: REST API with Spring MVC
- **Module 02**: REST with Reactive WebClient

### Phase 2: Modern APIs (NEXT)
- **Module 03**: WebFlux Reactive API
- **Module 04**: GraphQL API
- **Module 06**: gRPC API

### Phase 3: Legacy & Real-time
- **Module 05**: SOAP API
- **Module 07**: WebSocket API

### Phase 4: Integration
- **Module 08**: API Gateway Integration

---

## 📚 Module 03: WebFlux API - Implementation Plan

### Overview
Fully non-blocking reactive API using Spring WebFlux, Reactor, and R2DBC.

### Key Concepts
- ✅ Mono<T> - Single value reactive type
- ✅ Flux<T> - Multiple values reactive stream
- ✅ Reactive Repository (R2DBC)
- ✅ Backpressure handling
- ✅ Functional endpoints (RouterFunction)
- ✅ Reactive exception handling
- ✅ Server-Sent Events (SSE)
- ✅ Streaming API

### Architecture
```
Reactive Client
  ↓ Non-blocking HTTP
RouterFunction / @RestController
  ↓ Reactive Pipeline
Reactive Service (Mono/Flux)
  ↓ Non-blocking DB
R2DBC Repository
  ↓ Async Driver
H2 Database (R2DBC)
```

### Files to Create (25 files)

#### Core Application
1. `WebFluxApplication.java` - Main application class
2. `application.yml` - Configuration

#### Entities (R2DBC)
3. `CustomerEntity.java` - @Table with R2DBC
4. `OrderEntity.java` - Reactive entity
5. `OrderItemEntity.java` - Reactive entity
6. `PaymentEntity.java` - Reactive entity

#### DTOs (Reuse from Module 01)
7. `CustomerDto.java`
8. `OrderDto.java`
9. `OrderItemDto.java`
10. `PaymentDto.java`
11. `CreateCustomerRequest.java`
12. `CreateOrderRequest.java`

#### Repositories (Reactive)
13. `CustomerRepository.java` - extends ReactiveCrudRepository
14. `OrderRepository.java` - Custom reactive queries
15. `OrderItemRepository.java`
16. `PaymentRepository.java`

#### Services (Reactive)
17. `CustomerService.java` - Returns Mono/Flux
18. `OrderService.java` - Reactive business logic

#### Controllers
19. `CustomerController.java` - Reactive REST endpoints
20. `OrderController.java` - Reactive REST endpoints
21. `StreamingController.java` - SSE endpoints

#### Functional Endpoints
22. `CustomerRouter.java` - RouterFunction config
23. `CustomerHandler.java` - Handler functions

#### Configuration
24. `WebFluxConfig.java` - WebFlux configuration
25. `R2dbcConfig.java` - Database configuration

#### Exception Handling
26. `ReactiveExceptionHandler.java` - Global error handler

#### Tests
27. `CustomerServiceTest.java` - Reactive unit tests
28. `CustomerControllerTest.java` - WebTestClient integration tests

### Key Implementation Details

#### 1. Reactive Repository
```java
public interface CustomerRepository extends ReactiveCrudRepository<CustomerEntity, Long> {
    Mono<CustomerEntity> findByEmail(String email);
    Flux<CustomerEntity> findByCity(String city);
}
```

#### 2. Reactive Service
```java
@Service
public class CustomerService {
    public Mono<CustomerDto> getCustomerById(Long id) {
        return customerRepository.findById(id)
            .map(mapper::toDto)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Customer not found")));
    }
    
    public Flux<CustomerDto> getAllCustomers() {
        return customerRepository.findAll()
            .map(mapper::toDto);
    }
}
```

#### 3. Reactive Controller
```java
@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    @GetMapping("/{id}")
    public Mono<CustomerDto> getCustomer(@PathVariable Long id) {
        return customerService.getCustomerById(id);
    }
    
    @GetMapping
    public Flux<CustomerDto> getAllCustomers() {
        return customerService.getAllCustomers();
    }
}
```

#### 4. Server-Sent Events
```java
@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<OrderDto> streamOrders() {
    return orderService.getAllOrders()
        .delayElements(Duration.ofSeconds(1));
}
```

#### 5. Functional Endpoints
```java
@Configuration
public class CustomerRouter {
    @Bean
    public RouterFunction<ServerResponse> customerRoutes(CustomerHandler handler) {
        return RouterFunctions
            .route(GET("/functional/customers/{id}"), handler::getCustomer)
            .andRoute(GET("/functional/customers"), handler::getAllCustomers)
            .andRoute(POST("/functional/customers"), handler::createCustomer);
    }
}
```

### Testing Strategy
```java
@WebFluxTest(CustomerController.class)
class CustomerControllerTest {
    @Autowired
    private WebTestClient webTestClient;
    
    @Test
    void shouldGetCustomer() {
        webTestClient.get()
            .uri("/api/customers/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody(CustomerDto.class)
            .value(customer -> assertThat(customer.getId()).isEqualTo(1L));
    }
}
```

### Estimated Time: 2-3 days

---

## 📚 Module 04: GraphQL API - Implementation Plan

### Overview
GraphQL API using Spring GraphQL for flexible, client-driven data fetching.

### Key Concepts
- Schema-first design
- Query operations
- Mutation operations
- Subscription (real-time)
- DataFetcher
- N+1 problem solution
- DataLoader pattern
- Field-level authorization

### Architecture
```
GraphQL Client
  ↓ GraphQL Query/Mutation
GraphQL Controller (@QueryMapping/@MutationMapping)
  ↓
Service Layer
  ↓
Repository Layer
  ↓
Database
```

### Files to Create (20 files)

#### Core Application
1. `GraphQLApplication.java`
2. `application.yml`

#### GraphQL Schema
3. `schema.graphqls` - GraphQL schema definition

#### Entities (JPA - reuse pattern from Module 01)
4. `CustomerEntity.java`
5. `OrderEntity.java`
6. `OrderItemEntity.java`
7. `PaymentEntity.java`

#### GraphQL Types (DTOs)
8. `CustomerType.java`
9. `OrderType.java`
10. `OrderItemType.java`
11. `PaymentType.java`
12. `CreateCustomerInput.java`
13. `CreateOrderInput.java`

#### Repositories
14. `CustomerRepository.java`
15. `OrderRepository.java`
16. `PaymentRepository.java`

#### Services
17. `CustomerService.java`
18. `OrderService.java`

#### GraphQL Controllers
19. `CustomerController.java` - @QueryMapping, @MutationMapping
20. `OrderController.java` - GraphQL resolvers

#### DataLoaders (N+1 solution)
21. `CustomerDataLoader.java`
22. `OrderDataLoader.java`

#### Configuration
23. `GraphQLConfig.java`
24. `DataLoaderConfig.java`

#### Exception Handling
25. `GraphQLExceptionHandler.java`

#### Tests
26. `CustomerControllerTest.java` - GraphQL query tests
27. `OrderMutationTest.java` - Mutation tests

### Key Implementation Details

#### 1. GraphQL Schema
```graphql
type Customer {
  id: ID!
  firstName: String!
  lastName: String!
  email: String!
  orders: [Order!]!
}

type Order {
  id: ID!
  orderNumber: String!
  customer: Customer!
  items: [OrderItem!]!
  totalAmount: Float!
  status: OrderStatus!
}

enum OrderStatus {
  PENDING
  CONFIRMED
  SHIPPED
  DELIVERED
  CANCELLED
}

type Query {
  customerById(id: ID!): Customer
  customers: [Customer!]!
  orderById(id: ID!): Order
  orders: [Order!]!
}

type Mutation {
  createCustomer(input: CreateCustomerInput!): Customer!
  createOrder(input: CreateOrderInput!): Order!
  cancelOrder(id: ID!): Order!
}

input CreateCustomerInput {
  firstName: String!
  lastName: String!
  email: String!
  phone: String
}

input CreateOrderInput {
  customerId: ID!
  items: [OrderItemInput!]!
}

input OrderItemInput {
  productName: String!
  quantity: Int!
  price: Float!
}
```

#### 2. GraphQL Controller
```java
@Controller
public class CustomerController {
    
    @QueryMapping
    public Customer customerById(@Argument Long id) {
        return customerService.getCustomerById(id);
    }
    
    @QueryMapping
    public List<Customer> customers() {
        return customerService.getAllCustomers();
    }
    
    @MutationMapping
    public Customer createCustomer(@Argument CreateCustomerInput input) {
        return customerService.createCustomer(input);
    }
    
    @SchemaMapping(typeName = "Customer", field = "orders")
    public List<Order> orders(Customer customer) {
        return orderService.getOrdersByCustomerId(customer.getId());
    }
}
```

#### 3. DataLoader (N+1 Solution)
```java
@Component
public class CustomerDataLoader implements BatchLoader<Long, Customer> {
    
    @Override
    public CompletionStage<List<Customer>> load(List<Long> customerIds) {
        return CompletableFuture.supplyAsync(() -> 
            customerRepository.findAllById(customerIds)
        );
    }
}
```

#### 4. Testing
```java
@GraphQlTest(CustomerController.class)
class CustomerControllerTest {
    
    @Autowired
    private GraphQlTester graphQlTester;
    
    @Test
    void shouldGetCustomerById() {
        graphQlTester.documentName("getCustomer")
            .variable("id", 1)
            .execute()
            .path("customerById.id").entity(Long.class).isEqualTo(1L)
            .path("customerById.firstName").entity(String.class).isEqualTo("John");
    }
}
```

### Estimated Time: 2-3 days

---

## 📚 Module 05: SOAP API - Implementation Plan

### Overview
Contract-first SOAP API using Spring Web Services for legacy enterprise integration.

### Key Concepts
- WSDL (Web Services Description Language)
- XSD (XML Schema Definition)
- SOAP Envelope/Header/Body
- Contract-first design
- JAXB (Java XML Binding)
- XML validation
- SOAP Fault handling
- WS-Security

### Architecture
```
SOAP Client
  ↓ XML SOAP Request
SOAP Endpoint (@Endpoint)
  ↓ Unmarshal XML
Service Layer
  ↓
Repository Layer
  ↓
Database
```

### Files to Create (25 files)

#### Core Application
1. `SoapApiApplication.java`
2. `application.yml`

#### XSD Schemas
3. `orders.xsd` - Order service schema
4. `customers.xsd` - Customer service schema

#### Generated JAXB Classes (from XSD)
5. `GetOrderRequest.java` - Auto-generated
6. `GetOrderResponse.java` - Auto-generated
7. `CreateOrderRequest.java` - Auto-generated
8. `CreateOrderResponse.java` - Auto-generated
9. `OrderType.java` - Auto-generated
10. `CustomerType.java` - Auto-generated

#### Entities
11. `CustomerEntity.java`
12. `OrderEntity.java`
13. `OrderItemEntity.java`

#### Repositories
14. `CustomerRepository.java`
15. `OrderRepository.java`

#### Services
16. `CustomerService.java`
17. `OrderService.java`

#### SOAP Endpoints
18. `OrderEndpoint.java` - @Endpoint with @PayloadRoot
19. `CustomerEndpoint.java` - SOAP operations

#### Configuration
20. `WebServiceConfig.java` - WSDL generation
21. `SoapSecurityConfig.java` - WS-Security

#### Exception Handling
22. `SoapFaultHandler.java` - SOAP fault mapping

#### Tests
23. `OrderEndpointTest.java` - SOAP integration tests

### Key Implementation Details

#### 1. XSD Schema
```xml
<?xml version="1.0" encoding="UTF-8"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
           xmlns:tns="http://apiarchlab.com/orders"
           targetNamespace="http://apiarchlab.com/orders"
           elementFormDefault="qualified">

    <xs:element name="getOrderRequest">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="orderId" type="xs:long"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>

    <xs:element name="getOrderResponse">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="order" type="tns:order"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>

    <xs:complexType name="order">
        <xs:sequence>
            <xs:element name="orderId" type="xs:long"/>
            <xs:element name="orderNumber" type="xs:string"/>
            <xs:element name="customerName" type="xs:string"/>
            <xs:element name="totalAmount" type="xs:decimal"/>
            <xs:element name="status" type="xs:string"/>
        </xs:sequence>
    </xs:complexType>
</xs:schema>
```

#### 2. SOAP Endpoint
```java
@Endpoint
public class OrderEndpoint {
    private static final String NAMESPACE_URI = "http://apiarchlab.com/orders";
    
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getOrderRequest")
    @ResponsePayload
    public GetOrderResponse getOrder(@RequestPayload GetOrderRequest request) {
        Order order = orderService.getOrderById(request.getOrderId());
        
        GetOrderResponse response = new GetOrderResponse();
        response.setOrder(mapToOrderType(order));
        return response;
    }
    
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "createOrderRequest")
    @ResponsePayload
    public CreateOrderResponse createOrder(@RequestPayload CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        
        CreateOrderResponse response = new CreateOrderResponse();
        response.setOrder(mapToOrderType(order));
        return response;
    }
}
```

#### 3. WebService Configuration
```java
@Configuration
@EnableWs
public class WebServiceConfig extends WsConfigurerAdapter {
    
    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
            ApplicationContext context) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(context);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }
    
    @Bean(name = "orders")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema ordersSchema) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("OrdersPort");
        wsdl11Definition.setLocationUri("/ws");
        wsdl11Definition.setTargetNamespace("http://apiarchlab.com/orders");
        wsdl11Definition.setSchema(ordersSchema);
        return wsdl11Definition;
    }
    
    @Bean
    public XsdSchema ordersSchema() {
        return new SimpleXsdSchema(new ClassPathResource("xsd/orders.xsd"));
    }
}
```

#### 4. SOAP Fault Handling
```java
@Component
public class SoapFaultHandler extends SoapFaultMappingExceptionResolver {
    
    @Override
    protected void customizeFault(Object endpoint, Exception ex, SoapFault fault) {
        if (ex instanceof ResourceNotFoundException) {
            fault.setFaultCode(SoapFaultDefinition.SERVER);
            fault.setFaultStringOrReason("Resource not found: " + ex.getMessage());
        } else {
            fault.setFaultCode(SoapFaultDefinition.SERVER);
            fault.setFaultStringOrReason("Internal server error");
        }
    }
}
```

### Estimated Time: 2-3 days

---

## 📚 Module 06: gRPC API - Implementation Plan

### Overview
High-performance RPC using gRPC with Protocol Buffers for service-to-service communication.

### Key Concepts
- Protocol Buffers (.proto)
- Unary RPC
- Server streaming
- Client streaming
- Bidirectional streaming
- Stub generation
- Channel management
- Interceptors
- Metadata
- Deadline/Timeout
- mTLS

### Architecture
```
gRPC Client
  ↓ HTTP/2 + Protobuf
gRPC Service (Generated Stub)
  ↓
Service Implementation
  ↓
Repository Layer
  ↓
Database
```

### Files to Create (20 files)

#### Core Application
1. `GrpcApplication.java`
2. `application.yml`

#### Proto Definitions
3. `order_service.proto` - Service definition
4. `customer_service.proto` - Service definition
5. `common.proto` - Common types

#### Generated Classes (from .proto)
6. `OrderServiceGrpc.java` - Auto-generated
7. `CustomerServiceGrpc.java` - Auto-generated
8. `OrderProto.java` - Auto-generated messages

#### Service Implementations
9. `OrderServiceImpl.java` - extends OrderServiceGrpc.OrderServiceImplBase
10. `CustomerServiceImpl.java` - gRPC service

#### Entities
11. `CustomerEntity.java`
12. `OrderEntity.java`

#### Repositories
13. `CustomerRepository.java`
14. `OrderRepository.java`

#### Business Services
15. `CustomerService.java`
16. `OrderService.java`

#### Mappers
17. `ProtoMapper.java` - Entity to Proto conversion

#### Configuration
18. `GrpcServerConfig.java`
19. `GrpcClientConfig.java`

#### Interceptors
20. `LoggingInterceptor.java` - Request/response logging
21. `AuthInterceptor.java` - Authentication

#### Tests
22. `OrderServiceTest.java` - gRPC service tests
23. `GrpcIntegrationTest.java`

### Key Implementation Details

#### 1. Proto Definition
```protobuf
syntax = "proto3";

package com.apiarchlab.grpc;

option java_multiple_files = true;
option java_package = "com.apiarchlab.grpc.proto";

service OrderService {
  // Unary RPC
  rpc GetOrder (GetOrderRequest) returns (OrderResponse);
  
  // Server streaming
  rpc ListOrders (ListOrdersRequest) returns (stream OrderResponse);
  
  // Client streaming
  rpc CreateOrders (stream CreateOrderRequest) returns (CreateOrdersResponse);
  
  // Bidirectional streaming
  rpc ProcessOrders (stream OrderRequest) returns (stream OrderResponse);
}

message GetOrderRequest {
  int64 order_id = 1;
}

message OrderResponse {
  int64 order_id = 1;
  string order_number = 2;
  string customer_name = 3;
  double total_amount = 4;
  OrderStatus status = 5;
  repeated OrderItem items = 6;
}

message OrderItem {
  string product_name = 1;
  int32 quantity = 2;
  double price = 3;
}

enum OrderStatus {
  PENDING = 0;
  CONFIRMED = 1;
  SHIPPED = 2;
  DELIVERED = 3;
  CANCELLED = 4;
}

message CreateOrderRequest {
  int64 customer_id = 1;
  repeated OrderItem items = 2;
}

message ListOrdersRequest {
  int32 page = 1;
  int32 size = 2;
}

message CreateOrdersResponse {
  int32 created_count = 1;
}
```

#### 2. gRPC Service Implementation
```java
@GrpcService
public class OrderServiceImpl extends OrderServiceGrpc.OrderServiceImplBase {
    
    // Unary RPC
    @Override
    public void getOrder(GetOrderRequest request, 
                        StreamObserver<OrderResponse> responseObserver) {
        try {
            Order order = orderService.getOrderById(request.getOrderId());
            OrderResponse response = protoMapper.toProto(order);
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.NOT_FOUND
                .withDescription("Order not found")
                .asRuntimeException());
        }
    }
    
    // Server streaming
    @Override
    public void listOrders(ListOrdersRequest request,
                          StreamObserver<OrderResponse> responseObserver) {
        List<Order> orders = orderService.getAllOrders(
            request.getPage(), request.getSize());
        
        orders.forEach(order -> {
            OrderResponse response = protoMapper.toProto(order);
            responseObserver.onNext(response);
        });
        
        responseObserver.onCompleted();
    }
    
    // Client streaming
    @Override
    public StreamObserver<CreateOrderRequest> createOrders(
            StreamObserver<CreateOrdersResponse> responseObserver) {
        
        return new StreamObserver<CreateOrderRequest>() {
            private int count = 0;
            
            @Override
            public void onNext(CreateOrderRequest request) {
                orderService.createOrder(request);
                count++;
            }
            
            @Override
            public void onError(Throwable t) {
                log.error("Error creating orders", t);
            }
            
            @Override
            public void onCompleted() {
                CreateOrdersResponse response = CreateOrdersResponse.newBuilder()
                    .setCreatedCount(count)
                    .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }
}
```

#### 3. gRPC Client
```java
@Service
public class OrderGrpcClient {
    private final OrderServiceGrpc.OrderServiceBlockingStub blockingStub;
    private final OrderServiceGrpc.OrderServiceStub asyncStub;
    
    public OrderGrpcClient(ManagedChannel channel) {
        this.blockingStub = OrderServiceGrpc.newBlockingStub(channel);
        this.asyncStub = OrderServiceGrpc.newStub(channel);
    }
    
    public OrderResponse getOrder(Long orderId) {
        GetOrderRequest request = GetOrderRequest.newBuilder()
            .setOrderId(orderId)
            .build();
        
        return blockingStub
            .withDeadlineAfter(5, TimeUnit.SECONDS)
            .getOrder(request);
    }
    
    public void listOrdersAsync(Consumer<OrderResponse> onNext) {
        ListOrdersRequest request = ListOrdersRequest.newBuilder()
            .setPage(0)
            .setSize(10)
            .build();
        
        asyncStub.listOrders(request, new StreamObserver<OrderResponse>() {
            @Override
            public void onNext(OrderResponse response) {
                onNext.accept(response);
            }
            
            @Override
            public void onError(Throwable t) {
                log.error("Error listing orders", t);
            }
            
            @Override
            public void onCompleted() {
                log.info("Listing completed");
            }
        });
    }
}
```

#### 4. Interceptor
```java
@Component
public class LoggingInterceptor implements ServerInterceptor {
    
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        
        log.info("gRPC method called: {}", call.getMethodDescriptor().getFullMethodName());
        
        return next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
            @Override
            public void sendMessage(RespT message) {
                log.info("Sending response: {}", message);
                super.sendMessage(message);
            }
        }, headers);
    }
}
```

### Estimated Time: 3-4 days

---

## 📚 Module 07: WebSocket API - Implementation Plan

### Overview
Real-time bidirectional communication using WebSocket and STOMP for live updates.

### Key Concepts
- WebSocket handshake
- STOMP protocol
- Message broker
- Topic (broadcast)
- Queue (point-to-point)
- User-specific messages
- Connection lifecycle
- Heartbeat
- Error handling

### Architecture
```
Browser/Mobile Client
  ↓ WebSocket Connection
WebSocket Gateway
  ↓ STOMP
Message Broker (In-memory/RabbitMQ)
  ↓
Subscribers (Topics/Queues)
```

### Files to Create (18 files)

#### Core Application
1. `WebSocketApplication.java`
2. `application.yml`

#### Configuration
3. `WebSocketConfig.java` - WebSocket + STOMP config
4. `WebSocketSecurityConfig.java` - Security

#### Controllers
5. `OrderWebSocketController.java` - @MessageMapping
6. `NotificationWebSocketController.java`

#### Services
7. `OrderService.java`
8. `NotificationService.java`
9. `WebSocketMessageService.java` - Send messages

#### DTOs
10. `OrderUpdateMessage.java`
11. `NotificationMessage.java`
12. `ChatMessage.java`

#### Event Listeners
13. `WebSocketEventListener.java` - Connection events

#### Entities
14. `OrderEntity.java`
15. `NotificationEntity.java`

#### Repositories
16. `OrderRepository.java`
17. `NotificationRepository.java`

#### Tests
18. `WebSocketIntegrationTest.java`

### Key Implementation Details

#### 1. WebSocket Configuration
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for topics
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefix for messages from client
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefix for user-specific messages
        config.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOrigins("*")
            .withSockJS();
    }
}
```

#### 2. WebSocket Controller
```java
@Controller
public class OrderWebSocketController {
    
    // Client sends to /app/order-status
    // Server broadcasts to /topic/orders
    @MessageMapping("/order-status")
    @SendTo("/topic/orders")
    public OrderUpdateMessage updateOrderStatus(OrderUpdateMessage message) {
        log.info("Received order update: {}", message);
        
        // Process order update
        orderService.updateOrderStatus(message.getOrderId(), message.getStatus());
        
        // Broadcast to all subscribers
        return message;
    }
    
    // Send to specific user
    @MessageMapping("/order-notification")
    public void sendOrderNotification(OrderNotificationRequest request, 
                                     SimpMessageHeaderAccessor headerAccessor) {
        String username = headerAccessor.getUser().getName();
        
        OrderUpdateMessage message = orderService.getOrderUpdate(request.getOrderId());
        
        // Send to /user/{username}/queue/notifications
        messagingTemplate.convertAndSendToUser(
            username, 
            "/queue/notifications", 
            message
        );
    }
}
```

#### 3. Programmatic Message Sending
```java
@Service
public class WebSocketMessageService {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    // Broadcast to all subscribers of /topic/orders
    public void broadcastOrderUpdate(OrderUpdateMessage message) {
        messagingTemplate.convertAndSend("/topic/orders", message);
    }
    
    // Send to specific user
    public void sendNotificationToUser(String username, NotificationMessage message) {
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/notifications",
            message
        );
    }
    
    // Send to all users
    public void broadcastSystemAlert(String message) {
        messagingTemplate.convertAndSend("/topic/alerts", message);
    }
}
```

#### 4. Event Listener
```java
@Component
public class WebSocketEventListener {
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        log.info("New WebSocket connection: {}", sessionId);
    }
    
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = headerAccessor.getUser().getName();
        log.info("WebSocket disconnected: {} (user: {})", sessionId, username);
    }
}
```

#### 5. JavaScript Client Example
```javascript
// Connect to WebSocket
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe to order updates
    stompClient.subscribe('/topic/orders', function(message) {
        const orderUpdate = JSON.parse(message.body);
        console.log('Order update:', orderUpdate);
        updateUI(orderUpdate);
    });
    
    // Subscribe to user-specific notifications
    stompClient.subscribe('/user/queue/notifications', function(message) {
        const notification = JSON.parse(message.body);
        showNotification(notification);
    });
});

// Send message
function updateOrderStatus(orderId, status) {
    stompClient.send('/app/order-status', {}, JSON.stringify({
        orderId: orderId,
        status: status,
        timestamp: new Date().toISOString()
    }));
}
```

### Use Cases
1. **Live Order Tracking** - Real-time order status updates
2. **Chat Application** - Customer support chat
3. **Live Dashboard** - Real-time metrics and KPIs
4. **Notifications** - Push notifications to users
5. **Collaborative Editing** - Multiple users editing same data

### Estimated Time: 2-3 days

---

## 📚 Module 08: API Gateway Integration - Implementation Plan

### Overview
Unified API Gateway integrating all previous modules with routing, security, and monitoring.

### Key Concepts
- API Gateway pattern
- Route configuration
- Load balancing
- Circuit breaker
- Rate limiting
- Authentication/Authorization
- Request/Response transformation
- Centralized logging
- Metrics aggregation

### Architecture
```
Frontend/Mobile App
  ↓
API Gateway (Spring Cloud Gateway)
  ├─→ REST API (Module 01)
  ├─→ WebFlux API (Module 03)
  ├─→ GraphQL API (Module 04)
  ├─→ SOAP API (Module 05)
  ├─→ gRPC API (Module 06)
  └─→ WebSocket API (Module 07)
```

### Files to Create (15 files)

#### Core Application
1. `ApiGatewayApplication.java`
2. `application.yml` - Gateway routes

#### Configuration
3. `GatewayConfig.java` - Route configuration
4. `SecurityConfig.java` - JWT authentication
5. `CorsConfig.java` - CORS configuration

#### Filters
6. `AuthenticationFilter.java` - JWT validation
7. `LoggingFilter.java` - Request/response logging
8. `RateLimitFilter.java` - Rate limiting

#### Circuit Breaker
9. `CircuitBreakerConfig.java`
10. `FallbackController.java` - Fallback responses

#### Load Balancer
11. `LoadBalancerConfig.java`

#### Monitoring
12. `MetricsConfig.java`
13. `HealthCheckController.java`

#### Tests
14. `GatewayRoutingTest.java`
15. `SecurityFilterTest.java`

### Key Implementation Details

#### 1. Gateway Routes Configuration
```yaml
spring:
  cloud:
    gateway:
      routes:
        # REST API
        - id: rest-api
          uri: http://localhost:8081
          predicates:
            - Path=/api/rest/**
          filters:
            - StripPrefix=2
            - name: CircuitBreaker
              args:
                name: restApiCircuitBreaker
                fallbackUri: forward:/fallback/rest
        
        # WebFlux API
        - id: webflux-api
          uri: http://localhost:8082
          predicates:
            - Path=/api/reactive/**
          filters:
            - StripPrefix=2
        
        # GraphQL API
        - id: graphql-api
          uri: http://localhost:8083
          predicates:
            - Path=/graphql/**
        
        # SOAP API
        - id: soap-api
          uri: http://localhost:8084
          predicates:
            - Path=/ws/**
        
        # gRPC API (HTTP/2)
        - id: grpc-api
          uri: http://localhost:8085
          predicates:
            - Path=/grpc/**
        
        # WebSocket API
        - id: websocket-api
          uri: ws://localhost:8086
          predicates:
            - Path=/ws-live/**
```

#### 2. Authentication Filter
```java
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Skip authentication for public endpoints
        if (isPublicEndpoint(request.getPath().value())) {
            return chain.filter(exchange);
        }
        
        // Extract JWT token
        String token = extractToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        // Add user info to request headers
        String username = jwtUtil.extractUsername(token);
        ServerHttpRequest modifiedRequest = request.mutate()
            .header("X-User-Id", username)
            .build();
        
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }
    
    @Override
    public int getOrder() {
        return -100; // High priority
    }
}
```

#### 3. Circuit Breaker Configuration
```java
@Configuration
public class CircuitBreakerConfig {
    
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
            .circuitBreakerConfig(CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(5)
                .build())
            .timeLimiterConfig(TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5))
                .build())
            .build());
    }
}
```

#### 4. Rate Limiting
```java
@Configuration
public class RateLimitConfig {
    
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            return Mono.just(userId != null ? userId : "anonymous");
        };
    }
    
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20); // 10 requests per second, burst of 20
    }
}
```

#### 5. Fallback Controller
```java
@RestController
@RequestMapping("/fallback")
public class FallbackController {
    
    @GetMapping("/rest")
    public ResponseEntity<Map<String, String>> restFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "Service temporarily unavailable",
                "message", "Please try again later"
            ));
    }
    
    @GetMapping("/graphql")
    public ResponseEntity<Map<String, String>> graphqlFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "error", "GraphQL service unavailable",
                "message", "Fallback response"
            ));
    }
}
```

### Integration Testing
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayIntegrationTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @Test
    void shouldRouteToRestApi() {
        webTestClient.get()
            .uri("/api/rest/customers")
            .header("Authorization", "Bearer " + validToken)
            .exchange()
            .expectStatus().isOk();
    }
    
    @Test
    void shouldReturnUnauthorizedWithoutToken() {
        webTestClient.get()
            .uri("/api/rest/customers")
            .exchange()
            .expectStatus().isUnauthorized();
    }
    
    @Test
    void shouldTriggerCircuitBreaker() {
        // Simulate service down
        for (int i = 0; i < 20; i++) {
            webTestClient.get()
                .uri("/api/rest/customers")
                .exchange();
        }
        
        // Should get fallback response
        webTestClient.get()
            .uri("/api/rest/customers")
            .exchange()
            .expectStatus().is5xxServerError()
            .expectBody()
            .jsonPath("$.error").isEqualTo("Service temporarily unavailable");
    }
}
```

### Estimated Time: 3-4 days

---

## 🎯 Implementation Priority

### Phase 1 (Weeks 1-2): Core Reactive
1. ✅ Module 03: WebFlux API - **START HERE**
   - Foundation for reactive programming
   - Required for understanding Modules 04-07

### Phase 2 (Weeks 3-4): Modern APIs
2. Module 04: GraphQL API
3. Module 06: gRPC API

### Phase 3 (Weeks 5-6): Legacy & Real-time
4. Module 05: SOAP API
5. Module 07: WebSocket API

### Phase 4 (Week 7): Integration
6. Module 08: API Gateway

---

## 📊 Estimated Timeline

| Phase | Modules | Duration | Cumulative |
|-------|---------|----------|------------|
| Foundation | 00-02 | ✅ Complete | - |
| Core Reactive | 03 | 2-3 days | 2-3 days |
| Modern APIs | 04, 06 | 5-7 days | 7-10 days |
| Legacy & Real-time | 05, 07 | 4-6 days | 11-16 days |
| Integration | 08 | 3-4 days | 14-20 days |

**Total Estimated Time**: 3-4 weeks (part-time) or 2-3 weeks (full-time)

---

## 🛠️ Development Setup

### Prerequisites
```bash
# Java 17+
java -version

# Maven 3.8+
mvn -version

# Docker (for databases, message brokers)
docker --version

# Postman or curl (for testing)
```

### Build All Modules
```bash
cd /Users/nvenugopal/Documents/Workspace/java-api-architecture-lab
mvn clean install
```

### Run Specific Module
```bash
# Module 03
mvn spring-boot:run -pl 03-webflux-api

# Module 04
mvn spring-boot:run -pl 04-graphql-api

# Module 06
mvn spring-boot:run -pl 06-grpc-api
```

---

## 📚 Learning Resources

### Spring WebFlux
- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
- [Project Reactor](https://projectreactor.io/docs)
- [R2DBC Documentation](https://r2dbc.io/)

### GraphQL
- [Spring GraphQL](https://spring.io/projects/spring-graphql)
- [GraphQL Java](https://www.graphql-java.com/)
- [DataLoader Pattern](https://github.com/graphql-java/java-dataloader)

### SOAP
- [Spring Web Services](https://spring.io/projects/spring-ws)
- [JAXB Tutorial](https://www.baeldung.com/jaxb)

### gRPC
- [gRPC Java](https://grpc.io/docs/languages/java/)
- [Protocol Buffers](https://developers.google.com/protocol-buffers)

### WebSocket
- [Spring WebSocket](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket)
- [STOMP Protocol](https://stomp.github.io/)

### API Gateway
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [Resilience4j](https://resilience4j.readme.io/)

---

## 🎓 Next Steps

1. **Review this guide** - Understand the overall architecture
2. **Start with Module 03** - WebFlux is the foundation
3. **Follow the implementation plans** - Each module has detailed steps
4. **Test as you go** - Write tests for each component
5. **Document your learnings** - Keep notes on challenges and solutions

---

## 📞 Support

For questions or issues:
1. Check module-specific README files
2. Review implementation examples
3. Consult official documentation
4. Test with provided examples

---

**Ready to build world-class APIs!** 🚀