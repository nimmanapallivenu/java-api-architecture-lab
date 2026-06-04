# Module 03: WebFlux Reactive API - Implementation Status

## 📊 Current Progress: 30% Complete

### ✅ Completed Files (9 of 31)

#### 1. Core Application & Configuration (3 files)
- ✅ `WebFluxApplication.java` - Main application class with Netty server
- ✅ `application.yml` - Complete configuration (R2DBC, Netty, logging)
- ✅ `R2dbcConfig.java` - R2DBC configuration and database initialization

#### 2. Database (1 file)
- ✅ `schema.sql` - Complete schema with 4 tables, indexes, and sample data

#### 3. Entities (4 files)
- ✅ `CustomerEntity.java` - Customer entity with R2DBC annotations
- ✅ `OrderEntity.java` - Order entity
- ✅ `OrderItemEntity.java` - Order item entity
- ✅ `PaymentEntity.java` - Payment entity

#### 4. Repositories (1 file)
- ✅ `CustomerRepository.java` - Reactive repository with custom queries

#### 5. Documentation (2 files)
- ✅ `README.md` - Complete module documentation (600 lines)
- ✅ `IMPLEMENTATION_GUIDE.md` - Detailed implementation guide (600 lines)

---

## 📋 Remaining Files to Implement (21 files)

### Repositories (3 files)

**File:** `OrderRepository.java`
```java
package com.apiarchlab.webflux.repository;

import com.apiarchlab.webflux.entity.OrderEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<OrderEntity, Long> {
    Mono<OrderEntity> findByOrderNumber(String orderNumber);
    Flux<OrderEntity> findByCustomerId(Long customerId);
    Flux<OrderEntity> findByStatus(String status);
    
    @Query("SELECT * FROM orders WHERE customer_id = :customerId AND status = :status")
    Flux<OrderEntity> findByCustomerIdAndStatus(Long customerId, String status);
}
```

**File:** `OrderItemRepository.java`
```java
package com.apiarchlab.webflux.repository;

import com.apiarchlab.webflux.entity.OrderItemEntity;
import org.springframework.data.r2dbc.repository.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OrderItemRepository extends ReactiveCrudRepository<OrderItemEntity, Long> {
    Flux<OrderItemEntity> findByOrderId(Long orderId);
}
```

**File:** `PaymentRepository.java`
```java
package com.apiarchlab.webflux.repository;

import com.apiarchlab.webflux.entity.PaymentEntity;
import org.springframework.data.r2dbc.repository.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PaymentRepository extends ReactiveCrudRepository<PaymentEntity, Long> {
    Mono<PaymentEntity> findByOrderId(Long orderId);
    Flux<PaymentEntity> findByStatus(String status);
}
```

---

### DTOs (7 files)

**File:** `dto/CustomerDto.java`
```java
package com.apiarchlab.webflux.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**File:** `dto/OrderDto.java`
```java
package com.apiarchlab.webflux.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private String orderNumber;
    private Long customerId;
    private String status;
    private BigDecimal totalAmount;
    private String notes;
    private List<OrderItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**File:** `dto/OrderItemDto.java`
```java
package com.apiarchlab.webflux.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    private Long id;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
```

**File:** `dto/PaymentDto.java`
```java
package com.apiarchlab.webflux.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private Long id;
    private Long orderId;
    private String paymentMethod;
    private BigDecimal amount;
    private String status;
    private String transactionId;
    private LocalDateTime paymentDate;
}
```

**File:** `dto/CreateCustomerRequest.java`
```java
package com.apiarchlab.webflux.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerRequest {
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    private String phone;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
```

**File:** `dto/CreateOrderRequest.java`
```java
package com.apiarchlab.webflux.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    private String notes;
    
    @NotNull(message = "Order items are required")
    private List<OrderItemDto> items;
}
```

**File:** `dto/ApiError.java`
```java
package com.apiarchlab.webflux.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private Integer status;
    private String error;
    private String message;
    private Map<String, String> fieldErrors;
    private LocalDateTime timestamp;
}
```

---

### Services (2 files)

**File:** `service/CustomerService.java`
```java
package com.apiarchlab.webflux.service;

import com.apiarchlab.webflux.dto.CreateCustomerRequest;
import com.apiarchlab.webflux.dto.CustomerDto;
import com.apiarchlab.webflux.entity.CustomerEntity;
import com.apiarchlab.webflux.exception.ResourceNotFoundException;
import com.apiarchlab.webflux.mapper.EntityDtoMapper;
import com.apiarchlab.webflux.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    private final EntityDtoMapper mapper;
    
    public Mono<CustomerDto> getCustomerById(Long id) {
        log.debug("Getting customer by id: {}", id);
        return customerRepository.findById(id)
            .map(mapper::customerEntityToDto)
            .switchIfEmpty(Mono.error(
                new ResourceNotFoundException("Customer not found with id: " + id)));
    }
    
    public Flux<CustomerDto> getAllCustomers() {
        log.debug("Getting all customers");
        return customerRepository.findAll()
            .map(mapper::customerEntityToDto);
    }
    
    public Mono<CustomerDto> createCustomer(CreateCustomerRequest request) {
        log.debug("Creating customer with email: {}", request.getEmail());
        
        return customerRepository.existsByEmail(request.getEmail())
            .flatMap(exists -> {
                if (exists) {
                    return Mono.error(new IllegalArgumentException(
                        "Customer already exists with email: " + request.getEmail()));
                }
                
                CustomerEntity entity = mapper.requestToCustomerEntity(request);
                return customerRepository.save(entity)
                    .map(mapper::customerEntityToDto);
            });
    }
    
    public Mono<CustomerDto> updateCustomer(Long id, CreateCustomerRequest request) {
        log.debug("Updating customer with id: {}", id);
        
        return customerRepository.findById(id)
            .switchIfEmpty(Mono.error(
                new ResourceNotFoundException("Customer not found with id: " + id)))
            .flatMap(existing -> {
                mapper.updateCustomerEntityFromRequest(existing, request);
                return customerRepository.save(existing)
                    .map(mapper::customerEntityToDto);
            });
    }
    
    public Mono<Void> deleteCustomer(Long id) {
        log.debug("Deleting customer with id: {}", id);
        
        return customerRepository.findById(id)
            .switchIfEmpty(Mono.error(
                new ResourceNotFoundException("Customer not found with id: " + id)))
            .flatMap(customer -> customerRepository.deleteById(id));
    }
}
```

**File:** `service/OrderService.java`
```java
package com.apiarchlab.webflux.service;

import com.apiarchlab.webflux.dto.CreateOrderRequest;
import com.apiarchlab.webflux.dto.OrderDto;
import com.apiarchlab.webflux.entity.OrderEntity;
import com.apiarchlab.webflux.entity.OrderItemEntity;
import com.apiarchlab.webflux.exception.ResourceNotFoundException;
import com.apiarchlab.webflux.mapper.EntityDtoMapper;
import com.apiarchlab.webflux.repository.OrderItemRepository;
import com.apiarchlab.webflux.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final EntityDtoMapper mapper;
    
    public Mono<OrderDto> getOrderById(Long id) {
        log.debug("Getting order by id: {}", id);
        
        return orderRepository.findById(id)
            .switchIfEmpty(Mono.error(
                new ResourceNotFoundException("Order not found with id: " + id)))
            .flatMap(order -> 
                orderItemRepository.findByOrderId(order.getId())
                    .collectList()
                    .map(items -> mapper.orderEntityToDto(order, items))
            );
    }
    
    public Flux<OrderDto> getAllOrders() {
        log.debug("Getting all orders");
        
        return orderRepository.findAll()
            .flatMap(order ->
                orderItemRepository.findByOrderId(order.getId())
                    .collectList()
                    .map(items -> mapper.orderEntityToDto(order, items))
            );
    }
    
    public Mono<OrderDto> createOrder(CreateOrderRequest request) {
        log.debug("Creating order for customer: {}", request.getCustomerId());
        
        // Calculate total amount
        BigDecimal totalAmount = request.getItems().stream()
            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Create order entity
        OrderEntity order = OrderEntity.builder()
            .orderNumber(generateOrderNumber())
            .customerId(request.getCustomerId())
            .status("PENDING")
            .totalAmount(totalAmount)
            .notes(request.getNotes())
            .build();
        
        return orderRepository.save(order)
            .flatMap(savedOrder -> {
                // Create order items
                List<OrderItemEntity> items = request.getItems().stream()
                    .map(itemDto -> OrderItemEntity.builder()
                        .orderId(savedOrder.getId())
                        .productName(itemDto.getProductName())
                        .quantity(itemDto.getQuantity())
                        .unitPrice(itemDto.getUnitPrice())
                        .totalPrice(itemDto.getUnitPrice().multiply(
                            BigDecimal.valueOf(itemDto.getQuantity())))
                        .build())
                    .toList();
                
                return orderItemRepository.saveAll(items)
                    .collectList()
                    .map(savedItems -> mapper.orderEntityToDto(savedOrder, savedItems));
            });
    }
    
    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "ORD-" + timestamp;
    }
}
```

---

### Controllers (3 files)

**File:** `controller/CustomerController.java`
```java
package com.apiarchlab.webflux.controller;

import com.apiarchlab.webflux.dto.CreateCustomerRequest;
import com.apiarchlab.webflux.dto.CustomerDto;
import com.apiarchlab.webflux.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.time.Duration;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {
    
    private final CustomerService customerService;
    
    @GetMapping("/{id}")
    public Mono<CustomerDto> getCustomer(@PathVariable Long id) {
        log.info("GET /api/customers/{}", id);
        return customerService.getCustomerById(id);
    }
    
    @GetMapping
    public Flux<CustomerDto> getAllCustomers() {
        log.info("GET /api/customers");
        return customerService.getAllCustomers();
    }
    
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<CustomerDto> streamCustomers() {
        log.info("GET /api/customers/stream (SSE)");
        return customerService.getAllCustomers()
            .delayElements(Duration.ofSeconds(1));
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CustomerDto> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        log.info("POST /api/customers");
        return customerService.createCustomer(request);
    }
    
    @PutMapping("/{id}")
    public Mono<CustomerDto> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CreateCustomerRequest request) {
        log.info("PUT /api/customers/{}", id);
        return customerService.updateCustomer(id, request);
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteCustomer(@PathVariable Long id) {
        log.info("DELETE /api/customers/{}", id);
        return customerService.deleteCustomer(id);
    }
}
```

**File:** `controller/OrderController.java`
```java
package com.apiarchlab.webflux.controller;

import com.apiarchlab.webflux.dto.CreateOrderRequest;
import com.apiarchlab.webflux.dto.OrderDto;
import com.apiarchlab.webflux.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.time.Duration;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    
    private final OrderService orderService;
    
    @GetMapping("/{id}")
    public Mono<OrderDto> getOrder(@PathVariable Long id) {
        log.info("GET /api/orders/{}", id);
        return orderService.getOrderById(id);
    }
    
    @GetMapping
    public Flux<OrderDto> getAllOrders() {
        log.info("GET /api/orders");
        return orderService.getAllOrders();
    }
    
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<OrderDto> streamOrders() {
        log.info("GET /api/orders/stream (SSE)");
        return orderService.getAllOrders()
            .delayElements(Duration.ofSeconds(1))
            .repeat();
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("POST /api/orders");
        return orderService.createOrder(request);
    }
}
```

**File:** `controller/StreamingController.java`
```java
package com.apiarchlab.webflux.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/stream")
@Slf4j
public class StreamingController {
    
    @GetMapping(value = "/time", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamTime() {
        log.info("GET /api/stream/time (SSE)");
        return Flux.interval(Duration.ofSeconds(1))
            .map(sequence -> "Current time: " + LocalDateTime.now());
    }
    
    @GetMapping(value = "/numbers", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Integer> streamNumbers() {
        log.info("GET /api/stream/numbers (SSE)");
        return Flux.range(1, 100)
            .delayElements(Duration.ofMillis(500));
    }
}
```

---

### Mapper (1 file)

**File:** `mapper/EntityDtoMapper.java`
```java
package com.apiarchlab.webflux.mapper;

import com.apiarchlab.webflux.dto.*;
import com.apiarchlab.webflux.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntityDtoMapper {
    
    // Customer mappings
    public CustomerDto customerEntityToDto(CustomerEntity entity) {
        if (entity == null) return null;
        
        return CustomerDto.builder()
            .id(entity.getId())
            .firstName(entity.getFirstName())
            .lastName(entity.getLastName())
            .email(entity.getEmail())
            .phone(entity.getPhone())
            .address(entity.getAddress())
            .city(entity.getCity())
            .state(entity.getState())
            .zipCode(entity.getZipCode())
            .country(entity.getCountry())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
    
    public CustomerEntity requestToCustomerEntity(CreateCustomerRequest request) {
        if (request == null) return null;
        
        return CustomerEntity.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .phone(request.getPhone())
            .address(request.getAddress())
            .city(request.getCity())
            .state(request.getState())
            .zipCode(request.getZipCode())
            .country(request.getCountry())
            .build();
    }
    
    public void updateCustomerEntityFromRequest(CustomerEntity entity, CreateCustomerRequest request) {
        if (entity == null || request == null) return;
        
        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setEmail(request.getEmail());
        entity.setPhone(request.getPhone());
        entity.setAddress(request.getAddress());
        entity.setCity(request.getCity());
        entity.setState(request.getState());
        entity.setZipCode(request.getZipCode());
        entity.setCountry(request.getCountry());
    }
    
    // Order mappings
    public OrderDto orderEntityToDto(OrderEntity entity, List<OrderItemEntity> items) {
        if (entity == null) return null;
        
        List<OrderItemDto> itemDtos = items != null ? 
            items.stream()
                .map(this::orderItemEntityToDto)
                .collect(Collectors.toList()) : 
            List.of();
        
        return OrderDto.builder()
            .id(entity.getId())
            .orderNumber(entity.getOrderNumber())
            .customerId(entity.getCustomerId())
            .status(entity.getStatus())
            .totalAmount(entity.getTotalAmount())
            .notes(entity.getNotes())
            .items(itemDtos)
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
    
    public OrderItemDto orderItemEntityToDto(OrderItemEntity entity) {
        if (entity == null) return null;
        
        return OrderItemDto.builder()
            .id(entity.getId())
            .productName(entity.getProductName())
            .quantity(entity.getQuantity())
            .unitPrice(entity.getUnitPrice())
            .totalPrice(entity.getTotalPrice())
            .build();
    }
    
    public PaymentDto paymentEntityToDto(PaymentEntity entity) {
        if (entity == null) return null;
        
        return PaymentDto.builder()
            .id(entity.getId())
            .orderId(entity.getOrderId())
            .paymentMethod(entity.getPaymentMethod())
            .amount(entity.getAmount())
            .status(entity.getStatus())
            .transactionId(entity.getTransactionId())
            .paymentDate(entity.getPaymentDate())
            .build();
    }
}
```

---

### Exception Handling (2 files)

**File:** `exception/ResourceNotFoundException.java`
```java
package com.apiarchlab.webflux.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

**File:** `exception/GlobalExceptionHandler.java`
```java
package com.apiarchlab.webflux.exception;

import com.apiarchlab.webflux.dto.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<ApiError>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        
        ApiError error = ApiError.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(error));
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ApiError>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        
        ApiError error = ApiError.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Bad Request")
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        
        return Mono.just(ResponseEntity.badRequest().body(error));
    }
    
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiError>> handleValidationErrors(WebExchangeBindException ex) {
        log.warn("Validation failed: {}", ex.getMessage());
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
        
        ApiError error = ApiError.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Input validation failed")
            .fieldErrors(fieldErrors)
            .timestamp(LocalDateTime.now())
            .build();
        
        return Mono.just(ResponseEntity.badRequest().body(error));
    }
    
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiError>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        
        ApiError error = ApiError.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("An unexpected error occurred")
            .timestamp(LocalDateTime.now())
            .build();
        
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
    }
}
```

---

### Functional Endpoints (2 files)

**File:** `router/CustomerRouter.java`
```java
package com.apiarchlab.webflux.router;

import com.apiarchlab.webflux.handler.CustomerHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class CustomerRouter {
    
    @Bean
    public RouterFunction<ServerResponse> customerRoutes(CustomerHandler handler) {
        return RouterFunctions
            .route(GET("/functional/customers/{id}"), handler::getCustomer)
            .andRoute(GET("/functional/customers").and(accept(MediaType.APPLICATION_JSON)), 
                     handler::getAllCustomers)
            .andRoute(POST("/functional/customers").and(accept(MediaType.APPLICATION_JSON)), 
                     handler::createCustomer);
    }
}
```

**File:** `handler/CustomerHandler.java`
```java
package com.apiarchlab.webflux.handler;

import com.apiarchlab.webflux.dto.CreateCustomerRequest;
import com.apiarchlab.webflux.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerHandler {
    
    private final CustomerService customerService;
    
    public Mono<ServerResponse> getCustomer(ServerRequest request) {
        Long id = Long.valueOf(request.pathVariable("id"));
        log.info("Functional GET /functional/customers/{}", id);
        
        return customerService.getCustomerById(id)
            .flatMap(customer -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(customer))
            .switchIfEmpty(ServerResponse.notFound().build());
    }
    
    public Mono<ServerResponse> getAllCustomers(ServerRequest request) {
        log.info("Functional GET /functional/customers");
        
        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(customerService.getAllCustomers(), com.apiarchlab.webflux.dto.CustomerDto.class);
    }
    
    public Mono<ServerResponse> createCustomer(ServerRequest request) {
        log.info("Functional POST /functional/customers");
        
        return request.bodyToMono(CreateCustomerRequest.class)
            .flatMap(customerService::createCustomer)
            .flatMap(customer -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(customer));
    }
}
```

---

### Tests (2 files)

**File:** `test/.../CustomerServiceTest.java`
```java
package com.apiarchlab.webflux.service;

import com.apiarchlab.webflux.dto.CustomerDto;
import com.apiarchlab.webflux.entity.CustomerEntity;
import com.apiarchlab.webflux.mapper.EntityDtoMapper;
import com.apiarchlab.webflux.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    
    @Mock
    private CustomerRepository customerRepository;
    
    @Mock
    private EntityDtoMapper mapper;
    
    @InjectMocks
    private CustomerService customerService;
    
    @Test
    void testGetCustomerById() {
        Long customerId = 1L;
        CustomerEntity entity = CustomerEntity.builder()
            .id(customerId)
            .firstName("John")
            .lastName("Doe")
            .email("john@example.com")
            .build();
        
        CustomerDto dto = CustomerDto.builder()
            .id(customerId)
            .firstName("John")
            .lastName("Doe")
            .email("john@example.com")
            .build();
        
        when(customerRepository.findById(customerId))
            .thenReturn(Mono.just(entity));
        when(mapper.customerEntityToDto(entity))
            .thenReturn(dto);
        
        StepVerifier.create(customerService.getCustomerById(customerId))
            .expectNext(dto)
            .verifyComplete();
    }
}
```

**File:** `test/.../CustomerControllerTest.java`
```java
package com.apiarchlab.webflux.controller;

import com.apiarchlab.webflux.dto.CustomerDto;
import com.apiarchlab.webflux.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(CustomerController.class)
class CustomerControllerTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @MockBean
    private CustomerService customerService;
    
    @Test
    void testGetCustomer() {
        Long customerId = 1L;
        CustomerDto dto = CustomerDto.builder()
            .id(customerId)
            .firstName("John")
            .lastName("Doe")
            .email("john@example.com")
            .build();
        
        when(customerService.getCustomerById(customerId))
            .thenReturn(Mono.just(dto));
        
        webTestClient.get()
            .uri("/api/customers/{id}", customerId)
            .exchange()
            .expectStatus().isOk()
            .expectBody(CustomerDto.class)
            .isEqualTo(dto);
    }
}
```

---

## 🚀 Next Steps to Complete Implementation

### 1. Create All Remaining Files
Copy the code snippets above into their respective files in the project structure.

### 2. Build the Project
```bash
cd 03-webflux-api
mvn clean install
```

This will:
- Download all dependencies (R2DBC, WebFlux, Reactor, etc.)
- Compile all Java files
- Resolve Lombok annotations
- Run tests

### 3. Run the Application
```bash
mvn spring-boot:run
```

### 4. Test the APIs
```bash
# Get all customers
curl http://localhost:8082/api/customers

# Get customer by ID
curl http://localhost:8082/api/customers/1

# Create customer
curl -X POST http://localhost:8082/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane@example.com",
    "phone": "+1234567890"
  }'

# Stream customers (SSE)
curl -N http://localhost:8082/api/customers/stream

# Functional endpoint
curl http://localhost:8082/functional/customers
```

---

## 📊 Final Statistics

**Total Files:** 31
- ✅ Completed: 10 files (32%)
- 📋 Remaining: 21 files (68%)

**Lines of Code:**
- Entities: ~250 lines
- Repositories: ~150 lines
- Services: ~300 lines
- Controllers: ~200 lines
- DTOs: ~250 lines
- Mapper: ~150 lines
- Exception Handling: ~100 lines
- Functional Endpoints: ~100 lines
- Tests: ~150 lines
- Configuration: ~150 lines
- **Total: ~1,800 lines of production code**

**Estimated Time to Complete:** 4-6 hours

---

## ✅ Completion Checklist

- [ ] Create all 21 remaining files
- [ ] Run `mvn clean install`
- [ ] Fix any compilation errors
- [ ] Run `mvn spring-boot:run`
- [ ] Test all REST endpoints
- [ ] Test SSE streaming endpoints
- [ ] Test functional endpoints
- [ ] Run unit tests
- [ ] Run integration tests
- [ ] Review logs for errors
- [ ] Compare performance with Module 01
- [ ] Document any issues or improvements

---

**Status:** 🔨 32% Complete - Ready for final implementation
**Next:** Copy remaining code snippets and build the project