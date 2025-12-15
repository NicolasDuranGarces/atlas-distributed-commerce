<p align="center">
  <img src="https://raw.githubusercontent.com/github/explore/main/topics/spring-boot/spring-boot.png" alt="Atlas Commerce" width="120"/>
</p>

<h1 align="center">🌐 Atlas Distributed Commerce</h1>

<p align="center">
  <strong>Enterprise-grade distributed e-commerce platform built with microservices architecture</strong>
</p>

<p align="center">
  <a href="#-features">Features</a> •
  <a href="#-architecture">Architecture</a> •
  <a href="#-tech-stack">Tech Stack</a> •
  <a href="#-getting-started">Getting Started</a> •
  <a href="#-api-documentation">API Docs</a> •
  <a href="#-project-structure">Structure</a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Spring_Cloud-2023.0-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Cloud"/>
  <img src="https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker"/>
  <img src="https://img.shields.io/badge/PostgreSQL-16-336791?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL"/>
</p>

<p align="center">
  <a href="https://github.com/NicolasDuranGarces/atlas-distributed-commerce/actions/workflows/ci-cd.yml">
    <img src="https://github.com/NicolasDuranGarces/atlas-distributed-commerce/actions/workflows/ci-cd.yml/badge.svg" alt="CI/CD Pipeline"/>
  </a>
  <a href="https://github.com/NicolasDuranGarces/atlas-distributed-commerce/actions/workflows/pr-check.yml">
    <img src="https://github.com/NicolasDuranGarces/atlas-distributed-commerce/actions/workflows/pr-check.yml/badge.svg" alt="PR Check"/>
  </a>
  <img src="https://img.shields.io/badge/coverage-80%25-brightgreen?style=flat-square" alt="Coverage"/>
  <img src="https://img.shields.io/badge/tests-passing-brightgreen?style=flat-square" alt="Tests"/>
  <a href="https://github.com/NicolasDuranGarces/atlas-distributed-commerce/blob/main/LICENSE">
    <img src="https://img.shields.io/github/license/NicolasDuranGarces/atlas-distributed-commerce?style=flat-square" alt="License"/>
  </a>
</p>

---

## 📋 Overview

**Atlas Distributed Commerce** is a production-ready e-commerce backend platform demonstrating modern microservices architecture principles. Built as a portfolio project to showcase enterprise-level software engineering skills, it implements industry best practices for distributed systems, including service discovery, centralized configuration, event-driven communication, and resilient design patterns.

### 🎯 Project Goals

- Demonstrate proficiency in **Spring Cloud** ecosystem and microservices patterns
- Implement **distributed transactions** using the SAGA pattern
- Showcase **event-driven architecture** with RabbitMQ
- Apply **security best practices** with JWT authentication
- Build **containerized services** ready for cloud deployment

---

## ✨ Features

### 🔐 Authentication & Authorization
- JWT-based stateless authentication
- Role-based access control (USER, SELLER, ADMIN)
- BCrypt password encryption
- Account lockout protection against brute force attacks
- Token refresh mechanism

### 🛒 E-Commerce Core
- Product catalog with hierarchical categories
- Real-time inventory management with optimistic locking
- Shopping cart and checkout flow
- Order processing with distributed transactions
- Payment processing with idempotency guarantees

### 🏗️ Infrastructure Features
- **Service Discovery**: Dynamic service registration and discovery with Eureka
- **Centralized Configuration**: Externalized configuration management
- **API Gateway**: Single entry point with routing, rate limiting, and security
- **Circuit Breakers**: Graceful degradation with Resilience4j
- **Distributed Caching**: Redis-based caching for improved performance
- **Async Messaging**: Event-driven communication via RabbitMQ

### 📊 Observability
- Prometheus metrics collection
- Grafana dashboards
- Distributed tracing with Zipkin
- Health checks and actuator endpoints

---

## 🏛️ Architecture

```
                                    ┌─────────────────────────────────────┐
                                    │         CLIENT APPLICATIONS         │
                                    │       (Web, Mobile, Third-Party)    │
                                    └─────────────────┬───────────────────┘
                                                      │
                                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                    API GATEWAY (:8080)                                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌───────────────┐  │
│  │ JWT Filter  │  │Rate Limiter │  │Load Balancer│  │Circuit Break│  │ Request Router│  │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘  └───────────────┘  │
└─────────────────────────────────────────────────────────────────────────────────────────┘
                                                      │
        ┌──────────────────┬──────────────────┬───────┴───────┬──────────────────┐
        ▼                  ▼                  ▼               ▼                  ▼
┌───────────────┐  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐
│    USER       │  │   PRODUCT     │  │    ORDER      │  │   PAYMENT     │  │ NOTIFICATION  │
│   SERVICE     │  │   SERVICE     │  │   SERVICE     │  │   SERVICE     │  │   SERVICE     │
│    :8081      │  │    :8082      │  │    :8083      │  │    :8084      │  │    :8085      │
├───────────────┤  ├───────────────┤  ├───────────────┤  ├───────────────┤  ├───────────────┤
│ • Auth & JWT  │  │ • Catalog     │  │ • SAGA Pattern│  │ • Idempotency │  │ • Email/SMS   │
│ • Registration│  │ • Inventory   │  │ • Checkout    │  │ • Refunds     │  │ • Templates   │
│ • Profiles    │  │ • Categories  │  │ • Cart        │  │ • Gateway Sim │  │ • Events      │
└───────┬───────┘  └───────┬───────┘  └───────┬───────┘  └───────┬───────┘  └───────────────┘
        │                  │                  │                  │                  ▲
        ▼                  ▼                  ▼                  ▼                  │
┌───────────────┐  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐          │
│  PostgreSQL   │  │  PostgreSQL   │  │  PostgreSQL   │  │  PostgreSQL   │          │
│  atlas_users  │  │atlas_products │  │ atlas_orders  │  │atlas_payments │          │
└───────────────┘  └───────────────┘  └───────────────┘  └───────────────┘          │
                                                                                     │
                          ┌──────────────────────────────────────────────────────────┘
                          │  Event-Driven Communication
                          ▼
        ┌─────────────────────────────────────────────────────────────────────────────┐
        │                              RABBITMQ                                        │
        │   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐    │
        │   │order.created│  │payment.done │  │inventory.upd│  │notification.send│    │
        │   └─────────────┘  └─────────────┘  └─────────────┘  └─────────────────┘    │
        └─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                 INFRASTRUCTURE LAYER                                     │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐    │
│  │  EUREKA SERVER  │  │  CONFIG SERVER  │  │      REDIS      │  │     ZIPKIN      │    │
│  │     :8761       │  │     :8888       │  │     :6379       │  │     :9411       │    │
│  │Service Registry │  │Config Management│  │  Cache + Rate   │  │Distributed Trace│    │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

### Key Architecture Decisions

| Decision | Rationale |
|----------|-----------|
| **Database per Service** | Ensures loose coupling and independent deployability |
| **API Gateway Pattern** | Single entry point for security, routing, and cross-cutting concerns |
| **Event-Driven Async** | Decouples services and improves resilience |
| **SAGA Pattern** | Handles distributed transactions without 2PC overhead |
| **Circuit Breaker** | Prevents cascade failures in distributed systems |

---

## 🛠️ Tech Stack

### Core Framework
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 (LTS) | Primary language with modern features |
| Spring Boot | 3.2.1 | Application framework |
| Spring Cloud | 2023.0 (Leyton) | Microservices infrastructure |
| Maven | 3.9+ | Build and dependency management |

### Spring Cloud Components
| Component | Technology | Purpose |
|-----------|------------|---------|
| Service Discovery | Netflix Eureka | Dynamic service registration |
| Configuration | Spring Cloud Config | Centralized configuration |
| Gateway | Spring Cloud Gateway | API routing and filtering |
| Circuit Breaker | Resilience4j | Fault tolerance |
| Load Balancer | Spring Cloud LoadBalancer | Client-side load balancing |
| OpenFeign | Spring Cloud OpenFeign | Declarative REST clients |

### Data & Messaging
| Technology | Purpose |
|------------|---------|
| PostgreSQL 16 | Primary data store (per service) |
| Redis 7 | Caching & rate limiting |
| RabbitMQ 3.12 | Async message broker |

### Security
| Technology | Purpose |
|------------|---------|
| Spring Security | Authentication/Authorization |
| JWT (JJWT 0.12) | Stateless tokens |
| BCrypt | Password hashing |

### DevOps & Observability
| Technology | Purpose |
|------------|---------|
| Docker | Containerization |
| Docker Compose | Local orchestration |
| Prometheus | Metrics collection |
| Grafana | Metrics visualization |
| Zipkin | Distributed tracing |

---

## 🚀 Getting Started

### Prerequisites

```bash
# Required software
- Java 21+
- Maven 3.8+
- Docker & Docker Compose
- Git
```

### Installation

```bash
# 1. Clone the repository
git clone https://github.com/NicolasDuranGarces/atlas-distributed-commerce.git
cd atlas-distributed-commerce

# 2. Build all services
mvn clean package -DskipTests

# 3. Start the infrastructure
docker-compose up -d

# 4. Verify all services are running
docker-compose ps
```

### Service Startup Order

The `docker-compose.yml` handles dependencies, but the logical order is:

1. **Databases** (PostgreSQL instances)
2. **Messaging** (Redis, RabbitMQ)
3. **Infrastructure** (Eureka, Config Server)
4. **Gateway** (API Gateway)
5. **Services** (User, Product, Order, Payment, Notification)

### Access Points

| Service | URL | Credentials |
|---------|-----|-------------|
| 🌐 API Gateway | http://localhost:8080 | - |
| 📋 Eureka Dashboard | http://localhost:8761 | `admin` / `admin123` |
| 🐰 RabbitMQ Management | http://localhost:15672 | `guest` / `guest` |
| 📊 Grafana | http://localhost:3000 | `admin` / `admin` |
| 🔍 Zipkin | http://localhost:9411 | - |
| 📈 Prometheus | http://localhost:9090 | - |

---

## 📖 API Documentation

### Authentication

#### Register User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890"
  }'
```

**Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "john.doe@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "role": "USER"
    }
  }
}
```

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "SecurePass123!"
  }'
```

### Products

#### Create Product (Admin)
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "LAPTOP-001",
    "name": "MacBook Pro 16",
    "description": "Apple M3 Pro chip, 18GB RAM, 512GB SSD",
    "price": 2499.99,
    "compareAtPrice": 2699.99,
    "stockQuantity": 50,
    "brand": "Apple",
    "tags": ["electronics", "laptop", "apple"]
  }'
```

#### Search Products
```bash
curl "http://localhost:8080/api/products/search?q=laptop&page=0&size=20"
```

### Orders

#### Create Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {"productId": "uuid-here", "quantity": 2}
    ],
    "shippingAddress": {
      "street": "123 Tech Street",
      "city": "San Francisco",
      "state": "CA",
      "postalCode": "94102",
      "country": "USA",
      "recipientName": "John Doe",
      "recipientPhone": "+1234567890"
    },
    "paymentMethod": "CREDIT_CARD"
  }'
```

### Payments

#### Process Payment
```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "order-uuid-here",
    "idempotencyKey": "unique-key-12345",
    "amount": 4999.98,
    "paymentMethod": "CREDIT_CARD",
    "cardLastFour": "4242",
    "cardBrand": "VISA"
  }'
```

---

## 📁 Project Structure

```
atlas-distributed-commerce/
│
├── 📄 pom.xml                          # Parent POM (multi-module)
├── 📄 docker-compose.yml               # Full stack orchestration
├── 📄 .env.example                     # Environment template
├── 📄 README.md                        # This file
│
├── 📁 infrastructure/                  # Infrastructure services
│   ├── 📁 eureka-server/               # Service Discovery
│   │   ├── src/main/java/.../
│   │   │   ├── EurekaServerApplication.java
│   │   │   └── config/SecurityConfig.java
│   │   ├── src/main/resources/application.yml
│   │   └── Dockerfile
│   │
│   ├── 📁 config-server/               # Centralized Configuration
│   │   ├── src/main/java/.../ConfigServerApplication.java
│   │   ├── src/main/resources/
│   │   │   ├── application.yml
│   │   │   └── config-repo/            # Service configurations
│   │   │       ├── application.yml     # Shared defaults
│   │   │       ├── user-service.yml
│   │   │       ├── product-service.yml
│   │   │       └── ...
│   │   └── Dockerfile
│   │
│   ├── 📁 api-gateway/                 # API Gateway
│   │   ├── src/main/java/.../
│   │   │   ├── ApiGatewayApplication.java
│   │   │   ├── filter/AuthFilter.java
│   │   │   ├── config/RateLimiterConfig.java
│   │   │   └── controller/FallbackController.java
│   │   ├── src/main/resources/application.yml
│   │   └── Dockerfile
│   │
│   └── 📁 monitoring/                  # Observability configs
│       ├── prometheus/prometheus.yml
│       └── grafana/...
│
├── 📁 services/                        # Business microservices
│   ├── 📁 user-service/                # Authentication & Users
│   │   ├── src/main/java/.../
│   │   │   ├── entity/User.java, Address.java, Role.java
│   │   │   ├── repository/UserRepository.java
│   │   │   ├── service/AuthService.java
│   │   │   ├── controller/AuthController.java
│   │   │   ├── dto/LoginRequest.java, RegisterRequest.java, ...
│   │   │   └── config/SecurityConfig.java, RabbitMQConfig.java
│   │   ├── src/main/resources/application.yml
│   │   └── Dockerfile
│   │
│   ├── 📁 product-service/             # Catalog & Inventory
│   │   ├── src/main/java/.../
│   │   │   ├── entity/Product.java, Category.java
│   │   │   ├── repository/ProductRepository.java
│   │   │   ├── service/ProductService.java
│   │   │   └── controller/ProductController.java
│   │   └── Dockerfile
│   │
│   ├── 📁 order-service/               # Order Management
│   │   ├── src/main/java/.../
│   │   │   ├── entity/Order.java, OrderItem.java
│   │   │   ├── client/ProductClient.java  # Feign client
│   │   │   ├── service/OrderService.java  # SAGA implementation
│   │   │   └── controller/OrderController.java
│   │   └── Dockerfile
│   │
│   ├── 📁 payment-service/             # Payment Processing
│   │   ├── src/main/java/.../
│   │   │   ├── entity/Payment.java
│   │   │   ├── service/PaymentService.java  # Idempotent
│   │   │   └── controller/PaymentController.java
│   │   └── Dockerfile
│   │
│   └── 📁 notification-service/        # Notifications
│       ├── src/main/java/.../
│       │   ├── listener/NotificationEventListener.java
│       │   ├── service/EmailService.java
│       │   └── config/RabbitMQConfig.java
│       ├── src/main/resources/templates/  # Thymeleaf
│       └── Dockerfile
│
└── 📁 shared/                          # Shared libraries
    ├── 📁 common-models/               # DTOs & Events
    │   └── src/main/java/.../
    │       ├── entity/BaseEntity.java
    │       ├── dto/ApiResponse.java, PagedResponse.java
    │       └── event/OrderCreatedEvent.java, ...
    │
    └── 📁 common-utils/                # Utilities
        └── src/main/java/.../
            ├── exception/GlobalExceptionHandler.java, ...
            └── security/JwtTokenProvider.java
```

---

## 🎨 Design Patterns

### SAGA Pattern (Order Service)
Implements choreography-based saga for order creation:

```
┌─────────┐     ┌─────────┐     ┌─────────┐     ┌─────────┐
│ CREATE  │────▶│ RESERVE │────▶│ PROCESS │────▶│ CONFIRM │
│  ORDER  │     │INVENTORY│     │ PAYMENT │     │  ORDER  │
└─────────┘     └────┬────┘     └────┬────┘     └─────────┘
                     │               │
                     ▼               ▼
              ┌────────────┐  ┌────────────┐
              │  ROLLBACK  │  │  ROLLBACK  │
              │ INVENTORY  │◀─│  PAYMENT   │
              └────────────┘  └────────────┘
```

### Circuit Breaker Pattern
Configured in API Gateway using Resilience4j:
- **Failure threshold**: 50%
- **Wait duration**: 10 seconds
- **Fallback**: Returns graceful error response

### Idempotency Pattern (Payment Service)
Ensures safe retries using idempotency keys:
```java
if (paymentRepository.existsByIdempotencyKey(key)) {
    return existingPayment; // No duplicate processing
}
```

---

## 🧪 Testing

```bash
# Run unit tests
mvn test

# Run with coverage
mvn test jacoco:report

# Integration tests (requires Docker)
mvn verify -P integration-tests
```

---

## 🔒 Security Features

| Feature | Implementation |
|---------|----------------|
| **Password Storage** | BCrypt with strength 12 |
| **Token Auth** | JWT with HS256, 24h expiry |
| **Rate Limiting** | Redis-backed, 10 req/sec per user |
| **Account Protection** | Lockout after 5 failed attempts |
| **Input Validation** | Jakarta Bean Validation |
| **CORS** | Configured in API Gateway |

---

## 📊 Monitoring & Observability

### Metrics (Prometheus)
All services expose metrics at `/actuator/prometheus`:
- HTTP request rates and latencies
- JVM memory and GC stats
- Database connection pool metrics
- Circuit breaker state

### Tracing (Zipkin)
Distributed request tracing across all services for debugging and performance analysis.

### Logging
Structured JSON logging with correlation IDs for request tracking.

---

## 🚢 Deployment

### Docker Compose (Development)
```bash
docker-compose up -d
```

### Kubernetes (Production)
The architecture is ready for Kubernetes deployment:
- Stateless services with horizontal scaling
- ConfigMaps for externalized configuration
- Secrets for sensitive data
- Service mesh compatible

---

## 📝 Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `EUREKA_HOST` | Eureka server host | `localhost` |
| `CONFIG_HOST` | Config server host | `localhost` |
| `POSTGRES_HOST` | Database host | `localhost` |
| `REDIS_HOST` | Redis host | `localhost` |
| `RABBITMQ_HOST` | RabbitMQ host | `localhost` |
| `JWT_SECRET` | JWT signing key | (required) |

See [.env.example](.env.example) for complete list.

---

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Nicolás Durán Garcés**

- GitHub: [@NicolasDuranGarces](https://github.com/NicolasDuranGarces)

---

<p align="center">
  <strong>Built with ❤️ using Spring Cloud & Microservices Architecture</strong>
</p>
