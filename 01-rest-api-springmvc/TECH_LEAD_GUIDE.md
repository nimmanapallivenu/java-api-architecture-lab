# 🎓 Module 01: REST API with Spring MVC - Tech Lead Complete Guide

## 📚 Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Core Concepts Deep Dive](#core-concepts-deep-dive)
3. [Request Flow Diagrams](#request-flow-diagrams)
4. [Implementation Patterns](#implementation-patterns)
5. [Testing Strategies](#testing-strategies)
6. [Performance & Optimization](#performance--optimization)
7. [Production Considerations](#production-considerations)
8. [Troubleshooting Guide](#troubleshooting-guide)

---

## 🏗️ Architecture Overview

### Layered Architecture Pattern

This module implements a classic **3-tier layered architecture**:

```
┌─────────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                            │
│  • REST Controllers (@RestController)                            │
│  • Request/Response handling                                     │
│  • Input validation (@Valid)                                     │
│  • HTTP status codes                                             │
│  • OpenAPI documentation                                         │
└────────────────────────┬─────────────────────────────────────────┘
                         │
┌────────────────────────▼─────────────────────────────────────────┐
│                     BUSINESS LAYER                               │
│  • Service classes (@Service)                                    │
│  • Business logic                                                │
│  • Transaction management (@Transactional)                       │
│  • DTO ↔ Entity mapping                                         │
│  • Exception throwing                                            │
└────────────────────────┬─────────────────────────────────────────┘
                         │
┌────────────────────────▼─────────────────────────────────────────┐
│                   PERSISTENCE LAYER                              │
│  • Repository interfaces (@Repository)                           │
│  • Spring Data JPA                                               │
│  • Database queries                                              │
│  • Entity management                                             │
└────────────────────────┬─────────────────────────────────────────┘
                         │
┌────────────────────────▼─────────────────────────────────────────┐
│                      DATA LAYER                                  │
│  • JPA Entities (@Entity)                                        │
│  • Database tables                                               │
│  • Relationships                                                 │
│  • Constraints                                                   │
└──────────────────────────────────────────────────────────────────┘
```

### Component Interaction Diagram

```
┌──────────────┐
│   Client     │
│  (Browser,   │
│   Mobile,    │
│   Postman)   │
└──────┬───────┘
       │ HTTP Request (JSON)
       │
┌──────▼────────────────────────────────────────────────────────┐
│                    Spring MVC Framework                        │
│  ┌──────────────────────────────────────────────────────┐    │
│  │  DispatcherServlet                                    │    │
│  │  • Receives all HTTP requests                         │    │
│  │  • Routes to appropriate controller                   │    │
│  │  • Handles response serialization                     │    │
│  └──────────────────────────────────────────────────────┘    │
└───────────────────────────┬───────────────────────────────────┘
                            │
┌───────────────────────────▼───────────────────────────────────┐
│              @RestController (CustomerController)             │
│  ┌────────────────────────────────────────────────────┐      │
│  │  @GetMapping("/{id}")                              │      │
│  │  public ResponseEntity<CustomerDto> getCustomer()  │      │
│  │  {                                                  │      │
│  │      return customerService.getCustomerById(id);   │      │
│  │  }                                                  │      │
│  └────────────────────────────────────────────────────┘      │
└───────────────────────────┬───────────────────────────────────┘
                            │
┌───────────────────────────▼───────────────────────────────────┐
│                @Service (CustomerService)                     │
│  ┌────────────────────────────────────────────────────┐      │
│  │  @Transactional                                     │      │
│  │  public CustomerDto getCustomerById(Long id) {      │      │
│  │      CustomerEntity entity =                        │      │
│  │          repository.findById(id)                    │      │
│  │              .orElseThrow(() -> new                 │      │
│  │                  ResourceNotFoundException());      │      │
│  │      return mapper.entityToDto(entity);             │      │
│  │  }                                                  │      │
│  └────────────────────────────────────────────────────┘      │
└───────────────────────────┬───────────────────────────────────┘
                            │
┌───────────────────────────▼───────────────────────────────────┐
│           @Repository (CustomerRepository)                    │
│  ┌────────────────────────────────────────────────────┐      │
│  │  public interface CustomerRepository               │      │
│  │      extends JpaRepository<CustomerEntity, Long> { │      │
│  │                                                     │      │
│  │      Optional<CustomerEntity> findById(Long id);   │      │
│  │      Optional<CustomerEntity> findByEmail(String); │      │
│  │      Page<CustomerEntity> findAll(Pageable);       │      │
│  │  }                                                  │      │
│  └────────────────────────────────────────────────────┘      │
└───────────────────────────┬───────────────────────────────────┘
                            │
┌───────────────────────────▼───────────────────────────────────┐
│                    JPA/Hibernate                              │
│  • Translates method calls to SQL                            │
│  • Manages entity lifecycle                                  │
│  • Handles transactions                                      │
│  • Caches entities (1st level cache)                         │
└───────────────────────────┬───────────────────────────────────┘
                            │
┌───────────────────────────▼───────────────────────────────────┐
│                  H2 Database (In-Memory)                      │
│  Tables:                                                      │
│  • customers                                                  │
│  • orders                                                     │
│  • order_items                                                │
│  • payments                                                   │
└───────────────────────────────────────────────────────────────┘
```

---

## 🔍 Core Concepts Deep Dive

### Concept 1: @RestController & Request Mapping

#### Understanding @RestController

```java
@RestController  // = @Controller + @ResponseBody
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {
    
    private final CustomerService customerService;
    
    // GET /api/customers/{id}
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getCustomer(@PathVariable Long id) {
        log.info("Fetching customer with id: {}", id);
        CustomerDto customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(customer);
    }
}
```

**Key Points:**
- `@RestController` automatically converts return values to JSON
- `@RequestMapping` defines base path for all methods
- `@PathVariable` extracts URL parameters
- `ResponseEntity` provides full HTTP response control

#### HTTP Method Mapping

| Annotation | HTTP Method | Use Case | Example |
|------------|-------------|----------|---------|
| `@GetMapping` | GET | Retrieve resource(s) | Get customer by ID |
| `@PostMapping` | POST | Create new resource | Create customer |
| `@PutMapping` | PUT | Update entire resource | Update customer |
| `@PatchMapping` | PATCH | Partial update | Update email only |
| `@DeleteMapping` | DELETE | Delete resource | Delete customer |

---

### Concept 2: DTO vs Entity Pattern

#### Why Separate DTOs and Entities?

**Problem without DTOs:**
```java
// ❌ BAD: Exposing entity directly
@GetMapping("/{id}")
public CustomerEntity getCustomer(@PathVariable Long id) {
    return customerRepository.findById(id).orElseThrow();
}

// Issues:
// 1. Exposes database structure
// 2. Circular references cause JSON errors
// 3. Can't hide sensitive fields
// 4. Database changes break API
```

**Solution with DTOs:**
```java
// ✅ GOOD: Using DTO
@GetMapping("/{id}")
public ResponseEntity<CustomerDto> getCustomer(@PathVariable Long id) {
    CustomerEntity entity = customerRepository.findById(id).orElseThrow();
    CustomerDto dto = mapper.entityToDto(entity);
    return ResponseEntity.ok(dto);
}

// Benefits:
// 1. API contract independent from database
// 2. No circular references
// 3. Can hide/transform fields
// 4. Database changes don't affect API
```

#### Entity Example

```java
@Entity
@Table(name = "customers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    // Relationship - causes circular reference if exposed directly
    @OneToMany(mappedBy = "customer")
    private List<OrderEntity> orders;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

#### DTO Example

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    
    // No 'orders' field - prevents circular references
    // Use separate endpoint: GET /customers/{id}/orders
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Computed field (not in database)
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
```

---

### Concept 3: Request Validation

#### Validation Flow

```
Client Request
     ↓
JSON Deserialization
     ↓
@Valid Annotation Triggers Validation
     ↓
Jakarta Bean Validation Checks
     ↓
┌─────────────────────────┐
│ Validation Successful?  │
└───────┬─────────────────┘
        │
   ┌────┴────┐
   │         │
  YES       NO
   │         │
   │    MethodArgumentNotValidException
   │         ↓
   │    @RestControllerAdvice catches
   │         ↓
   │    Returns 400 Bad Request
   │    with field errors
   │
   ↓
Controller Method Executes
```

#### Validation Annotations

```java
public class CreateCustomerRequest {
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50)
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @Pattern(regexp = "^[+]?[0-9]{10,}$", 
             message = "Phone must be valid")
    private String phone;
}
```

#### Exception Handler

```java
@RestControllerAdvice
public class RestExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        });
        
        ApiError apiError = ApiError.builder()
            .status(400)
            .message("Validation failed")
            .fieldErrors(errors)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.badRequest().body(apiError);
    }
}
```

---

### Concept 4: Pagination & Sorting

#### Why Pagination?

```
Without Pagination:
GET /api/customers
→ Returns ALL customers (could be millions)
→ Slow response time
→ High memory usage
→ Poor user experience

With Pagination:
GET /api/customers?page=0&size=10
→ Returns only 10 customers
→ Fast response time
→ Low memory usage
→ Better user experience
```

#### Implementation

```java
// Controller
@GetMapping
public ResponseEntity<Page<CustomerDto>> getAllCustomers(
        Pageable pageable) {
    return ResponseEntity.ok(customerService.getAllCustomers(pageable));
}

// Service
@Transactional(readOnly = true)
public Page<CustomerDto> getAllCustomers(Pageable pageable) {
    return customerRepository.findAll(pageable)
            .map(mapper::entityToDto);
}

// Repository (Spring Data JPA provides this automatically)
public interface CustomerRepository 
        extends JpaRepository<CustomerEntity, Long> {
    // findAll(Pageable) is inherited from JpaRepository
}
```

#### Usage Examples

```bash
# Page 0, size 10 (first 10 records)
GET /api/customers?page=0&size=10

# Page 1, size 20 (records 21-40)
GET /api/customers?page=1&size=20

# Sort by createdAt descending
GET /api/customers?page=0&size=10&sort=createdAt,desc

# Multiple sort fields
GET /api/customers?page=0&size=10&sort=lastName,asc&sort=firstName,asc
```

#### Response Format

```json
{
  "content": [
    {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com"
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "pageNumber": 0,
    "pageSize": 10,
    "offset": 0
  },
  "totalElements": 100,
  "totalPages": 10,
  "last": false,
  "first": true,
  "number": 0,
  "size": 10,
  "numberOfElements": 10,
  "empty": false
}
```

---

### Concept 5: Filtering & Custom Queries

#### Query Methods (Spring Data JPA)

```java
public interface CustomerRepository 
        extends JpaRepository<CustomerEntity, Long> {
    
    // Derived query methods (Spring generates SQL)
    Optional<CustomerEntity> findByEmail(String email);
    List<CustomerEntity> findByCity(String city);
    List<CustomerEntity> findByLastNameContaining(String lastName);
    Page<CustomerEntity> findByCountry(String country, Pageable pageable);
    
    // Custom JPQL query
    @Query("SELECT c FROM CustomerEntity c WHERE c.email = :email OR c.phone = :phone")
    Optional<CustomerEntity> findByEmailOrPhone(
        @Param("email") String email,
        @Param("phone") String phone
    );
    
    // Native SQL query
    @Query(value = "SELECT * FROM customers WHERE created_at > :date", 
           nativeQuery = true)
    List<CustomerEntity> findCreatedAfter(@Param("date") LocalDateTime date);
}
```

#### Controller with Filtering

```java
@GetMapping("/search")
public ResponseEntity<Page<CustomerDto>> searchCustomers(
        @RequestParam(required = false) String city,
        @RequestParam(required = false) String country,
        Pageable pageable) {
    
    Page<CustomerDto> customers;
    
    if (city != null) {
        customers = customerService.findByCity(city, pageable);
    } else if (country != null) {
        customers = customerService.findByCountry(country, pageable);
    } else {
        customers = customerService.getAllCustomers(pageable);
    }
    
    return ResponseEntity.ok(customers);
}
```

---

### Concept 6: Transaction Management

#### Understanding @Transactional

```java
@Service
@RequiredArgsConstructor
@Transactional  // All methods are transactional by default
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    
    // Read-only transaction (optimization)
    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id) {
        OrderEntity order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return mapper.orderEntityToDto(order);
    }
    
    // Write transaction (default)
    public OrderDto createOrder(CreateOrderRequest request) {
        // All database operations in this method are in ONE transaction
        
        // 1. Create order
        OrderEntity order = new OrderEntity();
        order.setCustomerId(request.getCustomerId());
        order = orderRepository.save(order);
        
        // 2. Create payment
        PaymentEntity payment = new PaymentEntity();
        payment.setOrderId(order.getId());
        paymentRepository.save(payment);
        
        // If ANY operation fails, ALL are rolled back
        
        return mapper.orderEntityToDto(order);
    }
}
```

#### Transaction Propagation

```java
@Service
public class OrderService {
    
    // REQUIRED (default): Use existing transaction or create new
    @Transactional(propagation = Propagation.REQUIRED)
    public void method1() { }
    
    // REQUIRES_NEW: Always create new transaction
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void method2() { }
    
    // SUPPORTS: Use transaction if exists, otherwise non-transactional
    @Transactional(propagation = Propagation.SUPPORTS)
    public void method3() { }
    
    // MANDATORY: Must be called within existing transaction
    @Transactional(propagation = Propagation.MANDATORY)
    public void method4() { }
}
```

---

## 📊 Request Flow Diagrams

### Successful GET Request Flow

```
Time: 0ms
┌────────────────────────────────────────────────────────────┐
│  CLIENT                                                     │
│  GET /api/customers/1                                       │
│  Accept: application/json                                   │
└──────────────────────┬─────────────────────────────────────┘
                       │
Time: 5ms              │ HTTP Request
                       ▼
┌────────────────────────────────────────────────────────────┐
│  SPRING MVC DISPATCHER SERVLET                             │
│  • Receives request                                         │
│  • Finds @GetMapping("/{id}") in CustomerController        │
│  • Extracts path variable: id = 1                          │
└──────────────────────┬─────────────────────────────────────┘
                       │
Time: 10ms             │ Route to Controller
                       ▼
┌────────────────────────────────────────────────────────────┐
│  CONTROLLER: CustomerController.getCustomerById(1)         │
│  log.info("Fetching customer with id: 1")                  │
│  customerService.getCustomerById(1)                         │
└──────────────────────┬─────────────────────────────────────┘
                       │
Time: 15ms             │ Service Call
                       ▼
┌────────────────────────────────────────────────────────────┐
│  SERVICE: CustomerService.getCustomerById(1)               │
│  @Transactional(readOnly = true)                           │
│  customerRepository.findById(1)                             │
└──────────────────────┬─────────────────────────────────────┘
                       │
Time: 20ms             │ Repository Call
                       ▼
┌────────────────────────────────────────────────────────────┐
│  REPOSITORY: CustomerRepository.findById(1)                │
│  Spring Data JPA generates SQL:                            │
│  SELECT * FROM customers WHERE id = 1                      │
└──────────────────────┬─────────────────────────────────────┘
                       │
Time: 25ms             │ Database Query
                       ▼
┌────────────────────────────────────────────────────────────┐
│  DATABASE: H2                                              │
│  Executes query, returns row                               │
└──────────────────────┬─────────────────────────────────────┘
                       │
Time: 30ms             │ CustomerEntity
                       ▼
┌────────────────────────────────────────────────────────────┐
│  SERVICE: CustomerService                                  │
│  Optional<CustomerEntity> found                            │
│  mapper.entityToDto(entity) → CustomerDto                  │
└──────────────────────┬─────────────────────────────────────┘
                       │
Time: 35ms             │ CustomerDto
                       ▼
┌────────────────────────────────────────────────────────────┐
│  CONTROLLER: CustomerController                            │
│  ResponseEntity.ok(customerDto)                            │
│  Returns 200 OK with body                                  │
└──────────────────────┬─────────────────────────────────────┘
                       │
Time: 40ms             │ HTTP Response
                       ▼
┌────────────────────────────────────────────────────────────┐
│  SPRING MVC                                                │
│  • Serializes CustomerDto to JSON                          │
│  • Sets Content-Type: application/json                     │
│  • Sends HTTP response                                     │
└──────────────────────┬─────────────────────────────────────┘
                       │
Time: 45ms             │ JSON Response
                       ▼
┌────────────────────────────────────────────────────────────┐
│  CLIENT                                                     │
│  Receives:                                                  │
│  HTTP/1.1 200 OK                                           │
│  Content-Type: application/json                            │
│  {                                                          │
│    "id": 1,                                                │
│    "firstName": "John",                                    │
│    "lastName": "Doe",                                      │
│    "email": "john@example.com"                             │
│  }                                                          │
└─────────────────────────────────────────────────────────────┘

Total Time: ~45ms
```

### Failed Request Flow (Validation Error)

```
Time: 0ms
┌────────────────────────────────────────────────────────────┐
│  CLIENT                                                     │
│  POST /api/customers                                        │
│  {                                                          │
│    "firstName": "",           ← Invalid: blank             │
│    "email": "invalid-email"   ← Invalid: not email format  │
│  }                                                          │
└──────────────────────┬─────────────────────────────────────┘
                       │
Time: 5ms              │ HTTP Request
                       ▼
┌────────────────────────────────────────────────────────────┐
│  SPRING MVC                                                │
│  • Deserializes JSON → CreateCustomerRequest               │
│  • @Valid annotation triggers validation                   │
└──────────────────────┬─────────────────────────────────────┘
                       │
Time: 10ms             │ Validation
                       ▼
┌────────────────────────────────────────────────────────────┐
│  JAKARTA BEAN VALIDATION                                   │
│  ❌ @NotBlank on firstName fails                           │
│  ❌ @Email on email fails                                  │
│  Throws: MethodArgumentNotValidException                   │
└──────────────────────┬─────────────────────────────────────┘
                       │
Time: 15ms             │ Exception
                       ▼
┌────────────────────────────────────────────────────────────┐
│  @RestControllerAdvice: RestExceptionHandler              │
│  @ExceptionHandler(MethodArgumentNotValidException.class)  │
│  • Extracts field errors                                   │
│  • Builds ApiError response                                │
│  • Returns 400 Bad Request                                 │
└──────────────────────┬─────────────────────────────────────┘
                       │
Time: 20ms             │ Error Response
                       ▼
┌────────────────────────────────────────────────────────────┐
│  CLIENT                                                     │
│  HTTP/1.1 400 Bad Request                                  │
│  {                                                          │
│    "status": 400,                                          │
│    "error": "Validation Failed",                           │
│    "message": "Input validation failed",                   │
│    "fieldErrors": {                                        │
│      "firstName": "First name is required",                │
│      "email": "Email should be valid"                      │
│    },                                                       │
│    "timestamp": "2024-06-04T10:30:00"                      │
│  }                                                          │
└─────────────────────────────────────────────────────────────┘

Total Time: ~20ms (fast failure)
```

---

## 🧪 Testing Strategies

### Unit Testing (Service Layer)

```java
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    
    @Mock
    private CustomerRepository customerRepository;
    
    @Mock
    private EntityDtoMapper mapper;
    
    @InjectMocks
    private CustomerService customerService;
    
    @Test
    @DisplayName("Should return customer when ID exists")
    void testGetCustomerById_Success() {
        // Arrange
        Long customerId = 1L;
        CustomerEntity entity = CustomerEntity.builder()
            .id(customerId)
            .firstName("John")
            .lastName("Doe")
            .email("john@example.com")
            .build();
        
        CustomerDto expectedDto = CustomerDto.builder()
            .id(customerId)
            .firstName("John")
            .lastName("Doe")
            .email("john@example.com")
            .build();
        
        when(customerRepository.findById(customerId))
            .thenReturn(Optional.of(entity));
        when(mapper.entityToDto(entity))
            .thenReturn(expectedDto);
        
        // Act
        CustomerDto result = customerService.getCustomerById(customerId);
        
        // Assert
        assertNotNull(result);
        assertEquals(customerId, result.getId());
        assertEquals("John", result.getFirstName());
        
        verify(customerRepository).findById(customerId);
        verify(mapper).entityToDto(entity);
    }
    
    @Test
    @DisplayName("Should throw exception when customer not found")
    void testGetCustomerById_NotFound() {
        // Arrange
        Long customerId = 999L;
        when(customerRepository.findById(customerId))
            .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            customerService.getCustomerById(customerId);
        });
        
        verify(customerRepository).findById(customerId);
        verifyNoInteractions(mapper);
    }
}
```

### Integration Testing (Controller Layer)

```java
@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @DisplayName("GET /api/customers/{id} should return customer")
    void testGetCustomer() throws Exception {
        mockMvc.perform(get("/api/customers/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").exists())
            .andExpect(jsonPath("$.email").exists());
    }
    
    @Test
    @DisplayName("POST /api/customers should create customer")
    void testCreateCustomer() throws Exception {
        CreateCustomerRequest request = CreateCustomerRequest.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john@example.com")
            .phone("+1234567890")
            .build();
        
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.email").value("john@example.com"));
    }
    
    @Test
    @DisplayName("POST /api/customers with invalid data should return 400")
    void testCreateCustomer_ValidationError() throws Exception {
        CreateCustomerRequest request = CreateCustomerRequest.builder()
            .firstName("")  // Invalid: blank
            .lastName("Doe")
            .email("invalid-email")  // Invalid: not email format
            .build();
        
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.fieldErrors.firstName").exists())
            .andExpect(jsonPath("$.fieldErrors.email").exists());
    }
}
```

---

## ⚡ Performance & Optimization

### Database Query Optimization

```java
// ❌ BAD: N+1 Query Problem
@GetMapping
public List<OrderDto> getAllOrders() {
    List<OrderEntity> orders = orderRepository.findAll();
    // For each order, fetches customer separately (N+1 queries)
    return orders.stream()
        .map(order -> {
            CustomerEntity customer = customerRepository.findById(order.getCustomerId()).get();
            return mapper.toDto(order, customer);
        })
        .collect(Collectors.toList());
}

// ✅ GOOD: Use JOIN FETCH
@Query("SELECT o FROM OrderEntity o JOIN FETCH o.customer")
List<OrderEntity> findAllWithCustomer();

@GetMapping
public List<OrderDto> getAllOrders() {
    // Single query with JOIN
    List<OrderEntity> orders = orderRepository.findAllWithCustomer();
    return orders.stream()
        .map(mapper::toDto)
        .collect(Collectors.toList());
}
```

### Pagination Best Practices

```java
// ✅ Always use pagination for list endpoints
@GetMapping
public ResponseEntity<Page<CustomerDto>> getAllCustomers(
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable) {
    return ResponseEntity.ok(customerService.getAllCustomers(pageable));
}
```

### Caching (Future Enhancement)

```java
@Service
@CacheConfig(cacheNames = "customers")
public class CustomerService {
    
    @Cacheable(key = "#id")
    public CustomerDto getCustomerById(Long id) {
        // Cached after first call
        return customerRepository.findById(id)
            .map(mapper::entityToDto)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }
    
    @CacheEvict(key = "#id")
    public void deleteCustomer(Long id) {
        // Removes from cache
        customerRepository.deleteById(id);
    }
}
```

---

## 🚀 Production Considerations

### Security (To Be Added)

```java
// Add Spring Security dependency
// Configure JWT authentication
// Add role-based authorization

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer().jwt();
        return http.build();
    }
}
```

### Monitoring & Logging

```yaml
# application.yml
logging:
  level:
    com.apiarchlab: DEBUG
    org.hibernate.SQL: DEBUG
    org.springframework.web: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### Error Handling Best Practices

```java
@RestControllerAdvice
public class RestExceptionHandler {
    
    // Log all errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        
        // Don't expose internal details to client
        ApiError apiError = ApiError.builder()
            .status(500)
            .message("An unexpected error occurred")
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(500).body(apiError);
    }
}
```

---

## 🔧 Troubleshooting Guide

### Common Issues

#### Issue 1: H2 Console Not Accessible

**Problem:** Cannot access http://localhost:8080/api/h2-console

**Solution:**
```yaml
# application.yml
spring:
  h2:
    console:
      enabled: true
      path: /h2-console
```

#### Issue 2: Circular Reference in JSON

**Problem:** `JsonMappingException: Infinite recursion`

**Solution:** Use DTOs instead of entities, or use `@JsonIgnore`

```java
@Entity
public class OrderEntity {
    @OneToMany(mappedBy = "order")
    @JsonIgnore  // Prevents circular reference
    private List<OrderItemEntity> items;
}
```

#### Issue 3: Validation Not Working

**Problem:** `@Valid` not triggering validation

**Solution:** Ensure dependency is included:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

---

## 📚 Summary

This module demonstrates:
- ✅ Complete REST API with Spring MVC
- ✅ Layered architecture (Controller → Service → Repository)
- ✅ DTO pattern for API contracts
- ✅ Request validation with Jakarta Bean Validation
- ✅ Global exception handling
- ✅ Pagination and sorting
- ✅ OpenAPI/Swagger documentation
- ✅ Unit and integration testing
- ✅ Transaction management
- ✅ Best practices and patterns

**Next Steps:**
1. Review the code in detail
2. Run the application and test APIs
3. Modify and extend functionality
4. Move to Module 02 for async patterns

---

**Module Status:** ✅ Complete and Production-Ready
**Next Module:** [02-rest-reactive-webclient](../02-rest-reactive-webclient)