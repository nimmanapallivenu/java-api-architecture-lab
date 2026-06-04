# Module 04: GraphQL API with Spring Boot

## Overview

This module demonstrates a **production-ready GraphQL API** implementation using Spring for GraphQL. It showcases schema-first design, efficient data fetching with DataLoaders, field-level authorization, subscriptions, and best practices for building flexible, UI-driven APIs.

## 🎯 Learning Objectives

- **Schema-First GraphQL Design**: Define types, queries, mutations, and subscriptions
- **Resolvers & DataFetchers**: Implement efficient data resolution
- **N+1 Problem Solution**: Use DataLoader for batch loading
- **GraphQL Subscriptions**: Real-time updates with WebSocket
- **Field-Level Security**: Implement fine-grained authorization
- **Error Handling**: Custom error responses and validation
- **Testing**: Unit and integration tests for GraphQL APIs
- **Performance**: Query complexity analysis and depth limiting

## 📋 Technology Stack

- **Spring Boot 3.2.x**
- **Spring for GraphQL 1.2.x**
- **GraphQL Java 21.x**
- **DataLoader (java-dataloader)**
- **Spring Data JPA**
- **H2 Database** (in-memory)
- **Lombok**
- **GraphQL Playground** (UI for testing)

## 🏗️ Architecture

```
GraphQL Client (Playground/Frontend)
         ↓
   GraphQL Controller
         ↓
   Schema Definition (.graphqls)
         ↓
   Resolvers (Query/Mutation/Subscription)
         ↓
   DataLoaders (Batch Loading)
         ↓
   Service Layer
         ↓
   Repository Layer
         ↓
   Database
```

## 📁 Project Structure

```
04-graphql-api/
├── src/main/
│   ├── java/com/apiarchlab/graphql/
│   │   ├── GraphQLApplication.java
│   │   ├── config/
│   │   │   ├── GraphQLConfig.java
│   │   │   ├── DataLoaderConfig.java
│   │   │   └── SecurityConfig.java
│   │   ├── resolver/
│   │   │   ├── QueryResolver.java
│   │   │   ├── MutationResolver.java
│   │   │   ├── SubscriptionResolver.java
│   │   │   ├── CustomerResolver.java
│   │   │   └── OrderResolver.java
│   │   ├── dataloader/
│   │   │   ├── CustomerDataLoader.java
│   │   │   ├── OrderDataLoader.java
│   │   │   └── PaymentDataLoader.java
│   │   ├── service/
│   │   │   ├── CustomerService.java
│   │   │   ├── OrderService.java
│   │   │   └── PaymentService.java
│   │   ├── repository/
│   │   │   ├── CustomerRepository.java
│   │   │   ├── OrderRepository.java
│   │   │   ├── OrderItemRepository.java
│   │   │   └── PaymentRepository.java
│   │   ├── entity/
│   │   │   ├── CustomerEntity.java
│   │   │   ├── OrderEntity.java
│   │   │   ├── OrderItemEntity.java
│   │   │   └── PaymentEntity.java
│   │   ├── dto/
│   │   │   ├── CreateCustomerInput.java
│   │   │   ├── CreateOrderInput.java
│   │   │   ├── UpdateOrderStatusInput.java
│   │   │   └── OrderFilter.java
│   │   ├── exception/
│   │   │   ├── GraphQLExceptionHandler.java
│   │   │   └── ResourceNotFoundException.java
│   │   └── scalar/
│   │       └── DateTimeScalar.java
│   └── resources/
│       ├── application.yml
│       ├── graphql/
│       │   └── schema.graphqls
│       └── data.sql
└── pom.xml
```

## 🔧 GraphQL Schema

### Types

```graphql
type Customer {
  id: ID!
  name: String!
  email: String!
  phone: String!
  address: String
  orders: [Order!]!
  createdAt: DateTime!
  updatedAt: DateTime!
}

type Order {
  id: ID!
  orderNumber: String!
  customer: Customer!
  items: [OrderItem!]!
  totalAmount: Float!
  status: OrderStatus!
  payment: Payment
  createdAt: DateTime!
  updatedAt: DateTime!
}

type OrderItem {
  id: ID!
  productName: String!
  quantity: Int!
  unitPrice: Float!
  totalPrice: Float!
}

type Payment {
  id: ID!
  amount: Float!
  paymentMethod: String!
  status: PaymentStatus!
  transactionId: String
  paidAt: DateTime
  createdAt: DateTime!
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
  COMPLETED
  FAILED
  REFUNDED
}

scalar DateTime
```

### Queries

```graphql
type Query {
  # Customer queries
  customer(id: ID!): Customer
  customers(page: Int, size: Int): [Customer!]!
  searchCustomers(name: String!): [Customer!]!
  
  # Order queries
  order(id: ID!): Order
  orders(filter: OrderFilter, page: Int, size: Int): [Order!]!
  ordersByCustomer(customerId: ID!): [Order!]!
  ordersByStatus(status: OrderStatus!): [Order!]!
}

input OrderFilter {
  status: OrderStatus
  minAmount: Float
  maxAmount: Float
  startDate: DateTime
  endDate: DateTime
}
```

### Mutations

```graphql
type Mutation {
  # Customer mutations
  createCustomer(input: CreateCustomerInput!): Customer!
  updateCustomer(id: ID!, input: UpdateCustomerInput!): Customer!
  deleteCustomer(id: ID!): Boolean!
  
  # Order mutations
  createOrder(input: CreateOrderInput!): Order!
  updateOrderStatus(id: ID!, status: OrderStatus!): Order!
  cancelOrder(id: ID!): Order!
  
  # Payment mutations
  processPayment(orderId: ID!, input: PaymentInput!): Payment!
}

input CreateCustomerInput {
  name: String!
  email: String!
  phone: String!
  address: String
}

input CreateOrderInput {
  customerId: ID!
  items: [OrderItemInput!]!
}

input OrderItemInput {
  productName: String!
  quantity: Int!
  unitPrice: Float!
}

input PaymentInput {
  amount: Float!
  paymentMethod: String!
}
```

### Subscriptions

```graphql
type Subscription {
  orderStatusChanged(customerId: ID): Order!
  paymentProcessed(orderId: ID): Payment!
}
```

## 🚀 Key Features

### 1. DataLoader for N+1 Problem

```java
@Component
public class CustomerDataLoader implements BatchLoader<Long, Customer> {
    
    @Autowired
    private CustomerService customerService;
    
    @Override
    public CompletionStage<List<Customer>> load(List<Long> keys) {
        // Batch load customers in a single query
        return CompletableFuture.supplyAsync(() -> 
            customerService.findByIds(keys)
        );
    }
}
```

### 2. Field-Level Resolver

```java
@Controller
public class OrderResolver {
    
    @SchemaMapping(typeName = "Order", field = "customer")
    public CompletableFuture<Customer> customer(
            Order order, 
            DataLoader<Long, Customer> customerDataLoader) {
        return customerDataLoader.load(order.getCustomerId());
    }
    
    @SchemaMapping(typeName = "Order", field = "payment")
    public CompletableFuture<Payment> payment(
            Order order,
            DataLoader<Long, Payment> paymentDataLoader) {
        return paymentDataLoader.load(order.getId());
    }
}
```

### 3. Subscription Implementation

```java
@Controller
public class SubscriptionResolver {
    
    private final Sinks.Many<Order> orderSink = Sinks.many()
        .multicast()
        .onBackpressureBuffer();
    
    @SubscriptionMapping
    public Flux<Order> orderStatusChanged(@Argument Long customerId) {
        return orderSink.asFlux()
            .filter(order -> customerId == null || 
                   order.getCustomerId().equals(customerId));
    }
    
    public void publishOrderUpdate(Order order) {
        orderSink.tryEmitNext(order);
    }
}
```

### 4. Custom Error Handling

```java
@ControllerAdvice
public class GraphQLExceptionHandler {
    
    @GraphQlExceptionHandler
    public GraphQLError handle(ResourceNotFoundException ex) {
        return GraphQLError.newError()
            .errorType(ErrorType.NOT_FOUND)
            .message(ex.getMessage())
            .build();
    }
    
    @GraphQlExceptionHandler
    public GraphQLError handle(ValidationException ex) {
        return GraphQLError.newError()
            .errorType(ErrorType.BAD_REQUEST)
            .message(ex.getMessage())
            .extensions(Map.of("validationErrors", ex.getErrors()))
            .build();
    }
}
```

## 📊 Sample Queries

### Query Customer with Orders

```graphql
query GetCustomerWithOrders {
  customer(id: "1") {
    id
    name
    email
    orders {
      id
      orderNumber
      totalAmount
      status
      items {
        productName
        quantity
        unitPrice
      }
      payment {
        status
        transactionId
      }
    }
  }
}
```

### Create Order Mutation

```graphql
mutation CreateOrder {
  createOrder(input: {
    customerId: "1"
    items: [
      {
        productName: "Laptop"
        quantity: 1
        unitPrice: 1200.00
      },
      {
        productName: "Mouse"
        quantity: 2
        unitPrice: 25.00
      }
    ]
  }) {
    id
    orderNumber
    totalAmount
    status
    customer {
      name
      email
    }
  }
}
```

### Subscribe to Order Updates

```graphql
subscription OrderUpdates {
  orderStatusChanged(customerId: "1") {
    id
    orderNumber
    status
    customer {
      name
    }
  }
}
```

## 🧪 Testing

### GraphQL Test Example

```java
@SpringBootTest
@AutoConfigureGraphQlTester
class OrderResolverTest {
    
    @Autowired
    private GraphQlTester graphQlTester;
    
    @Test
    void shouldCreateOrder() {
        String mutation = """
            mutation {
              createOrder(input: {
                customerId: "1"
                items: [{
                  productName: "Test Product"
                  quantity: 1
                  unitPrice: 100.00
                }]
              }) {
                id
                orderNumber
                totalAmount
              }
            }
            """;
        
        graphQlTester.document(mutation)
            .execute()
            .path("createOrder.totalAmount")
            .entity(Double.class)
            .isEqualTo(100.00);
    }
}
```

## 🔒 Security

### Field-Level Authorization

```java
@PreAuthorize("hasRole('ADMIN')")
@SchemaMapping
public List<Customer> customers() {
    return customerService.findAll();
}

@PreAuthorize("@securityService.canAccessCustomer(#id)")
@QueryMapping
public Customer customer(@Argument Long id) {
    return customerService.findById(id);
}
```

## 📈 Performance Optimization

### Query Complexity Analysis

```java
@Configuration
public class GraphQLConfig {
    
    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
            .directiveWiring(new MaxQueryDepthInstrumentation(10))
            .directiveWiring(new MaxQueryComplexityInstrumentation(100));
    }
}
```

## 🎯 GraphQL vs REST Comparison

| Feature | GraphQL | REST |
|---------|---------|------|
| **Data Fetching** | Single request, exact data | Multiple requests, over/under-fetching |
| **Versioning** | No versioning needed | API versioning required |
| **Documentation** | Self-documenting schema | Separate documentation |
| **Caching** | Complex (field-level) | Simple (HTTP caching) |
| **Learning Curve** | Steeper | Gentler |
| **Best For** | UI-driven, flexible queries | Simple CRUD, public APIs |

## 🚀 Running the Application

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run

# Access GraphQL Playground
http://localhost:8084/graphiql

# GraphQL endpoint
http://localhost:8084/graphql
```

## 📚 Key Takeaways

1. **Schema-First Design**: Define your API contract upfront
2. **Solve N+1 Problem**: Always use DataLoaders for related data
3. **Flexible Queries**: Clients request exactly what they need
4. **Real-Time Updates**: Subscriptions for live data
5. **Strong Typing**: Schema provides type safety
6. **Field-Level Security**: Fine-grained access control
7. **Performance**: Monitor query complexity and depth
8. **Testing**: Use GraphQlTester for integration tests

## 🔗 Related Modules

- **Module 01**: REST API (comparison baseline)
- **Module 03**: WebFlux (reactive alternative)
- **Module 07**: WebSocket (real-time comparison)

## 📖 Additional Resources

- [Spring for GraphQL Documentation](https://spring.io/projects/spring-graphql)
- [GraphQL Java](https://www.graphql-java.com/)
- [DataLoader Pattern](https://github.com/graphql/dataloader)
- [GraphQL Best Practices](https://graphql.org/learn/best-practices/)

---

**Next Module**: [05-soap-api](../05-soap-api) - Legacy XML-based web services