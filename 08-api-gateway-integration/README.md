# Module 08: API Gateway Integration

## Overview

This module demonstrates a **production-ready API Gateway** implementation using Spring Cloud Gateway. It serves as the single entry point for all microservices, providing routing, load balancing, security, rate limiting, circuit breaking, and API composition capabilities.

## 🎯 Learning Objectives

- **API Gateway Pattern**: Single entry point for microservices
- **Dynamic Routing**: Route requests to appropriate services
- **Load Balancing**: Distribute traffic across service instances
- **Circuit Breaker**: Resilience patterns with Resilience4j
- **Rate Limiting**: Request throttling and quota management
- **Security**: Centralized authentication and authorization
- **API Composition**: Aggregate responses from multiple services
- **Observability**: Distributed tracing and monitoring
- **CORS**: Cross-origin resource sharing configuration
- **Request/Response Transformation**: Modify headers, body, status

## 📋 Technology Stack

- **Spring Cloud Gateway 4.x**
- **Spring Boot 3.2.x**
- **Resilience4j** (Circuit Breaker, Rate Limiter, Retry)
- **Spring Security** (JWT, OAuth2)
- **Spring Cloud LoadBalancer**
- **Micrometer** (Metrics)
- **Zipkin** (Distributed Tracing)
- **Redis** (Rate Limiting Store)
- **Eureka** (Service Discovery - Optional)

## 🏗️ Architecture

```
                    Internet/Clients
                           ↓
                    Load Balancer
                           ↓
                  ┌─────────────────┐
                  │   API Gateway   │
                  │   (Port 8080)   │
                  └─────────────────┘
                           ↓
        ┌──────────────────┼──────────────────┐
        ↓                  ↓                  ↓
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  REST API    │  │  WebFlux API │  │ GraphQL API  │
│  (Port 8081) │  │  (Port 8083) │  │ (Port 8084)  │
└──────────────┘  └──────────────┘  └──────────────┘
        ↓                  ↓                  ↓
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ REST+WebClient│ │   SOAP API   │  │   gRPC API   │
│  (Port 8082) │  │  (Port 8085) │  │ (Port 9090)  │
└──────────────┘  └──────────────┘  └──────────────┘
        ↓
┌──────────────┐
│ WebSocket API│
│  (Port 8087) │
└──────────────┘
```

## 📁 Project Structure

```
08-api-gateway-integration/
├── src/main/
│   ├── java/com/apiarchlab/gateway/
│   │   ├── GatewayApplication.java
│   │   ├── config/
│   │   │   ├── GatewayConfig.java
│   │   │   ├── SecurityConfig.java
│   │   │   ├── CorsConfig.java
│   │   │   ├── CircuitBreakerConfig.java
│   │   │   └── RateLimiterConfig.java
│   │   ├── filter/
│   │   │   ├── AuthenticationFilter.java
│   │   │   ├── LoggingFilter.java
│   │   │   ├── RequestIdFilter.java
│   │   │   └── ResponseTimeFilter.java
│   │   ├── handler/
│   │   │   ├── FallbackHandler.java
│   │   │   └── ErrorHandler.java
│   │   ├── service/
│   │   │   ├── AggregationService.java
│   │   │   └── JwtService.java
│   │   ├── controller/
│   │   │   ├── AggregationController.java
│   │   │   └── HealthController.java
│   │   └── model/
│   │       ├── AggregatedOrderResponse.java
│   │       └── GatewayError.java
│   ├── resources/
│   │   ├── application.yml
│   │   └── application-prod.yml
│   └── test/
│       └── java/com/apiarchlab/gateway/
│           └── GatewayIntegrationTest.java
└── pom.xml
```

## 🔧 Gateway Configuration

### Basic Route Configuration

```yaml
# application.yml
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        # REST API Routes
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
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
                redis-rate-limiter.requestedTokens: 1
        
        # REST + WebClient API Routes
        - id: rest-client-api
          uri: http://localhost:8082
          predicates:
            - Path=/api/rest-client/**
          filters:
            - StripPrefix=2
            - name: Retry
              args:
                retries: 3
                statuses: BAD_GATEWAY,SERVICE_UNAVAILABLE
                methods: GET,POST
                backoff:
                  firstBackoff: 50ms
                  maxBackoff: 500ms
                  factor: 2
        
        # WebFlux API Routes
        - id: webflux-api
          uri: http://localhost:8083
          predicates:
            - Path=/api/webflux/**
          filters:
            - StripPrefix=2
            - name: CircuitBreaker
              args:
                name: webfluxCircuitBreaker
                fallbackUri: forward:/fallback/webflux
        
        # GraphQL API Routes
        - id: graphql-api
          uri: http://localhost:8084
          predicates:
            - Path=/graphql/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 5
                redis-rate-limiter.burstCapacity: 10
        
        # SOAP API Routes
        - id: soap-api
          uri: http://localhost:8085
          predicates:
            - Path=/ws/**
          filters:
            - name: CircuitBreaker
              args:
                name: soapCircuitBreaker
        
        # gRPC API Routes (HTTP/2)
        - id: grpc-api
          uri: http://localhost:9090
          predicates:
            - Path=/grpc/**
          filters:
            - StripPrefix=1
        
        # WebSocket API Routes
        - id: websocket-api
          uri: ws://localhost:8087
          predicates:
            - Path=/ws/**
          filters:
            - name: PreserveHostHeader
      
      # Global CORS Configuration
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            exposedHeaders:
              - Authorization
              - X-Request-Id
            maxAge: 3600
      
      # Default Filters
      default-filters:
        - name: AddRequestHeader
          args:
            name: X-Gateway-Request
            value: API-Gateway
        - name: AddResponseHeader
          args:
            name: X-Gateway-Response
            value: API-Gateway
        - DedupeResponseHeader=Access-Control-Allow-Credentials Access-Control-Allow-Origin

server:
  port: 8080

# Resilience4j Configuration
resilience4j:
  circuitbreaker:
    configs:
      default:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 5s
        failureRateThreshold: 50
        eventConsumerBufferSize: 10
    instances:
      restApiCircuitBreaker:
        baseConfig: default
      webfluxCircuitBreaker:
        baseConfig: default
      soapCircuitBreaker:
        baseConfig: default
  
  timelimiter:
    configs:
      default:
        timeoutDuration: 3s
  
  ratelimiter:
    configs:
      default:
        limitForPeriod: 10
        limitRefreshPeriod: 1s
        timeoutDuration: 0s

# Redis Configuration (for Rate Limiting)
spring:
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms

# Actuator Configuration
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,gateway
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true

# Logging
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    reactor.netty: INFO
```

### Java-based Route Configuration

```java
@Configuration
public class GatewayConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // REST API with Circuit Breaker and Rate Limiting
            .route("rest-api", r -> r
                .path("/api/rest/**")
                .filters(f -> f
                    .stripPrefix(2)
                    .circuitBreaker(c -> c
                        .setName("restApiCircuitBreaker")
                        .setFallbackUri("forward:/fallback/rest"))
                    .requestRateLimiter(rl -> rl
                        .setRateLimiter(redisRateLimiter()))
                    .retry(retry -> retry
                        .setRetries(3)
                        .setStatuses(HttpStatus.BAD_GATEWAY, HttpStatus.SERVICE_UNAVAILABLE))
                    .addRequestHeader("X-Gateway-Request", "API-Gateway")
                    .addResponseHeader("X-Gateway-Response", "API-Gateway"))
                .uri("http://localhost:8081"))
            
            // WebFlux API with Load Balancing
            .route("webflux-api", r -> r
                .path("/api/webflux/**")
                .filters(f -> f
                    .stripPrefix(2)
                    .circuitBreaker(c -> c
                        .setName("webfluxCircuitBreaker")
                        .setFallbackUri("forward:/fallback/webflux")))
                .uri("lb://webflux-service")) // Load balanced
            
            // GraphQL API with Custom Filter
            .route("graphql-api", r -> r
                .path("/graphql/**")
                .filters(f -> f
                    .filter(new AuthenticationFilter())
                    .requestRateLimiter(rl -> rl
                        .setRateLimiter(redisRateLimiter())))
                .uri("http://localhost:8084"))
            
            // WebSocket API
            .route("websocket-api", r -> r
                .path("/ws/**")
                .filters(f -> f
                    .preserveHostHeader())
                .uri("ws://localhost:8087"))
            
            // Aggregation Endpoint
            .route("aggregation", r -> r
                .path("/api/aggregate/**")
                .filters(f -> f
                    .stripPrefix(2))
                .uri("forward:/aggregate"))
            
            .build();
    }
    
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20, 1);
    }
    
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.just(
            exchange.getRequest()
                .getHeaders()
                .getFirst("X-User-Id") != null 
                    ? exchange.getRequest().getHeaders().getFirst("X-User-Id")
                    : exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
        );
    }
}
```

## 🔒 Security Configuration

### JWT Authentication Filter

```java
@Component
@Slf4j
public class AuthenticationFilter implements GatewayFilter {
    
    @Autowired
    private JwtService jwtService;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Skip authentication for public endpoints
        if (isPublicEndpoint(request.getPath().value())) {
            return chain.filter(exchange);
        }
        
        // Extract JWT token
        String token = extractToken(request);
        
        if (token == null) {
            return onError(exchange, "Missing authorization token", HttpStatus.UNAUTHORIZED);
        }
        
        try {
            // Validate token
            if (!jwtService.validateToken(token)) {
                return onError(exchange, "Invalid authorization token", HttpStatus.UNAUTHORIZED);
            }
            
            // Extract user info and add to headers
            String userId = jwtService.getUserIdFromToken(token);
            String username = jwtService.getUsernameFromToken(token);
            List<String> roles = jwtService.getRolesFromToken(token);
            
            // Add user context to downstream services
            ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", userId)
                .header("X-Username", username)
                .header("X-User-Roles", String.join(",", roles))
                .build();
            
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
            
        } catch (Exception e) {
            log.error("Authentication error", e);
            return onError(exchange, "Authentication failed", HttpStatus.UNAUTHORIZED);
        }
    }
    
    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/auth/") || 
               path.startsWith("/actuator/") ||
               path.equals("/health");
    }
    
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        GatewayError error = new GatewayError(status.value(), message, LocalDateTime.now());
        DataBuffer buffer = response.bufferFactory().wrap(
            new ObjectMapper().writeValueAsBytes(error)
        );
        
        return response.writeWith(Mono.just(buffer));
    }
}
```

### Security Configuration

```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf().disable()
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/actuator/**", "/health").permitAll()
                .pathMatchers("/api/auth/**").permitAll()
                .pathMatchers("/api/admin/**").hasRole("ADMIN")
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtDecoder(jwtDecoder()))
            )
            .build();
    }
    
    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        return ReactiveJwtDecoders.fromIssuerLocation("https://auth-server.com");
    }
}
```

## 🎭 Custom Filters

### Logging Filter

```java
@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        log.info("Request: {} {} from {}", 
            request.getMethod(), 
            request.getURI(), 
            request.getRemoteAddress());
        
        long startTime = System.currentTimeMillis();
        
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("Response: {} {} - Status: {} - Duration: {}ms",
                request.getMethod(),
                request.getURI(),
                response.getStatusCode(),
                duration);
        }));
    }
    
    @Override
    public int getOrder() {
        return -1; // Execute first
    }
}
```

### Request ID Filter

```java
@Component
public class RequestIdFilter implements GlobalFilter, Ordered {
    
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        String requestId = request.getHeaders().getFirst(REQUEST_ID_HEADER);
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }
        
        ServerHttpRequest modifiedRequest = request.mutate()
            .header(REQUEST_ID_HEADER, requestId)
            .build();
        
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add(REQUEST_ID_HEADER, requestId);
        
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }
    
    @Override
    public int getOrder() {
        return 0;
    }
}
```

### Response Time Filter

```java
@Component
@Slf4j
public class ResponseTimeFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        exchange.getAttributes().put("startTime", System.currentTimeMillis());
        
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            Long startTime = exchange.getAttribute("startTime");
            if (startTime != null) {
                long duration = System.currentTimeMillis() - startTime;
                exchange.getResponse().getHeaders().add("X-Response-Time", duration + "ms");
                
                log.debug("Request to {} took {}ms", 
                    exchange.getRequest().getURI(), 
                    duration);
            }
        }));
    }
    
    @Override
    public int getOrder() {
        return 1;
    }
}
```

## 🔄 API Composition/Aggregation

### Aggregation Service

```java
@Service
@Slf4j
public class AggregationService {
    
    @Autowired
    private WebClient.Builder webClientBuilder;
    
    public Mono<AggregatedOrderResponse> getAggregatedOrder(Long orderId) {
        // Parallel calls to multiple services
        Mono<OrderDto> orderMono = getOrder(orderId);
        Mono<CustomerDto> customerMono = orderMono.flatMap(order -> 
            getCustomer(order.getCustomerId()));
        Mono<PaymentDto> paymentMono = getPayment(orderId);
        Mono<List<NotificationDto>> notificationsMono = getNotifications(orderId);
        
        return Mono.zip(orderMono, customerMono, paymentMono, notificationsMono)
            .map(tuple -> AggregatedOrderResponse.builder()
                .order(tuple.getT1())
                .customer(tuple.getT2())
                .payment(tuple.getT3())
                .notifications(tuple.getT4())
                .build())
            .timeout(Duration.ofSeconds(5))
            .onErrorResume(e -> {
                log.error("Error aggregating order data", e);
                return Mono.error(new RuntimeException("Failed to aggregate order data"));
            });
    }
    
    private Mono<OrderDto> getOrder(Long orderId) {
        return webClientBuilder.build()
            .get()
            .uri("http://localhost:8081/api/orders/{id}", orderId)
            .retrieve()
            .bodyToMono(OrderDto.class)
            .timeout(Duration.ofSeconds(2))
            .onErrorResume(e -> Mono.empty());
    }
    
    private Mono<CustomerDto> getCustomer(Long customerId) {
        return webClientBuilder.build()
            .get()
            .uri("http://localhost:8081/api/customers/{id}", customerId)
            .retrieve()
            .bodyToMono(CustomerDto.class)
            .timeout(Duration.ofSeconds(2))
            .onErrorResume(e -> Mono.empty());
    }
    
    private Mono<PaymentDto> getPayment(Long orderId) {
        return webClientBuilder.build()
            .get()
            .uri("http://localhost:8082/api/payments/order/{orderId}", orderId)
            .retrieve()
            .bodyToMono(PaymentDto.class)
            .timeout(Duration.ofSeconds(2))
            .onErrorResume(e -> Mono.empty());
    }
    
    private Mono<List<NotificationDto>> getNotifications(Long orderId) {
        return webClientBuilder.build()
            .get()
            .uri("http://localhost:8082/api/notifications/order/{orderId}", orderId)
            .retrieve()
            .bodyToFlux(NotificationDto.class)
            .collectList()
            .timeout(Duration.ofSeconds(2))
            .onErrorResume(e -> Mono.just(Collections.emptyList()));
    }
}
```

### Aggregation Controller

```java
@RestController
@RequestMapping("/aggregate")
@Slf4j
public class AggregationController {
    
    @Autowired
    private AggregationService aggregationService;
    
    @GetMapping("/orders/{orderId}")
    public Mono<ResponseEntity<AggregatedOrderResponse>> getAggregatedOrder(
            @PathVariable Long orderId) {
        
        log.info("Aggregating order data for orderId: {}", orderId);
        
        return aggregationService.getAggregatedOrder(orderId)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
```

## 🛡️ Fallback Handlers

### Fallback Handler

```java
@RestController
@RequestMapping("/fallback")
public class FallbackHandler {
    
    @GetMapping("/rest")
    public Mono<ResponseEntity<GatewayError>> restFallback() {
        GatewayError error = new GatewayError(
            503,
            "REST API is currently unavailable. Please try again later.",
            LocalDateTime.now()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error));
    }
    
    @GetMapping("/webflux")
    public Mono<ResponseEntity<GatewayError>> webfluxFallback() {
        GatewayError error = new GatewayError(
            503,
            "WebFlux API is currently unavailable. Please try again later.",
            LocalDateTime.now()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error));
    }
    
    @GetMapping("/soap")
    public Mono<ResponseEntity<GatewayError>> soapFallback() {
        GatewayError error = new GatewayError(
            503,
            "SOAP API is currently unavailable. Please try again later.",
            LocalDateTime.now()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error));
    }
}
```

## 📊 Monitoring & Observability

### Distributed Tracing with Zipkin

```yaml
# application.yml
spring:
  zipkin:
    base-url: http://localhost:9411
    enabled: true
  sleuth:
    sampler:
      probability: 1.0 # Sample 100% of requests
```

### Metrics Configuration

```java
@Configuration
public class MetricsConfig {
    
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
            .commonTags("application", "api-gateway")
            .commonTags("environment", "production");
    }
    
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
```

## 🧪 Testing

### Gateway Integration Test

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class GatewayIntegrationTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @Test
    void shouldRouteToRestApi() {
        webTestClient.get()
            .uri("/api/rest/customers/1")
            .header("Authorization", "Bearer " + getValidToken())
            .exchange()
            .expectStatus().isOk()
            .expectHeader().exists("X-Gateway-Response")
            .expectHeader().exists("X-Request-Id");
    }
    
    @Test
    void shouldApplyRateLimiting() {
        // Make 21 requests (burst capacity is 20)
        for (int i = 0; i < 21; i++) {
            WebTestClient.ResponseSpec response = webTestClient.get()
                .uri("/api/rest/customers")
                .exchange();
            
            if (i < 20) {
                response.expectStatus().isOk();
            } else {
                response.expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
            }
        }
    }
    
    @Test
    void shouldFallbackOnServiceFailure() {
        // Simulate service failure
        webTestClient.get()
            .uri("/api/rest/customers/999999")
            .exchange()
            .expectStatus().is5xxServerError()
            .expectBody()
            .jsonPath("$.message").exists();
    }
    
    @Test
    void shouldAggregateMultipleServices() {
        webTestClient.get()
            .uri("/api/aggregate/orders/1")
            .header("Authorization", "Bearer " + getValidToken())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.order").exists()
            .jsonPath("$.customer").exists()
            .jsonPath("$.payment").exists()
            .jsonPath("$.notifications").exists();
    }
    
    private String getValidToken() {
        // Generate or return a valid JWT token for testing
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
    }
}
```

## 🚀 Deployment Architecture

### Docker Compose

```yaml
version: '3.8'

services:
  api-gateway:
    build: ./08-api-gateway-integration
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - REDIS_HOST=redis
      - ZIPKIN_BASE_URL=http://zipkin:9411
    depends_on:
      - redis
      - zipkin
      - rest-api
      - webflux-api
      - graphql-api
  
  rest-api:
    build: ./01-rest-api-springmvc
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
  
  rest-client-api:
    build: ./02-rest-reactive-webclient
    ports:
      - "8082:8082"
  
  webflux-api:
    build: ./03-webflux-api
    ports:
      - "8083:8083"
  
  graphql-api:
    build: ./04-graphql-api
    ports:
      - "8084:8084"
  
  soap-api:
    build: ./05-soap-api
    ports:
      - "8085:8085"
  
  grpc-api:
    build: ./06-grpc-api
    ports:
      - "9090:9090"
  
  websocket-api:
    build: ./07-websocket-api
    ports:
      - "8087:8087"
  
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
  
  zipkin:
    image: openzipkin/zipkin
    ports:
      - "9411:9411"
  
  prometheus:
    image: prom/prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
  
  grafana:
    image: grafana/grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
```

## 📈 Performance Optimization

### Connection Pooling

```yaml
spring:
  cloud:
    gateway:
      httpclient:
        pool:
          type: ELASTIC
          max-connections: 500
          max-idle-time: 30s
          max-life-time: 60s
        connect-timeout: 5000
        response-timeout: 10s
```

### Load Balancing

```java
@Configuration
public class LoadBalancerConfig {
    
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
    
    @Bean
    public ServiceInstanceListSupplier serviceInstanceListSupplier(
            ConfigurableApplicationContext context) {
        return ServiceInstanceListSupplier.builder()
            .withDiscoveryClient()
            .withHealthChecks()
            .build(context);
    }
}
```

## 📊 Gateway Patterns Comparison

| Pattern | Use Case | Complexity | Performance |
|---------|----------|------------|-------------|
| **Simple Routing** | Basic proxy | Low | High |
| **Circuit Breaker** | Fault tolerance | Medium | Medium |
| **Rate Limiting** | Throttling | Medium | Medium |
| **API Composition** | Aggregation | High | Low |
| **Request Transformation** | Protocol translation | Medium | Medium |
| **Authentication** | Security | High | Medium |

## 🎯 API Gateway Best Practices

1. **Single Entry Point**: All external traffic goes through gateway
2. **Service Discovery**: Use Eureka/Consul for dynamic routing
3. **Circuit Breaker**: Prevent cascading failures
4. **Rate Limiting**: Protect backend services
5. **Caching**: Cache responses at gateway level
6. **Monitoring**: Track all requests and responses
7. **Security**: Centralized authentication and authorization
8. **Versioning**: Support multiple API versions
9. **Documentation**: Auto-generate API docs
10. **Testing**: Comprehensive integration tests

## 🔗 Complete Request Flow

```
1. Client Request
   ↓
2. API Gateway (Port 8080)
   ↓
3. Authentication Filter (JWT validation)
   ↓
4. Rate Limiter (Check quota)
   ↓
5. Circuit Breaker (Check service health)
   ↓
6. Route to Backend Service
   ↓
7. Backend Service Processing
   ↓
8. Response Transformation
   ↓
9. Add Response Headers
   ↓
10. Return to Client
```

## 📚 Key Takeaways

1. **Centralized Entry Point**: Single gateway for all microservices
2. **Resilience**: Circuit breaker, retry, timeout patterns
3. **Security**: JWT authentication, role-based authorization
4. **Performance**: Rate limiting, caching, load balancing
5. **Observability**: Distributed tracing, metrics, logging
6. **Flexibility**: Dynamic routing, request/response transformation
7. **Scalability**: Horizontal scaling with load balancer
8. **API Composition**: Aggregate multiple service responses

## 🚀 Running the Complete System

```bash
# Start all services with Docker Compose
docker-compose up -d

# Access API Gateway
http://localhost:8080

# Access individual services (if needed)
http://localhost:8081  # REST API
http://localhost:8082  # REST + WebClient
http://localhost:8083  # WebFlux API
http://localhost:8084  # GraphQL API
http://localhost:8085  # SOAP API
http://localhost:9090  # gRPC API
http://localhost:8087  # WebSocket API

# Monitoring
http://localhost:9411  # Zipkin (Tracing)
http://localhost:9090  # Prometheus (Metrics)
http://localhost:3000  # Grafana (Dashboards)

# Gateway Actuator
http://localhost:8080/actuator/health
http://localhost:8080/actuator/gateway/routes
http://localhost:8080/actuator/metrics
```

## 📖 Additional Resources

- [Spring Cloud Gateway Documentation](https://spring.io/projects/spring-cloud-gateway)
- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [API Gateway Pattern](https://microservices.io/patterns/apigateway.html)
- [Circuit Breaker Pattern](https://martinfowler.com/bliki/CircuitBreaker.html)

---

**Congratulations!** You have completed all 8 modules of the Java API Architecture Lab. You now have comprehensive knowledge of REST, Reactive, GraphQL, SOAP, gRPC, WebSocket, and API Gateway patterns.