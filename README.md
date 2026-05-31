<div align="center">

# ⚙️ Enterprise Management Platform - Backend API

### RESTful API & WebSocket Server for Business Management System

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white)](https://spring.io/projects/spring-security)

**🔗 [Frontend Repository](https://github.com/hamayari/Pfe-Frontend)** • [API Documentation](#) • [Report Bug](#)

> **Note:** This is the backend API repository. For the Angular frontend, visit the [frontend repository](https://github.com/hamayari/Pfe-Frontend).

</div>

---

## 🎯 Overview

**Enterprise-grade RESTful API** built with Spring Boot, providing secure and scalable backend services for business management, invoicing, real-time messaging, and AI-powered analytics.

> 🔗 **Frontend Repository:** [Angular Frontend](https://github.com/hamayari/Pfe-Frontend)

### 🌟 Key Features

- 🔐 **JWT Authentication** - Secure token-based auth with refresh tokens
- 📱 **Two-Factor Authentication (2FA)** - TOTP implementation
- 🔒 **Role-Based Access Control** - Multi-level permission system
- 💬 **WebSocket Server** - Real-time messaging with STOMP protocol
- 📊 **RESTful APIs** - 50+ endpoints for complete business operations
- 🗄️ **PostgreSQL Database** - Optimized schema with JPA/Hibernate
- 📧 **Email Service** - Automated notifications and alerts
- 📱 **SMS Integration** - Alert delivery system
- 🤖 **AI Integration Ready** - Endpoints for chatbot and analytics
- 📄 **OCR Processing** - Invoice validation and data extraction
- 📈 **KPI & Monitoring** - System metrics and business analytics
- 🔔 **Notification System** - Multi-channel notification delivery

---

## 🛠️ Tech Stack

### **Core Framework**
```
☕ Java 17                    🍃 Spring Boot 3.x
🔒 Spring Security 6.x        🗄️ Spring Data JPA
🔌 Spring WebSocket           📧 Spring Mail
```

### **Database & ORM**
```
🐘 PostgreSQL 15              🔄 Hibernate ORM
📊 Flyway/Liquibase          💾 Connection Pooling (HikariCP)
```

### **Security**
```
🔐 JWT (jjwt)                🔒 BCrypt Password Encoding
🛡️ CORS Configuration        🚫 CSRF Protection
📱 TOTP (2FA)                🔑 OAuth2 Ready
```

### **Real-Time & Messaging**
```
🔌 WebSocket (STOMP)         📡 SockJS Fallback
💬 Message Broker            🔄 Real-time Updates
```

### **Integrations**
```
📧 JavaMail API              📱 SMS Gateway
🤖 AI APIs (Gemini)          📄 OCR Services
📊 Reporting Libraries       📁 File Storage
```

---

## 📁 Project Architecture

```
src/main/java/
├── 📂 config/                      # Configuration Classes
│   ├── SecurityConfig.java         # Spring Security setup
│   ├── WebSocketConfig.java        # WebSocket configuration
│   ├── CorsConfig.java             # CORS settings
│   └── JwtConfig.java              # JWT configuration
│
├── 📂 controller/                  # REST Controllers (50+ endpoints)
│   ├── AuthController.java         # Authentication endpoints
│   ├── UserController.java         # User management
│   ├── ConventionController.java   # Contract management
│   ├── InvoiceController.java      # Invoice operations
│   ├── MessageController.java      # Messaging endpoints
│   ├── NotificationController.java # Notification management
│   └── KpiController.java          # KPI & analytics
│
├── 📂 service/                     # Business Logic Layer
│   ├── AuthService.java            # Authentication logic
│   ├── UserService.java            # User operations
│   ├── ConventionService.java      # Contract business logic
│   ├── InvoiceService.java         # Invoice processing
│   ├── MessagingService.java       # Real-time messaging
│   ├── NotificationService.java    # Notification delivery
│   ├── EmailService.java           # Email sending
│   └── OcrService.java             # OCR processing
│
├── 📂 repository/                  # Data Access Layer
│   ├── UserRepository.java         # User data access
│   ├── ConventionRepository.java   # Contract data access
│   ├── InvoiceRepository.java      # Invoice data access
│   └── MessageRepository.java      # Message data access
│
├── 📂 model/                       # Entity Classes
│   ├── User.java                   # User entity
│   ├── Convention.java             # Contract entity
│   ├── Invoice.java                # Invoice entity
│   ├── Message.java                # Message entity
│   └── Notification.java           # Notification entity
│
├── 📂 dto/                         # Data Transfer Objects
│   ├── request/                    # Request DTOs
│   └── response/                   # Response DTOs
│
├── 📂 security/                    # Security Components
│   ├── JwtTokenProvider.java       # JWT generation/validation
│   ├── JwtAuthFilter.java          # JWT authentication filter
│   ├── UserDetailsServiceImpl.java # User details service
│   └── TwoFactorAuthService.java   # 2FA implementation
│
├── 📂 exception/                   # Exception Handling
│   ├── GlobalExceptionHandler.java # Global error handler
│   └── CustomExceptions.java       # Custom exceptions
│
└── 📂 util/                        # Utility Classes
    ├── DateUtil.java               # Date utilities
    ├── ValidationUtil.java         # Validation helpers
    └── FileUtil.java               # File operations
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 17+** (JDK)
- **Maven 3.8+**
- **PostgreSQL 15+**
- **Git**

### Installation

```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/YOUR_BACKEND_REPO.git
cd YOUR_BACKEND_REPO

# Configure database
# Edit src/main/resources/application.properties

# Install dependencies
mvn clean install

# Run the application
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

---

## ⚙️ Configuration

### Database Setup

```properties
# src/main/resources/application.properties

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/your_database
spring.datasource.username=your_username
spring.datasource.password=your_password

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT Configuration
jwt.secret=your-secret-key
jwt.expiration=86400000
jwt.refresh-expiration=604800000

# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

---

## 📡 API Endpoints Overview

### Authentication
```
POST   /api/auth/register          # User registration
POST   /api/auth/login             # User login
POST   /api/auth/refresh           # Refresh token
POST   /api/auth/logout            # User logout
POST   /api/auth/2fa/enable        # Enable 2FA
POST   /api/auth/2fa/verify        # Verify 2FA code
POST   /api/auth/forgot-password   # Password reset request
POST   /api/auth/reset-password    # Reset password
```

### Users
```
GET    /api/users                  # Get all users
GET    /api/users/{id}             # Get user by ID
PUT    /api/users/{id}             # Update user
DELETE /api/users/{id}             # Delete user
GET    /api/users/profile          # Get current user profile
```

### Contracts (Conventions)
```
GET    /api/conventions            # Get all contracts
POST   /api/conventions            # Create contract
GET    /api/conventions/{id}       # Get contract by ID
PUT    /api/conventions/{id}       # Update contract
DELETE /api/conventions/{id}       # Delete contract
```

### Invoices
```
GET    /api/invoices               # Get all invoices
POST   /api/invoices               # Create invoice
GET    /api/invoices/{id}          # Get invoice by ID
PUT    /api/invoices/{id}          # Update invoice
POST   /api/invoices/{id}/payment  # Record payment
GET    /api/invoices/{id}/pdf      # Generate PDF
```

### Notifications
```
GET    /api/notifications          # Get user notifications
POST   /api/notifications/mark-read # Mark as read
DELETE /api/notifications/{id}     # Delete notification
GET    /api/notifications/preferences # Get preferences
PUT    /api/notifications/preferences # Update preferences
```

### WebSocket
```
CONNECT /ws                        # WebSocket connection
SUBSCRIBE /topic/messages          # Subscribe to messages
SEND    /app/chat.send             # Send message
```

---

## 🔒 Security Features

- ✅ JWT token-based authentication
- ✅ Refresh token mechanism
- ✅ Two-Factor Authentication (TOTP)
- ✅ Role-based access control (RBAC)
- ✅ Password encryption (BCrypt)
- ✅ CORS configuration
- ✅ CSRF protection
- ✅ SQL injection prevention
- ✅ XSS protection
- ✅ Rate limiting
- ✅ Audit logging

---

## 🐳 Docker Deployment

```bash
# Build Docker image
docker build -t enterprise-backend .

# Run with Docker Compose
docker-compose up -d
```

### docker-compose.yml
```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: enterprise_db
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
  
  backend:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - postgres
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/enterprise_db
```

---

## 📊 Database Schema

### Core Tables
- `users` - User accounts and authentication
- `roles` - User roles and permissions
- `conventions` - Business contracts
- `invoices` - Invoice records
- `payments` - Payment transactions
- `messages` - Chat messages
- `notifications` - User notifications
- `audit_logs` - System audit trail
- `kpi_alerts` - KPI monitoring alerts

---

## 🧪 Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify

# Generate coverage report
mvn jacoco:report
```

---

## 📚 API Documentation

API documentation is available via **Swagger/OpenAPI**:

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📝 License

This project is licensed under the **MIT License**.

---

## 📧 Contact

**Project Links:**
- ⚙️ Backend Repository: [This Repo](https://github.com/YOUR_USERNAME/YOUR_BACKEND_REPO)
- 🎨 Frontend Repository: [Frontend Repo](https://github.com/hamayari/Pfe-Frontend)

---

<div align="center">

## 🌟 Backend Highlights for Recruiters

### **Technical Skills Demonstrated**

```
Java 17 • Spring Boot 3.x • Spring Security • Spring Data JPA
PostgreSQL • Hibernate • JWT Authentication • WebSocket (STOMP)
RESTful API Design • Microservices Architecture • Docker
Maven • JUnit • Mockito • Swagger/OpenAPI
Email Integration • SMS Integration • OCR Processing
2FA (TOTP) • RBAC • Audit Logging • Performance Optimization
```

### **Architecture & Design Patterns**

- 🏗️ **Layered Architecture** (Controller → Service → Repository)
- 🔄 **Dependency Injection** (Spring IoC)
- 🎯 **DTO Pattern** for data transfer
- 🛡️ **Security Best Practices** (JWT, BCrypt, CORS)
- 📊 **Database Optimization** (Indexing, Query optimization)
- 🔌 **Real-Time Communication** (WebSocket)
- 📧 **Event-Driven** notification system

---

### ⭐ If this project helps you, please give it a star!

**Built with ❤️ for modern enterprise management**

</div>
