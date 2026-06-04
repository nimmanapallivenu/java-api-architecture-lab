# Java API Architecture Lab - Navigation Guide

Welcome to the **Java API Architecture Lab**! This is your complete learning resource for mastering 8 different API architectural styles using a unified Order Management domain.

---

## 📍 Where to Start

### First Time?
**→ Start here: [README.md](README.md)**
- 5-minute overview of the entire project
- 5 guided learning paths (pick one!)
- Architecture diagrams and comparisons

### Quick Start?
**→ Then read: [QUICKSTART.md](QUICKSTART.md)**
- Immediate next steps
- Build commands
- Key package locations

### In-Depth Setup?
**→ Follow: [docs/SETUP_GUIDE.md](docs/SETUP_GUIDE.md)**
- Module-by-module instructions
- Patterns for each API style
- Troubleshooting guide

---

## 📚 Documentation Map

```
README.md (Start here!)
├── Overview of all 8 API styles
├── 5 Learning paths
└── Quick comparison table

QUICKSTART.md
├── What's been created
├── Build & run commands
└── Next action checklist

docs/SETUP_GUIDE.md (Implementation guide)
├── General Spring Boot setup
├── Module 01: REST (MVC) - Basic patterns
├── Module 02: REST + WebClient - Async patterns
├── Module 03: WebFlux - Reactive patterns
├── Module 04: GraphQL - Query-driven patterns
├── Module 05: SOAP - Legacy patterns
├── Module 06: gRPC - High-performance patterns
├── Module 07: WebSocket - Real-time patterns
├── Module 08: API Gateway - Composition patterns
└── Troubleshooting section

docs/API_COMPARISON.md (Deep analysis)
├── Side-by-side comparison tables
├── Request-response patterns for each style
├── Scaling & performance analysis
├── Concurrency models explained
├── Data fetching patterns
├── Error handling comparison
├── Security considerations
└── Decision tree: "When to use which?"

docs/IMPLEMENTATION_EXAMPLES.md (Code copy-paste)
├── REST (MVC) - Controllers, services, repos
├── REST + WebClient - Async patterns
├── WebFlux - Reactive patterns
├── GraphQL - Schema, resolvers, types
├── gRPC - Protocol buffers, services
├── WebSocket - Config, handlers, client
├── API Gateway - Routes, filters
└── Common patterns

PROJECT_SUMMARY.md (What was created)
├── Deliverables checklist
├── File statistics
├── Architecture diagram
└── Next steps

.gitignore
└── Pre-configured for Maven/Spring Boot projects
```

---

## 🎯 Learning Path Selector

### 👶 Path 1: REST Fundamentals (Beginner)
**Duration**: 2-3 weeks | **Difficulty**: Beginner

**Modules**: 01 → 02 → 08

**Topics**:
- Basic REST CRUD operations
- HTTP status codes and semantics
- Creating REST controllers and services
- Async WebClient for calling downstream APIs
- API Gateway routing

**Documentation**:
1. Read: [README.md](README.md) - REST section
2. Setup: [docs/SETUP_GUIDE.md](docs/SETUP_GUIDE.md) - Modules 01, 02, 08
3. Code: [docs/IMPLEMENTATION_EXAMPLES.md](docs/IMPLEMENTATION_EXAMPLES.md) - Sections 1, 2

**After This Path** → Continue to reactive systems or GraphQL

---

### 🔄 Path 2: Reactive Systems (Intermediate)
**Duration**: 2-3 weeks | **Difficulty**: Intermediate

**Modules**: 03 → 02 → 08 (reactive mode)

**Topics**:
- Reactive streams (Mono, Flux)
- Non-blocking I/O with WebFlux
- Reactor library and operators
- R2DBC for reactive database access
- Backpressure handling

**Documentation**:
1. Read: [README.md](README.md) - WebFlux section + Concurrency section
2. Comparison: [docs/API_COMPARISON.md](docs/API_COMPARISON.md) - Concurrency models
3. Setup: [docs/SETUP_GUIDE.md](docs/SETUP_GUIDE.md) - Module 03
4. Code: [docs/IMPLEMENTATION_EXAMPLES.md](docs/IMPLEMENTATION_EXAMPLES.md) - Section 3

**Prerequisite**: Module 01 (REST) understanding recommended

**After This Path** → Explore GraphQL or Enterprise patterns

---

### 🔍 Path 3: Query-Driven APIs (Intermediate)
**Duration**: 2 weeks | **Difficulty**: Intermediate

**Modules**: 04 ↔ 01 ↔ 08

**Topics**:
- GraphQL fundamentals
- Schema design and queries
- Mutations and subscriptions
- Field resolvers and data loaders
- Comparison with REST

**Documentation**:
1. Read: [README.md](README.md) - GraphQL section
2. Comparison: [docs/API_COMPARISON.md](docs/API_COMPARISON.md) - GraphQL section
3. Setup: [docs/SETUP_GUIDE.md](docs/SETUP_GUIDE.md) - Module 04
4. Code: [docs/IMPLEMENTATION_EXAMPLES.md](docs/IMPLEMENTATION_EXAMPLES.md) - Section 4

**Prerequisite**: Basic REST understanding

**After This Path** → Combine with real-time (WebSocket) for full capability

---

### 🏭 Path 4: Enterprise APIs (Advanced)
**Duration**: 3-4 weeks | **Difficulty**: Advanced

**Modules**: 05 → 06 → 07 → 08

**Topics**:
- SOAP/XML enterprise integration
- Contract-first design (WSDL, XSD)
- gRPC high-performance APIs
- Protocol Buffers (Protobuf)
- WebSocket real-time communication
- Multi-protocol API Gateway

**Documentation**:
1. Read: [README.md](README.md) - SOAP, gRPC, WebSocket sections
2. Deep Dive: [docs/API_COMPARISON.md](docs/API_COMPARISON.md) - Enterprise section
3. Setup: [docs/SETUP_GUIDE.md](docs/SETUP_GUIDE.md) - Modules 05, 06, 07, 08
4. Code: [docs/IMPLEMENTATION_EXAMPLES.md](docs/IMPLEMENTATION_EXAMPLES.md) - Sections 5, 6, 7

**Prerequisite**: REST and WebFlux understanding

**After This Path** → You're ready for production systems!

---

### ⚡ Path 5: Real-Time & Event-Driven (Advanced)
**Duration**: 2-3 weeks | **Difficulty**: Advanced

**Modules**: 07 → 03 → 04 → 08

**Topics**:
- Bidirectional WebSocket communication
- STOMP messaging protocol
- Server Sent Events (SSE)
- GraphQL subscriptions
- Event streaming
- Real-time notifications

**Documentation**:
1. Read: [README.md](README.md) - WebSocket section
2. Deep Dive: [docs/API_COMPARISON.md](docs/API_COMPARISON.md) - Real-time section
3. Setup: [docs/SETUP_GUIDE.md](docs/SETUP_GUIDE.md) - Module 07
4. Code: [docs/IMPLEMENTATION_EXAMPLES.md](docs/IMPLEMENTATION_EXAMPLES.md) - Section 7

**Prerequisite**: REST and basic WebFlux understanding

**After This Path** → Expert level API design!

---

## 🚀 Quick Commands

### Build Everything
```bash
cd /Users/nvenugopal/Documents/Workspace/java-api-architecture-lab
mvn clean install
```

### Run a Specific Module
```bash
# Module 1: REST (Port 8080)
mvn spring-boot:run -pl 01-rest-api-springmvc

# Module 3: WebFlux (Port 8080)
mvn spring-boot:run -pl 03-webflux-api

# Module 4: GraphQL (Port 8080, visit /graphiql)
mvn spring-boot:run -pl 04-graphql-api

# Module 6: gRPC (Port 50051)
mvn spring-boot:run -pl 06-grpc-api
```

### View Shared Domain Entities
```bash
ls -la 00-shared-domain/src/main/java/com/apiarchlab/domain/

# Entities:
# - Customer.java
# - Order.java
# - OrderItem.java
# - Payment.java
# - Notification.java

# Enums:
# - OrderStatus.java
# - PaymentStatus.java
# - NotificationStatus.java
# - NotificationType.java
```

---

## 📊 Module Ports Reference

| Module | Name | Port | Endpoint |
|--------|------|------|----------|
| 01 | REST API | 8080 | http://localhost:8080/api |
| 02 | REST + WebClient | 8080 | http://localhost:8080/api |
| 03 | WebFlux | 8080 | http://localhost:8080/api |
| 04 | GraphQL | 8080 | http://localhost:8080/graphql<br>UI: /graphiql |
| 05 | SOAP | 8080 | http://localhost:8080/ws |
| 06 | gRPC | 50051 | grpc://localhost:50051 |
| 07 | WebSocket | 8080 | ws://localhost:8080/ws |
| 08 | API Gateway | 8000 | http://localhost:8000 |

---

## 🔗 Navigation by Topic

### Looking for a specific topic?

**REST / HTTP Basics**
- → Start: [README.md](README.md)
- → Setup: [docs/SETUP_GUIDE.md](docs/SETUP_GUIDE.md) - Module 01
- → Code: [docs/IMPLEMENTATION_EXAMPLES.md](docs/IMPLEMENTATION_EXAMPLES.md) - Section 1

**Non-blocking & Async**
- → Setup: [docs/SETUP_GUIDE.md](docs/SETUP_GUIDE.md) - Modules 02, 03
- → Patterns: [docs/API_COMPARISON.md](docs/API_COMPARISON.md) - Concurrency Models
- → Code: [docs/IMPLEMENTATION_EXAMPLES.md](docs/IMPLEMENTATION_EXAMPLES.md) - Sections 2, 3

**GraphQL**
- → Overview: [README.md](README.md) - GraphQL table
- → Comparison: [docs/API_COMPARISON.md](docs/API_COMPARISON.md) - GraphQL section
- → Setup: [docs/SETUP_GUIDE.md](docs/SETUP_GUIDE.md) - Module 04
- → Code: [docs/IMPLEMENTATION_EXAMPLES.md](docs/IMPLEMENTATION_EXAMPLES.md) - Section 4

**Enterprise & Legacy**
- → Setup: [docs/SETUP_GUIDE.md](docs/SETUP_GUIDE.md) - Modules 05, 06
- → Comparison: [docs/API_COMPARISON.md](docs/API_COMPARISON.md) - Enterprise section
- → Code: [docs/IMPLEMENTATION_EXAMPLES.md](docs/IMPLEMENTATION_EXAMPLES.md) - Sections 5, 6

**Real-Time & Events**
- → Setup: [docs/SETUP_GUIDE.md](docs/SETUP_GUIDE.md) - Module 07
- → Code: [docs/IMPLEMENTATION_EXAMPLES.md](docs/IMPLEMENTATION_EXAMPLES.md) - Section 7

**API Composition**
- → Setup: [docs/SETUP_GUIDE.md](docs/SETUP_GUIDE.md) - Module 08
- → Patterns: [docs/API_COMPARISON.md](docs/API_COMPARISON.md) - Gateway section

**Performance Comparison**
- → [docs/API_COMPARISON.md](docs/API_COMPARISON.md) - Scaling & Performance section

**Decision Making**
- → [docs/API_COMPARISON.md](docs/API_COMPARISON.md) - Decision tree & When to use

**Troubleshooting**
- → [docs/SETUP_GUIDE.md](docs/SETUP_GUIDE.md) - Troubleshooting section

---

## ✅ Progress Checklist

Track your learning progress:

### Foundation
- [ ] Read README.md (5 min)
- [ ] Read QUICKSTART.md (5 min)
- [ ] Build project: `mvn clean install` (5 min)
- [ ] View shared domain entities (5 min)

### Module 01: REST
- [ ] Read SETUP_GUIDE.md - Module 01 section
- [ ] Read IMPLEMENTATION_EXAMPLES.md - Section 1
- [ ] Run: `mvn spring-boot:run -pl 01-rest-api-springmvc`
- [ ] Implement CustomerController
- [ ] Implement OrderController
- [ ] Write integration tests

### Module 03: WebFlux
- [ ] Read API_COMPARISON.md - Concurrency section
- [ ] Read SETUP_GUIDE.md - Module 03 section
- [ ] Read IMPLEMENTATION_EXAMPLES.md - Section 3
- [ ] Run WebFlux module
- [ ] Compare with REST (Module 01)

### Module 04: GraphQL
- [ ] Read API_COMPARISON.md - GraphQL section
- [ ] Read SETUP_GUIDE.md - Module 04 section
- [ ] Read IMPLEMENTATION_EXAMPLES.md - Section 4
- [ ] Create GraphQL schema
- [ ] Write resolvers
- [ ] Test with GraphiQL UI

### Beyond
- [ ] Complete chosen learning path
- [ ] Extend domain with new entities
- [ ] Performance test all modules
- [ ] Document your findings

---

## 🎓 Learning Outcomes by Module

After each module, you should understand:

**Module 01** ✅ REST fundamentals, HTTP, CRUD operations, status codes  
**Module 02** ✅ Async patterns, WebClient, non-blocking callbacks  
**Module 03** ✅ Reactive streams, backpressure, Mono/Flux operators  
**Module 04** ✅ GraphQL schema, queries, mutations, resolvers  
**Module 05** ✅ SOAP/XML, WSDL, XSD, contract-first design  
**Module 06** ✅ gRPC, Protobuf, binary protocol, performance  
**Module 07** ✅ WebSocket, STOMP, real-time, bidirectional comms  
**Module 08** ✅ API Gateway, routing, composition, load balancing  

---

## 💬 Getting Help

1. **Setup issues?** → Check [docs/SETUP_GUIDE.md](docs/SETUP_GUIDE.md) - Troubleshooting
2. **"When should I use X?"** → Check [docs/API_COMPARISON.md](docs/API_COMPARISON.md) - Decision tree
3. **"How do I do Y?"** → Check [docs/IMPLEMENTATION_EXAMPLES.md](docs/IMPLEMENTATION_EXAMPLES.md)
4. **General questions?** → Check [README.md](README.md)
5. **Quick reference?** → Check [QUICKSTART.md](QUICKSTART.md)

---

## 🏁 Ready to Begin?

**Recommended first 30 minutes:**

```bash
# 1. Read the README (5 min)
cat README.md | less

# 2. Build the project (10 min)
cd /Users/nvenugopal/Documents/Workspace/java-api-architecture-lab
mvn clean install

# 3. Explore the domain (5 min)
ls -la 00-shared-domain/src/main/java/com/apiarchlab/domain/
cat 00-shared-domain/src/main/java/com/apiarchlab/domain/entity/Order.java

# 4. Run Module 01 (10 min)
mvn spring-boot:run -pl 01-rest-api-springmvc
# Visit http://localhost:8080/api/orders
```

**Then choose your learning path above and start implementing!**

---

**Happy Learning!** 🚀

*Last Updated: June 4, 2026*  
*Status: Ready to Use* ✅

