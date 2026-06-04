# Module 07: WebSocket API with STOMP Messaging

## Overview

This module demonstrates a **production-ready WebSocket API** implementation using Spring WebSocket and STOMP (Simple Text Oriented Messaging Protocol). It showcases real-time bidirectional communication, pub/sub messaging, user-specific messages, and best practices for building live, interactive applications.

## 🎯 Learning Objectives

- **WebSocket Protocol**: Full-duplex communication over TCP
- **STOMP**: Simple messaging protocol over WebSocket
- **Message Broker**: In-memory and external brokers (RabbitMQ, ActiveMQ)
- **Pub/Sub Pattern**: Topic-based and queue-based messaging
- **User-Specific Messages**: Send messages to specific users
- **Connection Lifecycle**: Connect, subscribe, send, disconnect
- **Security**: WebSocket authentication and authorization
- **Testing**: WebSocket integration tests
- **Scalability**: Horizontal scaling with external broker

## 📋 Technology Stack

- **Spring Boot 3.2.x**
- **Spring WebSocket 6.x**
- **STOMP Protocol**
- **SockJS** (WebSocket fallback)
- **RabbitMQ** (optional external broker)
- **Spring Security**
- **Spring Data JPA**
- **H2 Database**

## 🏗️ Architecture

```
Browser/Mobile Client
         ↓
   WebSocket Connection
         ↓
   STOMP over WebSocket
         ↓
   Message Broker (In-Memory/RabbitMQ)
         ↓
   @MessageMapping Controllers
         ↓
   Service Layer
         ↓
   Database
```

## 📁 Project Structure

```
07-websocket-api/
├── src/main/
│   ├── java/com/apiarchlab/websocket/
│   │   ├── WebSocketApplication.java
│   │   ├── config/
│   │   │   ├── WebSocketConfig.java
│   │   │   ├── WebSocketSecurityConfig.java
│   │   │   └── WebSocketEventListener.java
│   │   ├── controller/
│   │   │   ├── OrderWebSocketController.java
│   │   │   ├── ChatWebSocketController.java
│   │   │   └── NotificationWebSocketController.java
│   │   ├── service/
│   │   │   ├── OrderService.java
│   │   │   ├── NotificationService.java
│   │   │   └── ChatService.java
│   │   ├── model/
│   │   │   ├── OrderUpdate.java
│   │   │   ├── ChatMessage.java
│   │   │   ├── Notification.java
│   │   │   └── UserPresence.java
│   │   ├── repository/
│   │   │   ├── OrderRepository.java
│   │   │   └── ChatMessageRepository.java
│   │   ├── entity/
│   │   │   ├── OrderEntity.java
│   │   │   └── ChatMessageEntity.java
│   │   └── interceptor/
│   │       └── WebSocketAuthInterceptor.java
│   ├── resources/
│   │   ├── application.yml
│   │   └── static/
│   │       ├── index.html
│   │       ├── app.js
│   │       └── style.css
│   └── test/
│       └── java/com/apiarchlab/websocket/
│           └── WebSocketIntegrationTest.java
└── pom.xml
```

## 🔧 WebSocket Configuration

### Basic STOMP Configuration

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory broker
        config.enableSimpleBroker("/topic", "/queue");
        
        // Set application destination prefix
        config.setApplicationDestinationPrefixes("/app");
        
        // Set user destination prefix
        config.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register STOMP endpoint with SockJS fallback
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")
                .withSockJS();
        
        // Register native WebSocket endpoint
        registry.addEndpoint("/ws-native")
                .setAllowedOrigins("*");
    }
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new WebSocketAuthInterceptor());
    }
}
```

### External Broker Configuration (RabbitMQ)

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Value("${spring.rabbitmq.host}")
    private String rabbitHost;
    
    @Value("${spring.rabbitmq.port}")
    private int rabbitPort;
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable RabbitMQ broker
        config.enableStompBrokerRelay("/topic", "/queue")
              .setRelayHost(rabbitHost)
              .setRelayPort(rabbitPort)
              .setClientLogin("guest")
              .setClientPasscode("guest")
              .setSystemLogin("guest")
              .setSystemPasscode("guest");
        
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }
}
```

## 📡 WebSocket Controllers

### Order Updates Controller

```java
@Controller
public class OrderWebSocketController {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private OrderService orderService;
    
    // Receive message from client
    @MessageMapping("/order.create")
    @SendTo("/topic/orders")
    public OrderUpdate createOrder(CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return new OrderUpdate(order, "CREATED");
    }
    
    // Update order status
    @MessageMapping("/order.updateStatus")
    public void updateOrderStatus(UpdateOrderStatusRequest request) {
        Order order = orderService.updateStatus(
            request.getOrderId(), 
            request.getStatus()
        );
        
        // Send to specific user
        messagingTemplate.convertAndSendToUser(
            order.getCustomerId().toString(),
            "/queue/order-updates",
            new OrderUpdate(order, "STATUS_CHANGED")
        );
        
        // Broadcast to all subscribers
        messagingTemplate.convertAndSend(
            "/topic/orders",
            new OrderUpdate(order, "STATUS_CHANGED")
        );
    }
    
    // Scheduled updates
    @Scheduled(fixedRate = 5000)
    public void sendOrderUpdates() {
        List<Order> recentOrders = orderService.getRecentOrders();
        recentOrders.forEach(order -> {
            messagingTemplate.convertAndSend(
                "/topic/orders",
                new OrderUpdate(order, "PERIODIC_UPDATE")
            );
        });
    }
}
```

### Chat Controller

```java
@Controller
public class ChatWebSocketController {
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    // Public chat room
    @MessageMapping("/chat.send")
    @SendTo("/topic/chat")
    public ChatMessage sendMessage(ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        chatService.saveMessage(message);
        return message;
    }
    
    // Private message
    @MessageMapping("/chat.private")
    public void sendPrivateMessage(PrivateChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        chatService.saveMessage(message);
        
        // Send to recipient
        messagingTemplate.convertAndSendToUser(
            message.getRecipientId(),
            "/queue/messages",
            message
        );
    }
    
    // User joined
    @MessageMapping("/chat.join")
    @SendTo("/topic/chat")
    public UserPresence userJoined(UserPresence presence) {
        presence.setStatus("ONLINE");
        presence.setTimestamp(LocalDateTime.now());
        return presence;
    }
    
    // User left
    @MessageMapping("/chat.leave")
    @SendTo("/topic/chat")
    public UserPresence userLeft(UserPresence presence) {
        presence.setStatus("OFFLINE");
        presence.setTimestamp(LocalDateTime.now());
        return presence;
    }
}
```

### Notification Controller

```java
@Controller
public class NotificationWebSocketController {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    // Subscribe to user-specific notifications
    @SubscribeMapping("/user/queue/notifications")
    public List<Notification> getNotifications(Principal principal) {
        String userId = principal.getName();
        return notificationService.getUnreadNotifications(userId);
    }
    
    // Send notification to specific user
    public void sendNotification(String userId, Notification notification) {
        messagingTemplate.convertAndSendToUser(
            userId,
            "/queue/notifications",
            notification
        );
    }
    
    // Broadcast system notification
    public void broadcastSystemNotification(Notification notification) {
        messagingTemplate.convertAndSend(
            "/topic/system-notifications",
            notification
        );
    }
    
    // Mark notification as read
    @MessageMapping("/notification.markRead")
    public void markAsRead(MarkReadRequest request, Principal principal) {
        String userId = principal.getName();
        notificationService.markAsRead(userId, request.getNotificationId());
    }
}
```

## 🔒 WebSocket Security

### Security Configuration

```java
@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig {
    
    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager(
            MessageMatcherDelegatingAuthorizationManager.Builder messages) {
        
        messages
            // Allow connection
            .simpDestMatchers("/app/**").authenticated()
            .simpSubscribeDestMatchers("/user/queue/**").authenticated()
            .simpSubscribeDestMatchers("/topic/**").permitAll()
            
            // Require specific roles
            .simpDestMatchers("/app/admin/**").hasRole("ADMIN")
            
            // Allow all other messages
            .anyMessage().authenticated();
        
        return messages.build();
    }
}
```

### Authentication Interceptor

```java
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = 
            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                
                if (tokenProvider.validateToken(token)) {
                    String username = tokenProvider.getUsernameFromToken(token);
                    UsernamePasswordAuthenticationToken auth = 
                        new UsernamePasswordAuthenticationToken(username, null, null);
                    accessor.setUser(auth);
                }
            }
        }
        
        return message;
    }
}
```

## 🎭 Event Listeners

### Connection Event Listener

```java
@Component
@Slf4j
public class WebSocketEventListener {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = headerAccessor.getUser().getName();
        
        log.info("WebSocket connected: user={}, session={}", username, sessionId);
        
        // Broadcast user online status
        UserPresence presence = new UserPresence(username, "ONLINE");
        messagingTemplate.convertAndSend("/topic/user-presence", presence);
    }
    
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = headerAccessor.getUser().getName();
        
        log.info("WebSocket disconnected: user={}, session={}", username, sessionId);
        
        // Broadcast user offline status
        UserPresence presence = new UserPresence(username, "OFFLINE");
        messagingTemplate.convertAndSend("/topic/user-presence", presence);
    }
    
    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        String username = headerAccessor.getUser().getName();
        
        log.info("User subscribed: user={}, destination={}", username, destination);
    }
}
```

## 💻 JavaScript Client

### SockJS + STOMP Client

```javascript
// app.js
let stompClient = null;

function connect() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    
    // Set headers (e.g., JWT token)
    const headers = {
        'Authorization': 'Bearer ' + getJwtToken()
    };
    
    stompClient.connect(headers, function(frame) {
        console.log('Connected: ' + frame);
        
        // Subscribe to public topic
        stompClient.subscribe('/topic/orders', function(message) {
            showOrder(JSON.parse(message.body));
        });
        
        // Subscribe to user-specific queue
        stompClient.subscribe('/user/queue/notifications', function(message) {
            showNotification(JSON.parse(message.body));
        });
        
        // Subscribe to chat
        stompClient.subscribe('/topic/chat', function(message) {
            showChatMessage(JSON.parse(message.body));
        });
    }, function(error) {
        console.error('WebSocket error:', error);
        setTimeout(connect, 5000); // Reconnect after 5 seconds
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    console.log('Disconnected');
}

function sendOrder(order) {
    stompClient.send('/app/order.create', {}, JSON.stringify(order));
}

function sendChatMessage(message) {
    stompClient.send('/app/chat.send', {}, JSON.stringify({
        sender: getCurrentUser(),
        content: message,
        type: 'CHAT'
    }));
}

function sendPrivateMessage(recipientId, message) {
    stompClient.send('/app/chat.private', {}, JSON.stringify({
        senderId: getCurrentUser(),
        recipientId: recipientId,
        content: message
    }));
}

// Auto-connect on page load
window.addEventListener('load', function() {
    connect();
});

// Disconnect on page unload
window.addEventListener('beforeunload', function() {
    disconnect();
});
```

### Native WebSocket Client

```javascript
let ws = null;

function connectNative() {
    ws = new WebSocket('ws://localhost:8087/ws-native');
    
    ws.onopen = function() {
        console.log('WebSocket connected');
        
        // Send STOMP CONNECT frame
        const connectFrame = 
            'CONNECT\n' +
            'accept-version:1.2\n' +
            'heart-beat:10000,10000\n' +
            '\n' +
            '\x00';
        ws.send(connectFrame);
    };
    
    ws.onmessage = function(event) {
        console.log('Received:', event.data);
        handleStompFrame(event.data);
    };
    
    ws.onerror = function(error) {
        console.error('WebSocket error:', error);
    };
    
    ws.onclose = function() {
        console.log('WebSocket closed');
        setTimeout(connectNative, 5000);
    };
}

function sendNative(destination, body) {
    const frame = 
        'SEND\n' +
        'destination:' + destination + '\n' +
        'content-type:application/json\n' +
        '\n' +
        JSON.stringify(body) + '\x00';
    ws.send(frame);
}
```

## 📊 Message Models

### Order Update

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderUpdate {
    private Long orderId;
    private String orderNumber;
    private String status;
    private BigDecimal totalAmount;
    private String updateType; // CREATED, STATUS_CHANGED, CANCELLED
    private LocalDateTime timestamp;
}
```

### Chat Message

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private String id;
    private String sender;
    private String content;
    private MessageType type; // CHAT, JOIN, LEAVE
    private LocalDateTime timestamp;
}
```

### Notification

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    private String id;
    private String userId;
    private String title;
    private String message;
    private NotificationType type; // INFO, WARNING, ERROR, SUCCESS
    private boolean read;
    private LocalDateTime timestamp;
}
```

## 🧪 Testing

### WebSocket Integration Test

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    private WebSocketStompClient stompClient;
    private StompSession stompSession;
    
    @BeforeEach
    void setup() throws Exception {
        WebSocketClient webSocketClient = new StandardWebSocketClient();
        this.stompClient = new WebSocketStompClient(webSocketClient);
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        
        String url = "ws://localhost:" + port + "/ws";
        this.stompSession = stompClient.connect(url, new StompSessionHandlerAdapter() {})
            .get(1, TimeUnit.SECONDS);
    }
    
    @Test
    void shouldReceiveOrderUpdate() throws Exception {
        BlockingQueue<OrderUpdate> blockingQueue = new LinkedBlockingQueue<>();
        
        stompSession.subscribe("/topic/orders", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return OrderUpdate.class;
            }
            
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue.add((OrderUpdate) payload);
            }
        });
        
        // Send order creation request
        CreateOrderRequest request = new CreateOrderRequest(1L, List.of());
        stompSession.send("/app/order.create", request);
        
        // Wait for response
        OrderUpdate update = blockingQueue.poll(5, TimeUnit.SECONDS);
        
        assertNotNull(update);
        assertEquals("CREATED", update.getUpdateType());
    }
    
    @AfterEach
    void teardown() {
        if (stompSession != null) {
            stompSession.disconnect();
        }
    }
}
```

## 📈 Performance & Scalability

### Connection Limits

```yaml
# application.yml
spring:
  websocket:
    max-text-message-size: 64KB
    max-binary-message-size: 64KB
    max-session-idle-timeout: 30m
    
server:
  tomcat:
    max-connections: 10000
    threads:
      max: 200
```

### Horizontal Scaling with RabbitMQ

```
Load Balancer
    ↓
┌───────────────┬───────────────┐
│  Server 1     │  Server 2     │
│  (WebSocket)  │  (WebSocket)  │
└───────┬───────┴───────┬───────┘
        │               │
        └───────┬───────┘
                ↓
          RabbitMQ Broker
```

## 🔍 Monitoring

### WebSocket Metrics

```java
@Component
public class WebSocketMetrics {
    
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final AtomicLong messagesSent = new AtomicLong(0);
    private final AtomicLong messagesReceived = new AtomicLong(0);
    
    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        activeConnections.incrementAndGet();
    }
    
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        activeConnections.decrementAndGet();
    }
    
    public int getActiveConnections() {
        return activeConnections.get();
    }
    
    public long getMessagesSent() {
        return messagesSent.get();
    }
    
    public long getMessagesReceived() {
        return messagesReceived.get();
    }
}
```

## 📊 WebSocket vs Other Protocols

| Feature | WebSocket | SSE | Long Polling | REST |
|---------|-----------|-----|--------------|------|
| **Bidirectional** | Yes | No | No | No |
| **Real-time** | Excellent | Good | Fair | Poor |
| **Overhead** | Low | Medium | High | High |
| **Browser Support** | Modern | Modern | All | All |
| **Complexity** | Medium | Low | Low | Low |
| **Best For** | Chat, gaming, live updates | Server push only | Legacy support | Request/response |

## 🚀 Use Cases

1. **Live Order Tracking**: Real-time order status updates
2. **Chat Applications**: Instant messaging and group chat
3. **Notifications**: Push notifications to users
4. **Live Dashboard**: Real-time metrics and analytics
5. **Collaborative Editing**: Multiple users editing same document
6. **Gaming**: Multiplayer game state synchronization
7. **Stock Tickers**: Live stock price updates
8. **IoT**: Device status and telemetry

## 🚀 Running the Application

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run

# Access WebSocket endpoint
ws://localhost:8087/ws

# Access test page
http://localhost:8087/index.html
```

## 📚 Key Takeaways

1. **Full-Duplex**: Bidirectional communication over single connection
2. **Low Latency**: Real-time updates with minimal overhead
3. **STOMP**: Simple protocol for messaging over WebSocket
4. **Pub/Sub**: Topic and queue-based messaging patterns
5. **User-Specific**: Send messages to specific users
6. **Scalability**: Use external broker for horizontal scaling
7. **Security**: Authenticate and authorize WebSocket connections
8. **Fallback**: SockJS provides fallback for older browsers

## 🔗 Related Modules

- **Module 03**: WebFlux (reactive alternative)
- **Module 04**: GraphQL Subscriptions (alternative real-time)
- **Module 06**: gRPC Streaming (alternative streaming)

## 📖 Additional Resources

- [Spring WebSocket Documentation](https://spring.io/guides/gs/messaging-stomp-websocket/)
- [STOMP Protocol](https://stomp.github.io/)
- [SockJS](https://github.com/sockjs/sockjs-client)
- [WebSocket API](https://developer.mozilla.org/en-US/docs/Web/API/WebSocket)

---

**Next Module**: [08-api-gateway-integration](../08-api-gateway-integration) - API Gateway orchestration