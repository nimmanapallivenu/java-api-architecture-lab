# Module 05: SOAP API with Spring Web Services

## Overview

This module demonstrates a **production-ready SOAP (Simple Object Access Protocol) API** implementation using Spring Web Services. It showcases contract-first WSDL design, XML Schema validation, WS-Security, SOAP fault handling, and integration patterns commonly used in enterprise and legacy systems.

## 🎯 Learning Objectives

- **Contract-First Design**: Define WSDL and XSD before implementation
- **SOAP Protocol**: Understanding SOAP envelope, header, body, and fault
- **XML Schema (XSD)**: Strong typing with XML Schema Definition
- **JAXB**: Java Architecture for XML Binding
- **WS-Security**: Authentication, encryption, and digital signatures
- **SOAP Fault Handling**: Proper error responses
- **WSDL Generation**: Auto-generate from XSD
- **Testing**: SoapUI and integration tests

## 📋 Technology Stack

- **Spring Boot 3.2.x**
- **Spring Web Services 4.0.x**
- **JAXB (Jakarta XML Binding)**
- **Apache CXF** (alternative framework)
- **WSDL4J** (WSDL parsing)
- **Spring Data JPA**
- **H2 Database**
- **SoapUI** (testing tool)

## 🏗️ Architecture

```
SOAP Client (SoapUI/Legacy System)
         ↓
   SOAP Envelope (XML)
         ↓
   Spring WS Endpoint
         ↓
   JAXB Marshalling/Unmarshalling
         ↓
   Service Layer
         ↓
   Repository Layer
         ↓
   Database
```

## 📁 Project Structure

```
05-soap-api/
├── src/main/
│   ├── java/com/apiarchlab/soap/
│   │   ├── SoapApiApplication.java
│   │   ├── config/
│   │   │   ├── WebServiceConfig.java
│   │   │   └── SecurityConfig.java
│   │   ├── endpoint/
│   │   │   ├── CustomerEndpoint.java
│   │   │   ├── OrderEndpoint.java
│   │   │   └── PaymentEndpoint.java
│   │   ├── service/
│   │   │   ├── CustomerService.java
│   │   │   ├── OrderService.java
│   │   │   └── PaymentService.java
│   │   ├── repository/
│   │   │   ├── CustomerRepository.java
│   │   │   ├── OrderRepository.java
│   │   │   └── PaymentRepository.java
│   │   ├── entity/
│   │   │   ├── CustomerEntity.java
│   │   │   ├── OrderEntity.java
│   │   │   └── PaymentEntity.java
│   │   ├── mapper/
│   │   │   └── EntityMapper.java
│   │   ├── exception/
│   │   │   ├── SoapFaultException.java
│   │   │   └── DetailSoapFaultDefinitionExceptionResolver.java
│   │   └── interceptor/
│   │       ├── LoggingInterceptor.java
│   │       └── SecurityInterceptor.java
│   └── resources/
│       ├── application.yml
│       ├── wsdl/
│       │   └── orders.wsdl (auto-generated)
│       └── xsd/
│           ├── orders.xsd
│           ├── customers.xsd
│           ├── payments.xsd
│           └── common.xsd
└── pom.xml
```

## 📄 XML Schema Design

### Common Types (common.xsd)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
           targetNamespace="http://apiarchlab.com/soap/common"
           xmlns:tns="http://apiarchlab.com/soap/common"
           elementFormDefault="qualified">

    <xs:simpleType name="OrderStatus">
        <xs:restriction base="xs:string">
            <xs:enumeration value="PENDING"/>
            <xs:enumeration value="CONFIRMED"/>
            <xs:enumeration value="PROCESSING"/>
            <xs:enumeration value="SHIPPED"/>
            <xs:enumeration value="DELIVERED"/>
            <xs:enumeration value="CANCELLED"/>
        </xs:restriction>
    </xs:simpleType>

    <xs:simpleType name="PaymentStatus">
        <xs:restriction base="xs:string">
            <xs:enumeration value="PENDING"/>
            <xs:enumeration value="COMPLETED"/>
            <xs:enumeration value="FAILED"/>
            <xs:enumeration value="REFUNDED"/>
        </xs:restriction>
    </xs:simpleType>

    <xs:complexType name="Address">
        <xs:sequence>
            <xs:element name="street" type="xs:string"/>
            <xs:element name="city" type="xs:string"/>
            <xs:element name="state" type="xs:string"/>
            <xs:element name="zipCode" type="xs:string"/>
            <xs:element name="country" type="xs:string"/>
        </xs:sequence>
    </xs:complexType>

    <xs:complexType name="Money">
        <xs:sequence>
            <xs:element name="amount" type="xs:decimal"/>
            <xs:element name="currency" type="xs:string"/>
        </xs:sequence>
    </xs:complexType>
</xs:schema>
```

### Customer Schema (customers.xsd)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
           targetNamespace="http://apiarchlab.com/soap/customers"
           xmlns:tns="http://apiarchlab.com/soap/customers"
           xmlns:common="http://apiarchlab.com/soap/common"
           elementFormDefault="qualified">

    <xs:import namespace="http://apiarchlab.com/soap/common"
               schemaLocation="common.xsd"/>

    <!-- Customer Type -->
    <xs:complexType name="Customer">
        <xs:sequence>
            <xs:element name="customerId" type="xs:long"/>
            <xs:element name="name" type="xs:string"/>
            <xs:element name="email" type="xs:string"/>
            <xs:element name="phone" type="xs:string"/>
            <xs:element name="address" type="common:Address" minOccurs="0"/>
            <xs:element name="createdDate" type="xs:dateTime"/>
        </xs:sequence>
    </xs:complexType>

    <!-- Get Customer Request -->
    <xs:element name="GetCustomerRequest">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="customerId" type="xs:long"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>

    <!-- Get Customer Response -->
    <xs:element name="GetCustomerResponse">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="customer" type="tns:Customer"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>

    <!-- Create Customer Request -->
    <xs:element name="CreateCustomerRequest">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="name" type="xs:string"/>
                <xs:element name="email" type="xs:string"/>
                <xs:element name="phone" type="xs:string"/>
                <xs:element name="address" type="common:Address" minOccurs="0"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>

    <!-- Create Customer Response -->
    <xs:element name="CreateCustomerResponse">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="customer" type="tns:Customer"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>

    <!-- SOAP Fault -->
    <xs:element name="CustomerFault">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="errorCode" type="xs:string"/>
                <xs:element name="errorMessage" type="xs:string"/>
                <xs:element name="timestamp" type="xs:dateTime"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>
</xs:schema>
```

### Order Schema (orders.xsd)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
           targetNamespace="http://apiarchlab.com/soap/orders"
           xmlns:tns="http://apiarchlab.com/soap/orders"
           xmlns:common="http://apiarchlab.com/soap/common"
           elementFormDefault="qualified">

    <xs:import namespace="http://apiarchlab.com/soap/common"
               schemaLocation="common.xsd"/>

    <!-- Order Item -->
    <xs:complexType name="OrderItem">
        <xs:sequence>
            <xs:element name="itemId" type="xs:long"/>
            <xs:element name="productName" type="xs:string"/>
            <xs:element name="quantity" type="xs:int"/>
            <xs:element name="unitPrice" type="common:Money"/>
            <xs:element name="totalPrice" type="common:Money"/>
        </xs:sequence>
    </xs:complexType>

    <!-- Order -->
    <xs:complexType name="Order">
        <xs:sequence>
            <xs:element name="orderId" type="xs:long"/>
            <xs:element name="orderNumber" type="xs:string"/>
            <xs:element name="customerId" type="xs:long"/>
            <xs:element name="customerName" type="xs:string"/>
            <xs:element name="items" type="tns:OrderItem" maxOccurs="unbounded"/>
            <xs:element name="totalAmount" type="common:Money"/>
            <xs:element name="status" type="common:OrderStatus"/>
            <xs:element name="orderDate" type="xs:dateTime"/>
        </xs:sequence>
    </xs:complexType>

    <!-- Get Order Request -->
    <xs:element name="GetOrderRequest">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="orderId" type="xs:long"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>

    <!-- Get Order Response -->
    <xs:element name="GetOrderResponse">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="order" type="tns:Order"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>

    <!-- Create Order Request -->
    <xs:element name="CreateOrderRequest">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="customerId" type="xs:long"/>
                <xs:element name="items" type="tns:OrderItem" maxOccurs="unbounded"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>

    <!-- Create Order Response -->
    <xs:element name="CreateOrderResponse">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="order" type="tns:Order"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>

    <!-- Update Order Status Request -->
    <xs:element name="UpdateOrderStatusRequest">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="orderId" type="xs:long"/>
                <xs:element name="status" type="common:OrderStatus"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>

    <!-- Update Order Status Response -->
    <xs:element name="UpdateOrderStatusResponse">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="order" type="tns:Order"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>
</xs:schema>
```

## 🔧 Spring Configuration

### Web Service Configuration

```java
@Configuration
@EnableWs
public class WebServiceConfig extends WsConfigurerAdapter {

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
            ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    @Bean(name = "orders")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema ordersSchema) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("OrdersPort");
        wsdl11Definition.setLocationUri("/ws");
        wsdl11Definition.setTargetNamespace("http://apiarchlab.com/soap/orders");
        wsdl11Definition.setSchema(ordersSchema);
        return wsdl11Definition;
    }

    @Bean
    public XsdSchema ordersSchema() {
        return new SimpleXsdSchema(new ClassPathResource("xsd/orders.xsd"));
    }

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("com.apiarchlab.soap.generated");
        return marshaller;
    }
}
```

## 📡 SOAP Endpoint Implementation

### Customer Endpoint

```java
@Endpoint
public class CustomerEndpoint {
    
    private static final String NAMESPACE_URI = "http://apiarchlab.com/soap/customers";
    
    @Autowired
    private CustomerService customerService;
    
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetCustomerRequest")
    @ResponsePayload
    public GetCustomerResponse getCustomer(@RequestPayload GetCustomerRequest request) {
        Customer customer = customerService.findById(request.getCustomerId());
        
        GetCustomerResponse response = new GetCustomerResponse();
        response.setCustomer(customer);
        return response;
    }
    
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "CreateCustomerRequest")
    @ResponsePayload
    public CreateCustomerResponse createCustomer(@RequestPayload CreateCustomerRequest request) {
        Customer customer = customerService.createCustomer(
            request.getName(),
            request.getEmail(),
            request.getPhone(),
            request.getAddress()
        );
        
        CreateCustomerResponse response = new CreateCustomerResponse();
        response.setCustomer(customer);
        return response;
    }
}
```

### Order Endpoint

```java
@Endpoint
public class OrderEndpoint {
    
    private static final String NAMESPACE_URI = "http://apiarchlab.com/soap/orders";
    
    @Autowired
    private OrderService orderService;
    
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetOrderRequest")
    @ResponsePayload
    public GetOrderResponse getOrder(@RequestPayload GetOrderRequest request) 
            throws SoapFaultException {
        try {
            Order order = orderService.findById(request.getOrderId());
            
            GetOrderResponse response = new GetOrderResponse();
            response.setOrder(order);
            return response;
        } catch (OrderNotFoundException ex) {
            throw new SoapFaultException("Order not found: " + request.getOrderId());
        }
    }
    
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "CreateOrderRequest")
    @ResponsePayload
    public CreateOrderResponse createOrder(@RequestPayload CreateOrderRequest request) {
        Order order = orderService.createOrder(
            request.getCustomerId(),
            request.getItems()
        );
        
        CreateOrderResponse response = new CreateOrderResponse();
        response.setOrder(order);
        return response;
    }
    
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "UpdateOrderStatusRequest")
    @ResponsePayload
    public UpdateOrderStatusResponse updateOrderStatus(
            @RequestPayload UpdateOrderStatusRequest request) {
        Order order = orderService.updateStatus(
            request.getOrderId(),
            request.getStatus()
        );
        
        UpdateOrderStatusResponse response = new UpdateOrderStatusResponse();
        response.setOrder(order);
        return response;
    }
}
```

## 🛡️ SOAP Fault Handling

```java
@Component
public class DetailSoapFaultDefinitionExceptionResolver 
        extends SoapFaultMappingExceptionResolver {
    
    private static final QName CODE = new QName("errorCode");
    private static final QName MESSAGE = new QName("errorMessage");
    
    @Override
    protected void customizeFault(Object endpoint, Exception ex, SoapFault fault) {
        if (ex instanceof SoapFaultException) {
            SoapFaultException soapEx = (SoapFaultException) ex;
            
            SoapFaultDetail detail = fault.addFaultDetail();
            detail.addFaultDetailElement(CODE).addText(soapEx.getErrorCode());
            detail.addFaultDetailElement(MESSAGE).addText(soapEx.getMessage());
        }
    }
}
```

## 📨 Sample SOAP Request/Response

### Request: Get Order

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:ord="http://apiarchlab.com/soap/orders">
   <soapenv:Header/>
   <soapenv:Body>
      <ord:GetOrderRequest>
         <ord:orderId>1</ord:orderId>
      </ord:GetOrderRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

### Response: Get Order

```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
   <SOAP-ENV:Header/>
   <SOAP-ENV:Body>
      <ns2:GetOrderResponse xmlns:ns2="http://apiarchlab.com/soap/orders">
         <ns2:order>
            <ns2:orderId>1</ns2:orderId>
            <ns2:orderNumber>ORD-2024-001</ns2:orderNumber>
            <ns2:customerId>1</ns2:customerId>
            <ns2:customerName>John Doe</ns2:customerName>
            <ns2:items>
               <ns2:itemId>1</ns2:itemId>
               <ns2:productName>Laptop</ns2:productName>
               <ns2:quantity>1</ns2:quantity>
               <ns2:unitPrice>
                  <amount>1200.00</amount>
                  <currency>USD</currency>
               </ns2:unitPrice>
               <ns2:totalPrice>
                  <amount>1200.00</amount>
                  <currency>USD</currency>
               </ns2:totalPrice>
            </ns2:items>
            <ns2:totalAmount>
               <amount>1200.00</amount>
               <currency>USD</currency>
            </ns2:totalAmount>
            <ns2:status>PENDING</ns2:status>
            <ns2:orderDate>2024-01-15T10:30:00</ns2:orderDate>
         </ns2:order>
      </ns2:GetOrderResponse>
   </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

### SOAP Fault Response

```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
   <SOAP-ENV:Body>
      <SOAP-ENV:Fault>
         <faultcode>SOAP-ENV:Server</faultcode>
         <faultstring xml:lang="en">Order not found</faultstring>
         <detail>
            <errorCode>ORDER_NOT_FOUND</errorCode>
            <errorMessage>Order with ID 999 does not exist</errorMessage>
            <timestamp>2024-01-15T10:30:00Z</timestamp>
         </detail>
      </SOAP-ENV:Fault>
   </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

## 🧪 Testing with SoapUI

### 1. Import WSDL
```
http://localhost:8085/ws/orders.wsdl
```

### 2. Create Test Suite
- Right-click on project → New TestSuite
- Add test cases for each operation
- Configure assertions

### 3. Sample Test Case
```groovy
// Groovy script for SoapUI
def response = context.expand('${GetOrder#Response}')
assert response.contains('<ns2:orderId>1</ns2:orderId>')
assert response.contains('<ns2:status>PENDING</ns2:status>')
```

## 🔒 WS-Security Implementation

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public Wss4jSecurityInterceptor securityInterceptor() {
        Wss4jSecurityInterceptor security = new Wss4jSecurityInterceptor();
        
        // Validation
        security.setValidationActions("UsernameToken");
        security.setValidationCallbackHandler(callbackHandler());
        
        // Encryption (optional)
        security.setSecurementActions("Encrypt");
        security.setSecurementEncryptionKeyIdentifier("DirectReference");
        
        return security;
    }
    
    @Bean
    public SimplePasswordValidationCallbackHandler callbackHandler() {
        SimplePasswordValidationCallbackHandler handler = 
            new SimplePasswordValidationCallbackHandler();
        handler.setUsersMap(Map.of("admin", "secret"));
        return handler;
    }
}
```

## 📊 SOAP vs REST Comparison

| Feature | SOAP | REST |
|---------|------|------|
| **Protocol** | XML-based protocol | Architectural style |
| **Transport** | HTTP, SMTP, TCP | Primarily HTTP |
| **Message Format** | XML only | JSON, XML, etc. |
| **Contract** | WSDL (strict) | OpenAPI (flexible) |
| **Security** | WS-Security | OAuth, JWT |
| **Statefulness** | Can be stateful | Stateless |
| **Error Handling** | SOAP Faults | HTTP status codes |
| **Performance** | Slower (XML overhead) | Faster (JSON) |
| **Best For** | Enterprise, B2B, Banking | Modern web, mobile |

## 🚀 Running the Application

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run

# Access WSDL
http://localhost:8085/ws/orders.wsdl
http://localhost:8085/ws/customers.wsdl

# SOAP Endpoint
http://localhost:8085/ws
```

## 📚 Key Takeaways

1. **Contract-First**: Always define XSD/WSDL before coding
2. **Strong Typing**: XML Schema provides compile-time safety
3. **Enterprise Ready**: Built-in security, transactions, reliability
4. **Legacy Integration**: Essential for B2B and legacy systems
5. **Verbose**: More overhead than REST but more features
6. **Tooling**: Excellent tool support (SoapUI, WSDL2Java)
7. **Standards**: WS-* standards for security, reliability, transactions

## 🔗 Related Modules

- **Module 01**: REST API (modern alternative)
- **Module 04**: GraphQL (flexible alternative)
- **Module 06**: gRPC (high-performance alternative)

## 📖 Additional Resources

- [Spring Web Services Documentation](https://spring.io/projects/spring-ws)
- [SOAP Specification](https://www.w3.org/TR/soap/)
- [WS-Security](https://www.oasis-open.org/committees/wss/)
- [SoapUI](https://www.soapui.org/)

---

**Next Module**: [06-grpc-api](../06-grpc-api) - High-performance RPC with Protocol Buffers