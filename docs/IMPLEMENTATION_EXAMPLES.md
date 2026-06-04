# Implementation Examples

Quick reference implementations for each API style using the Order domain.

## 1️⃣ REST API (Module 01) - Spring MVC

### Controller
```java
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<List<OrderDto>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.findAll(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        return orderService.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderDto created = orderService.create(request);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderDto> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderRequest request) {
        return ResponseEntity.ok(orderService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/payment")
    public ResponseEntity<PaymentDto> processPayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentRequest request) {
        PaymentDto payment = orderService.processPayment(id, request);
        return ResponseEntity.ok(payment);
    }
}
```

### Service
```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository itemRepository;
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    public OrderDto create(CreateOrderRequest request) {
        // Validate customer exists
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(request.getCustomerId()));

        // Create order
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        // Add items
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CreateOrderItemRequest itemReq : request.getItems()) {
            OrderItem item = new OrderItem();
            item.setProductId(itemReq.getProductId());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(itemReq.getUnitPrice());
            order.addItem(item);
            subtotal = subtotal.add(item.getTotalPrice());
        }

        // Calculate totals
        order.setSubtotalAmount(subtotal);
        order.setTaxAmount(subtotal.multiply(new BigDecimal("0.1")));
        order.setTotalAmount(order.calculateTotal());

        // Save
        Order saved = orderRepository.save(order);

        // Send notification
        notificationService.notifyOrderCreated(saved);

        return mapToDto(saved);
    }

    public PaymentDto processPayment(Long orderId, PaymentRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStatusException("Cannot process payment for non-pending order");
        }

        // Process payment
        Payment payment = paymentService.authorize(order.getTotalAmount(), request);

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            notificationService.notifyPaymentReceived(order);
        } else {
            notificationService.notifyPaymentFailed(order, payment);
        }

        return mapPaymentToDto(payment);
    }
}
```

### Repository
```java
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
    
    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.createdAt DESC")
    Page<Order> findByStatus(@Param("status") OrderStatus status, Pageable pageable);
}
```

---

## 2️⃣ REST + WebClient (Module 02) - Async Downstream Calls

### Service (Async WebClient)
```java
@Service
@RequiredArgsConstructor
public class AsyncOrderService {
    private final OrderRepository orderRepository;
    private final WebClient webClient;

    public Order createOrderWithAsyncCalls(CreateOrderRequest request) {
        // 1. Create order synchronously
        Order order = createOrder(request);

        // 2. Call payment service asynchronously (fire and forget)
        CompletableFuture.runAsync(() -> {
            try {
                PaymentDto payment = webClient.post()
                        .uri("http://payment-service/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(new PaymentRequest(order.getId(), order.getTotalAmount()))
                        .retrieve()
                        .bodyToMono(PaymentDto.class)
                        .block(Duration.ofSeconds(5));

                if (payment.getStatus() == PaymentStatus.COMPLETED) {
                    order.setStatus(OrderStatus.CONFIRMED);
                    orderRepository.save(order);
                }
            } catch (Exception e) {
                log.error("Payment processing failed", e);
            }
        });

        // 3. Call notification service asynchronously (fire and forget)
        CompletableFuture.runAsync(() -> {
            try {
                webClient.post()
                        .uri("http://notification-service/api/notifications")
                        .bodyValue(new NotificationRequest(order.getCustomerId(), "Order created"))
                        .retrieve()
                        .toBodilessEntity()
                        .block(Duration.ofSeconds(3));
            } catch (Exception e) {
                log.error("Notification failed", e);
            }
        });

        return order;
    }

    // WebClient bean configuration
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://localhost")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(new ExchangeFilterFunction() {
                    @Override
                    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
                        log.info(">>> {} {}", request.getMethod(), request.getUrl());
                        return next.exchange(request)
                                .doOnNext(response -> 
                                    log.info("<<< {} {}", request.getMethod(), response.getStatusCode())
                                );
                    }
                })
                .build();
    }
}
```

---

## 3️⃣ WebFlux (Module 03) - Fully Reactive

### Controller (Reactive)
```java
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class ReactiveOrderController {
    private final ReactiveOrderService orderService;

    @GetMapping
    public Flux<OrderDto> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return orderService.findAll(page, size);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<OrderDto>> getOrderById(@PathVariable Long id) {
        return orderService.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<OrderDto>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        return orderService.create(request)
                .map(dto -> ResponseEntity.status(201).body(dto));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteOrder(@PathVariable Long id) {
        return orderService.delete(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }
}
```

### Service (Reactive)
```java
@Service
@RequiredArgsConstructor
public class ReactiveOrderService {
    private final OrderRepository orderRepository;
    private final WebClient webClient;

    public Mono<OrderDto> create(CreateOrderRequest request) {
        return Mono.just(request)
                // Validate customer
                .filterWhen(req -> customerService.exists(req.getCustomerId())
                        .switchIfEmpty(Mono.error(
                            new CustomerNotFoundException(req.getCustomerId())
                        ))
                )
                // Create order
                .flatMap(req -> createOrder(req))
                // Process payment asynchronously
                .flatMap(order -> processPaymentAsync(order))
                // Send notification asynchronously
                .flatMap(order -> sendNotificationAsync(order))
                .map(this::mapToDto);
    }

    private Mono<Order> createOrder(CreateOrderRequest request) {
        return Mono.fromCallable(() -> {
            Order order = new Order();
            order.setCustomerId(request.getCustomerId());
            order.setStatus(OrderStatus.PENDING);
            // ... initialize order
            return order;
        }).subscribeOn(Schedulers.boundedElastic()) // Offload to separate scheduler
                .flatMap(order -> orderRepository.save(order));
    }

    private Mono<Order> processPaymentAsync(Order order) {
        return webClient.post()
                .uri("http://payment-service/api/payments")
                .bodyValue(new PaymentRequest(order.getId(), order.getTotalAmount()))
                .retrieve()
                .bodyToMono(PaymentDto.class)
                .flatMap(payment -> {
                    if (payment.getStatus() == PaymentStatus.COMPLETED) {
                        order.setStatus(OrderStatus.CONFIRMED);
                        return orderRepository.save(order);
                    }
                    return Mono.just(order);
                })
                .onErrorResume(e -> {
                    log.error("Payment failed", e);
                    return Mono.just(order); // Proceed even if payment fails
                });
    }

    private Mono<Order> sendNotificationAsync(Order order) {
        return webClient.post()
                .uri("http://notification-service/api/notifications")
                .bodyValue(new NotificationRequest(order.getCustomerId(), "Order created"))
                .retrieve()
                .toBodilessEntity()
                .then(Mono.just(order))
                .onErrorResume(e -> {
                    log.error("Notification failed", e);
                    return Mono.just(order);
                });
    }
}
```

---

## 4️⃣ GraphQL (Module 04)

### GraphQL Schema
```graphql
# schema.graphqls
type Query {
    orders(page: Int, size: Int): [Order!]!
    order(id: ID!): Order
    customer(id: ID!): Customer
}

type Mutation {
    createOrder(input: CreateOrderInput!): Order!
    updateOrder(id: ID!, input: UpdateOrderInput!): Order!
    deleteOrder(id: ID!): Boolean!
    processPayment(orderId: ID!, input: PaymentInput!): PaymentResponse!
}

type Order {
    id: ID!
    orderNumber: String!
    status: OrderStatus!
    customer: Customer!
    items: [OrderItem!]!
    payment: Payment
    totalAmount: Float!
    createdAt: String!
}

type Customer {
    id: ID!
    firstName: String!
    lastName: String!
    email: String!
    orders(page: Int): [Order!]!
}

type OrderItem {
    id: ID!
    productId: String!
    productName: String!
    quantity: Int!
    unitPrice: Float!
}

type Payment {
    id: ID!
    status: PaymentStatus!
    amount: Float!
    processedAt: String
}

type PaymentResponse {
    success: Boolean!
    payment: Payment
    message: String
}

enum OrderStatus {
    PENDING
    CONFIRMED
    PROCESSING
    SHIPPED
    DELIVERED
    CANCELLED
}

enum PaymentStatus {
    PENDING
    AUTHORIZED
    COMPLETED
    FAILED
}

input CreateOrderInput {
    customerId: ID!
    items: [CreateOrderItemInput!]!
}

input CreateOrderItemInput {
    productId: String!
    quantity: Int!
    unitPrice: Float!
}

input PaymentInput {
    paymentMethod: String!
    cardToken: String
}
```

### GraphQL Controller
```java
@Controller
@RequiredArgsConstructor
public class OrderGraphQLController {
    private final OrderService orderService;
    private final CustomerService customerService;
    private final PaymentService paymentService;

    @QueryMapping
    public List<OrderDto> orders(
            @Argument(name = "page", defaultValue = "0") int page,
            @Argument(name = "size", defaultValue = "10") int size) {
        return orderService.findAll(page, size);
    }

    @QueryMapping
    public OrderDto order(@Argument Long id) {
        return orderService.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @MutationMapping
    public OrderDto createOrder(@Argument CreateOrderInput input) {
        return orderService.create(mapToRequest(input));
    }

    @MutationMapping
    public PaymentResponse processPayment(
            @Argument Long orderId,
            @Argument PaymentInput input) {
        try {
            PaymentDto payment = paymentService.process(orderId, mapToRequest(input));
            return new PaymentResponse(true, payment, "Payment processed");
        } catch (Exception e) {
            return new PaymentResponse(false, null, e.getMessage());
        }
    }

    // Field resolver for Customer within Order
    @SchemaMapping
    public CustomerDto customer(OrderDto order) {
        return customerService.findById(order.getCustomerId());
    }

    // Field resolver for Orders within Customer
    @SchemaMapping
    public List<OrderDto> orders(CustomerDto customer) {
        return orderService.findByCustomerId(customer.getId());
    }
}
```

---

## 5️⃣ gRPC (Module 06)

### Protocol Buffer Definition
```protobuf
# orders.proto
syntax = "proto3";

package com.apiarchlab.grpc;

service OrderService {
    rpc CreateOrder(CreateOrderRequest) returns (OrderResponse) {}
    rpc GetOrder(GetOrderRequest) returns (OrderResponse) {}
    rpc ListOrders(ListOrdersRequest) returns (stream OrderResponse) {}
    rpc ProcessPayment(PaymentRequest) returns (PaymentResponse) {}
}

message CreateOrderRequest {
    int64 customer_id = 1;
    repeated OrderItemRequest items = 2;
}

message OrderItemRequest {
    string product_id = 1;
    int32 quantity = 2;
    float unit_price = 3;
}

message GetOrderRequest {
    int64 id = 1;
}

message ListOrdersRequest {
    int32 page = 1;
    int32 size = 2;
}

message OrderResponse {
    int64 id = 1;
    string order_number = 2;
    int64 customer_id = 3;
    string status = 4;
    repeated OrderItem items = 5;
    float total_amount = 6;
    int64 created_at = 7;
}

message OrderItem {
    string product_id = 1;
    string product_name = 2;
    int32 quantity = 3;
    float unit_price = 4;
}

message PaymentRequest {
    int64 order_id = 1;
    string payment_method = 2;
    string card_token = 3;
}

message PaymentResponse {
    bool success = 1;
    string transaction_id = 2;
    string status = 3;
    string message = 4;
}
```

### gRPC Service Implementation
```java
@GrpcService
@RequiredArgsConstructor
public class OrderGrpcService extends OrderServiceGrpc.OrderServiceImplBase {
    private final OrderService orderService;
    private final PaymentService paymentService;

    @Override
    public void createOrder(CreateOrderRequest request, 
                          StreamObserver<OrderResponse> responseObserver) {
        try {
            OrderDto orderDto = orderService.create(mapToRequest(request));
            responseObserver.onNext(mapToProto(orderDto));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asException());
        }
    }

    @Override
    public void getOrder(GetOrderRequest request,
                        StreamObserver<OrderResponse> responseObserver) {
        try {
            OrderDto order = orderService.findById(request.getId())
                    .orElseThrow(() -> new OrderNotFoundException(request.getId()));
            responseObserver.onNext(mapToProto(order));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Order not found")
                    .asException());
        }
    }

    @Override
    public void listOrders(ListOrdersRequest request,
                          StreamObserver<OrderResponse> responseObserver) {
        try {
            List<OrderDto> orders = orderService.findAll(request.getPage(), request.getSize());
            for (OrderDto order : orders) {
                responseObserver.onNext(mapToProto(order));
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asException());
        }
    }

    @Override
    public void processPayment(PaymentRequest request,
                              StreamObserver<PaymentResponse> responseObserver) {
        try {
            PaymentDto payment = paymentService.authorize(
                    request.getOrderId(),
                    request.getPaymentMethod(),
                    request.getCardToken()
            );

            PaymentResponse response = PaymentResponse.newBuilder()
                    .setSuccess(payment.getStatus() == PaymentStatus.COMPLETED)
                    .setTransactionId(payment.getTransactionId())
                    .setStatus(payment.getStatus().toString())
                    .setMessage("Payment processed successfully")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asException());
        }
    }
}
```

---

## 6️⃣ WebSocket (Module 07) - Real-Time Notifications

### WebSocket Configuration
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/notifications")
                .setAllowedOrigins("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Authentication can happen here
                }
                return message;
            }
        });
    }
}
```

### WebSocket Controller
```java
@Controller
@RequiredArgsConstructor
public class NotificationController {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final OrderService orderService;

    @MessageMapping("/orders/create")
    public void createOrder(CreateOrderRequest request, Principal principal) {
        // Create order
        OrderDto order = orderService.create(request);

        // Broadcast to all subscribers
        simpMessagingTemplate.convertAndSend(
                "/topic/orders",
                new OrderNotification("ORDER_CREATED", order)
        );

        // Send to specific user
        simpMessagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/orders",
                new OrderNotification("ORDER_CREATED_FOR_YOU", order)
        );
    }

    @MessageMapping("/orders/{id}/subscribe")
    public void subscribeToOrder(@DestinationVariable Long id, Principal principal) {
        // User subscribed to order updates
        simpMessagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/order/" + id,
                new OrderNotification("SUBSCRIBED", null)
        );
    }

    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        // Broadcast order status changes to all subscribers
        simpMessagingTemplate.convertAndSend(
                "/topic/orders/" + event.getOrderId(),
                new OrderNotification("STATUS_CHANGED", event.getOrder())
        );
    }
}
```

### Client-Side JavaScript
```javascript
let stompClient = null;

function connect() {
    const socket = new SockJS('/ws/notifications');
    stompClient = Stomp.over(socket);
    
    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame.server);
        
        // Subscribe to order updates
        stompClient.subscribe('/topic/orders', function(message) {
            console.log('Order update:', JSON.parse(message.body));
        });
        
        // Subscribe to user-specific queue
        stompClient.subscribe('/user/queue/orders', function(message) {
            console.log('Your order update:', JSON.parse(message.body));
        });
    });
}

function createOrder(orderData) {
    stompClient.send("/app/orders/create", {}, JSON.stringify(orderData));
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
}
```

---

## 🎯 Key Patterns

### Error Handling Pattern
```java
// REST
@ExceptionHandler(OrderNotFoundException.class)
public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException e) {
    return ResponseEntity.status(404).body(new ErrorResponse("ORDER_NOT_FOUND", e.getMessage()));
}

// WebFlux
@ExceptionHandler(OrderNotFoundException.class)
public Mono<ResponseEntity<ErrorResponse>> handleOrderNotFound(OrderNotFoundException e) {
    return Mono.just(ResponseEntity.status(404)
            .body(new ErrorResponse("ORDER_NOT_FOUND", e.getMessage())));
}

// GraphQL
@QueryMapping -> throws exception -> GraphQL returns in "errors" field

// gRPC
responseObserver.onError(Status.NOT_FOUND
        .withDescription("Order not found")
        .asException());
```

### Database Access Pattern
```java
// REST (blocking)
Order order = orderRepository.findById(id).orElseThrow();

// WebFlux (reactive)
Mono<Order> order = orderRepository.findById(id)
        .switchIfEmpty(Mono.error(new OrderNotFoundException(id)));
```

---

**These examples provide the foundation for implementing each API style with the Order Management domain. Customize and extend based on your specific requirements!**

