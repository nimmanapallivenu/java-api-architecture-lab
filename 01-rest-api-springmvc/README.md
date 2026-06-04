# Module 01: REST API with Spring MVC

A comprehensive REST API implementation for Order Management System using Spring Boot and Spring MVC.

## 📋 Concepts Covered

- ✅ **@RestController & @RequestMapping** - HTTP request handling
- ✅ **DTO vs Entity** - Data Transfer Objects vs JPA Entities
- ✅ **Request Validation** - Bean Validation (Jakarta Validation)
- ✅ **Exception Handling** - Global exception handler with @RestControllerAdvice
- ✅ **Pagination** - Spring Data pagination with Pageable
- ✅ **Sorting** - Dynamic sorting with Sort
- ✅ **Filtering** - Query params and custom repository queries
- ✅ **OpenAPI/Swagger** - API documentation with Springdoc OpenAPI
- ✅ **Unit Testing** - MockMvc and service layer tests
- ✅ **Integration Testing** - Controller integration tests
- ✅ **Actuator** - Application health and metrics endpoints
- ✅ **Spring Data JPA** - Repository pattern and query methods

## 🏗️ Architecture

```
Client (HTTP JSON)
    ↓
@RestController (CustomerController, OrderController)
    ↓
@RequestMapping (/customers, /orders)
    ↓
@Service (CustomerService, OrderService)
    ↓
Validation & Business Logic
    ↓
@Repository (CustomerRepository, OrderRepository)
    ↓
JPA Entity (CustomerEntity, OrderEntity)
    ↓
Database (H2)
```

## 📦 Project Structure

```
01-rest-api-springmvc/
├── src/main/java/com/apiarchlab/rest/
│   ├── RestApiApplication.java              # Main Spring Boot application
│   ├── controller/
│   │   ├── CustomerController.java          # REST endpoints for customers
│   │   └── OrderController.java             # REST endpoints for orders
│   ├── service/
│   │   ├── CustomerService.java             # Customer business logic
│   │   └── OrderService.java                # Order business logic
│   ├── repository/
│   │   ├── CustomerRepository.java          # JpaRepository for customers
│   │   ├── OrderRepository.java             # JpaRepository for orders
│   │   ├── OrderItemRepository.java         # JpaRepository for order items
│   │   └── PaymentRepository.java           # JpaRepository for payments
│   ├── entity/
│   │   ├── CustomerEntity.java              # JPA entity
│   │   ├── OrderEntity.java                 # JPA entity
│   │   ├── OrderItemEntity.java             # JPA entity
│   │   └── PaymentEntity.java               # JPA entity
│   ├── dto/
│   │   ├── CustomerDto.java                 # Data Transfer Object
│   │   ├── OrderDto.java                    # Data Transfer Object
│   │   ├── CreateCustomerRequest.java       # Request DTO
│   │   ├── CreateOrderRequest.java          # Request DTO
│   │   ├── PaymentDto.java                  # Response DTO
│   │   ├── PaymentRequest.java              # Request DTO
│   │   └── ApiError.java                    # Error response DTO
│   ├── mapper/
│   │   └── EntityDtoMapper.java             # Entity ↔ DTO mapping
│   ├── exception/
│   │   ├── ResourceNotFoundException.java    # Custom exception
│   │   └── BusinessException.java           # Custom exception
│   ├── enums/
│   │   ├── OrderStatus.java                 # Order status enumeration
│   │   └── PaymentStatus.java               # Payment status enumeration
│   ├── config/
│   │   ├── RestExceptionHandler.java        # @RestControllerAdvice
│   │   └── OpenApiConfig.java               # Swagger/OpenAPI config
│   └── util/
│       └── OrderNumberGenerator.java        # Order number generation
│
├── src/main/resources/
│   └── application.yml                      # Spring Boot configuration
│
├── src/test/java/com/apiarchlab/rest/
│   ├── service/
│   │   └── CustomerServiceTest.java         # Unit tests
│   └── controller/
│       └── CustomerControllerTest.java      # Integration tests
│
└── pom.xml                                  # Maven configuration
```

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+

### Build

```bash
cd 01-rest-api-springmvc
mvn clean install
```

### Run

```bash
mvn spring-boot:run
```

Server starts on: `http://localhost:8080/api`

## 📚 API Endpoints

### Customers

```
GET    /api/customers                          # List all customers (paginated)
GET    /api/customers/{id}                     # Get customer by ID
GET    /api/customers/by-email/{email}         # Get customer by email
POST   /api/customers                          # Create new customer
PUT    /api/customers/{id}                     # Update customer
DELETE /api/customers/{id}                     # Delete customer
```

### Orders

```
GET    /api/orders                             # List all orders (paginated)
GET    /api/orders/{id}                        # Get order by ID
GET    /api/orders/number/{orderNumber}        # Get order by number
GET    /api/orders/customer/{customerId}       # Get orders by customer
GET    /api/orders/status/{status}             # Get orders by status
POST   /api/orders                             # Create new order
PUT    /api/orders/{id}                        # Update pending order
POST   /api/orders/{id}/cancel                 # Cancel order
DELETE /api/orders/{id}                        # Delete order
POST   /api/orders/{id}/payment                # Process payment
```

## 📖 API Documentation

### Swagger UI
```
http://localhost:8080/api/swagger-ui.html
```

### OpenAPI JSON
```
http://localhost:8080/api/v3/api-docs
```

## 💡 Key Concepts Demonstrated

### 1. REST Controller & Mapping

```java
@RestController
@RequestMapping("/customers")
public class CustomerController {
    
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }
}
```

**Key Points:**
- `@RestController` - Automatically converts return values to JSON
- `@RequestMapping` - Maps HTTP requests to handler methods
- `@PathVariable` - Extracts path variables from URL
- `ResponseEntity` - Full control over HTTP response

### 2. DTO vs Entity Pattern

**Entity:**
```java
@Entity
@Table(name = "customers")
public class CustomerEntity {
    @Id
    @GeneratedValue
    private Long id;
    // JPA annotations for database mapping
}
```

**DTO:**
```java
public class CustomerDto {
    private Long id;
    // Simple POJO for API communication
    // No database concerns
}
```

**Benefits:**
- Entities are tied to database schema
- DTOs can be customized for API consumers
- Can expose or hide fields in API independently
- Mapper handles conversion

### 3. Request Validation

```java
public class CreateCustomerRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @Pattern(regexp = "^[+]?[0-9]{10,}$", 
             message = "Phone number should be valid")
    private String phone;
}

// In controller:
@PostMapping
public ResponseEntity<CustomerDto> createCustomer(
        @Valid @RequestBody CreateCustomerRequest request) {
    // ...
}
```

**Features:**
- `@Valid` - Triggers validation
- Standard Jakarta validation annotations
- Global exception handler processes validation errors
- Returns 400 Bad Request with field errors

### 4. Exception Handling

```java
@RestControllerAdvice
public class RestExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(
            ResourceNotFoundException ex) {
        return new ResponseEntity<>(
            new ApiError(404, ex.getMessage(), "Not Found"),
            HttpStatus.NOT_FOUND);
    }
}
```

**Benefits:**
- Centralized error handling
- Consistent error response format
- No try-catch in controllers
- Easy maintenance and updates

### 5. Pagination & Sorting

```java
@GetMapping
public ResponseEntity<Page<CustomerDto>> getAllCustomers(
        Pageable pageable) {
    return ResponseEntity.ok(customerService.getAllCustomers(pageable));
}
```

**Usage:**
```
GET /api/customers?page=0&size=10&sort=createdAt,desc
```

**Response:**
```json
{
  "content": [...],
  "pageable": {...},
  "totalElements": 100,
  "totalPages": 10,
  "number": 0
}
```

### 6. Filtering

```java
// Repository method with custom query
@Query("SELECT o FROM OrderEntity o WHERE o.customerId = :customerId AND o.status = :status")
Page<OrderEntity> findByCustomerIdAndStatus(
    @Param("customerId") Long customerId,
    @Param("status") OrderStatus status,
    Pageable pageable);
```

**Usage:**
```
GET /api/orders/customer/1?page=0&size=10
GET /api/orders/status/PENDING
```

### 7. OpenAPI/Swagger Documentation

```java
@GetMapping("/{id}")
@Operation(summary = "Get customer by ID", 
           description = "Retrieve a specific customer")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Customer found"),
    @ApiResponse(responseCode = "404", description = "Customer not found")
})
public ResponseEntity<CustomerDto> getCustomerById(
    @Parameter(description = "Customer ID") @PathVariable Long id) {
    // ...
}
```

### 8. Service Layer & Business Logic

```java
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    
    public OrderDto createOrder(CreateOrderRequest request) {
        // Validate customer exists
        if (!customerService.existsById(request.getCustomerId())) {
            throw new ResourceNotFoundException(...);
        }
        
        // Create and save order
        OrderEntity order = new OrderEntity();
        // ... build order ...
        order = orderRepository.save(order);
        
        // Map to DTO and return
        return mapper.orderEntityToDto(order);
    }
}
```

**Key Points:**
- `@Transactional` - Manages transaction boundaries
- Service contains business logic (not controller)
- Repositories handle data access
- Exceptions are thrown for validation failures

### 9. Repository Pattern with Spring Data JPA

```java
@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
    
    Optional<CustomerEntity> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT c FROM CustomerEntity c WHERE c.email = :email OR c.phone = :phone")
    Optional<CustomerEntity> findByEmailOrPhone(
        @Param("email") String email,
        @Param("phone") String phone);
}
```

**Features:**
- Extended JpaRepository - CRUD + pagination
- Query methods - Simple method names generate SQL
- @Query - Custom JPQL queries
- Type-safe - Compile-time checking

### 10. Unit Testing

```java
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    @Mock
    private CustomerRepository customerRepository;
    
    @InjectMocks
    private CustomerService customerService;
    
    @Test
    void testGetCustomerById() {
        // Arrange
        when(customerRepository.findById(1L))
            .thenReturn(Optional.of(customerEntity));
        
        // Act
        CustomerDto result = customerService.getCustomerById(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }
}
```

### 11. Integration Testing

```java
@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testGetAllCustomers() throws Exception {
        mockMvc.perform(get("/api/customers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)));
    }
}
```

### 12. Actuator & Health Endpoints

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

**Endpoints:**
```
http://localhost:8080/api/actuator/health       # Health check
http://localhost:8080/api/actuator/info         # Application info
http://localhost:8080/api/actuator/metrics      # Performance metrics
```

## 🧪 Running Tests

### Run all tests
```bash
mvn test
```

### Run specific test class
```bash
mvn test -Dtest=CustomerServiceTest
```

### Run with coverage report
```bash
mvn test jacoco:report
```

## 📊 HTTP Status Codes Used

```
200 OK              - Successful GET, PUT
201 Created         - Successful POST
204 No Content      - Successful DELETE
400 Bad Request     - Validation error
404 Not Found       - Resource not found
409 Conflict        - Business logic violation (duplicate)
500 Internal Error  - Server error
```

## 🔄 Request/Response Examples

### Create Customer

**Request:**
```bash
POST /api/customers
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phone": "+19876543210",
  "address": "123 Main St",
  "city": "Springfield",
  "state": "IL",
  "zipCode": "62701",
  "country": "USA"
}
```

**Response:**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phone": "+19876543210",
  "address": "123 Main St",
  "city": "Springfield",
  "state": "IL",
  "zipCode": "62701",
  "country": "USA",
  "createdAt": "2024-06-04T10:30:00",
  "updatedAt": "2024-06-04T10:30:00"
}
```

### Get Orders Paginated

**Request:**
```bash
GET /api/orders?page=0&size=5&sort=createdAt,desc
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "orderNumber": "ORD-20240604103000-000001",
      "customerId": 1,
      "status": "PENDING",
      "totalAmount": 299.99,
      "items": [...]
    }
  ],
  "pageable": {
    "sort": {
      "empty": false,
      "sorted": true
    },
    "offset": 0,
    "pageSize": 5
  },
  "totalElements": 42,
  "totalPages": 9,
  "number": 0,
  "size": 5
}
```

## 🛠️ Key Dependencies

```xml
<!-- Spring Boot -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- OpenAPI/Swagger -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

## 📈 Performance Considerations

1. **Connection Pooling** - HikariCP configured in application.yml
2. **Pagination** - Always use pagination for list endpoints
3. **Lazy Loading** - `@ManyToOne(fetch = FetchType.LAZY)`
4. **Caching** - Can be added with Spring Cache
5. **Indexes** - Database indexes on frequently queried columns

## 🔐 Security Notes

Currently this module does NOT include:
- Authentication
- Authorization
- HTTPS/TLS
- Request rate limiting
- CORS configuration

These can be added using Spring Security (see Module 2 guide).

## 📝 Common Patterns

### Creating a New REST Endpoint

1. Create request/response DTOs
2. Add repository method if needed
3. Add service method with business logic
4. Add controller method with @RequestMapping
5. Add OpenAPI annotations for documentation
6. Add exception handling if needed
7. Write unit and integration tests

### Entity Relationships

```java
// One-to-Many
@Entity
public class OrderEntity {
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItemEntity> items;
}

@Entity
public class OrderItemEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;
}
```

## 🚨 Troubleshooting

### H2 Console
```
http://localhost:8080/api/h2-console/
```

### Enable SQL Logging
```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### View Application Logs
```bash
mvn spring-boot:run -DskipTests -X
```

## 🎓 Learning Outcomes

After this module, you should understand:
- How to build REST APIs with Spring MVC
- Controller and Service layer organization
- Request/response handling and transformation
- Validation and error handling
- Database access with Spring Data JPA
- Writing unit and integration tests
- API documentation with OpenAPI
- Pagination, sorting, and filtering

## 📚 Further Reading

- [Spring REST documentation](https://spring.io/guides/gs/rest-service/)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Springdoc OpenAPI](https://springdoc.org/)
- [Jakarta Validation](https://jakarta.ee/specifications/bean-validation/)

---

**Next Module**: [02-rest-reactive-webclient](../02-rest-reactive-webclient) - Adding async WebClient for non-blocking I/O

