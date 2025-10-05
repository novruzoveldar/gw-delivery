# gw-delivery

## 🧭 About

The **gw-delivery** microservice provides secure gateway-level management for all delivery-related microservices in the Guavapay delivery platform.

It leverages **Spring Cloud Gateway** and **Eureka Service Discovery** to dynamically route incoming requests to registered microservices, such as:

* `ms-delivery-admin` – for admin and parcel management
* `ms-courier-order` – for courier order management
* `ms-courier` – for courier information and operations

Additionally, JWT token validation and header propagation are implemented to ensure consistent authentication across all downstream services.

---

## 🚀 Features

* API Gateway for all delivery microservices
* Dynamic service discovery via **Eureka**
* Secure authentication via **JWT** header propagation
* Load balancing & routing through **Spring Cloud Gateway**
* Configurable routes via `application.yml` or Config Server
* CORS configuration for frontend integration
* Request logging and global exception handling
* Spring Boot Actuator endpoints for health monitoring

---

## 🛠 Tech Stack

| Component                 | Description                    |
| ------------------------- | ------------------------------ |
| **Java 11+**              | Programming language           |
| **Spring Boot 2.6+**      | Framework                      |
| **Spring Cloud Gateway**  | API Gateway & routing          |
| **Spring Cloud Eureka**   | Service discovery              |
| **Spring Security (JWT)** | Authentication & authorization |
| **Spring Cloud Config**   | Externalized configuration     |
| **Gradle**                | Build system                   |
| **Swagger / OpenAPI**     | API documentation (if enabled) |

---

## 📦 Project Structure

```
gw-delivery/
├── src/
│   ├── main/
│   │   ├── java/com/guavapay/gateway/
│   │   │   ├── config/        # Security, Gateway, and CORS configurations
│   │   │   ├── filter/        # Global filters (e.g., logging, auth propagation)
│   │   │   ├── util/          # Utility classes
│   │   │   └── GwDeliveryApplication.java
│   │   └── resources/
│   │       ├── application.yml   # Route and environment configuration
│   │       └── bootstrap.yml     # Spring Cloud config setup
│   └── test/                     # Unit & integration tests
├── build.gradle
└── settings.gradle
```

---

## ⚙️ Configuration

The gateway supports configuration via **Spring Cloud Config** or local YAML files.

### Key Properties

```yaml
spring:
  application:
    name: gw-delivery
  cloud:
    config:
      uri: http://localhost:8888
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
  main:
    allow-bean-definition-overriding: true

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true

server:
  port: 8080

application:
  security:
    jwt:
      secret: your-secret-key
      token-validity-in-seconds: 86400
```

---

## 🧩 Routing Example

Example route configuration in `application.yml`:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: ms-delivery-admin
          uri: lb://MS-DELIVERY-ADMIN
          predicates:
            - Path=/admin/**
          filters:
            - StripPrefix=1

        - id: ms-courier-order
          uri: lb://MS-COURIER-ORDER
          predicates:
            - Path=/courier/order/**
          filters:
            - StripPrefix=1

        - id: ms-parcel-order
          uri: lb://MS-PARCEL-ORDER
          predicates:
            - Path=/parcel/**
          filters:
            - StripPrefix=1
```

This configuration dynamically routes requests such as:

```
/admin/**        → ms-delivery-admin
/courier/order/** → ms-courier-order
/parcel/**       → ms-parcel-order
```

---

## 🔒 Security

* JWT tokens are validated at the gateway level and passed downstream in `Authorization` headers.
* Each microservice performs its own token verification for additional security.
* Optional **CORS** rules can be configured to allow frontend origins.

Example CORS configuration:

```yaml
application:
  cors:
    allowed-origins: http://localhost:3000
    allowed-methods: GET,POST,PUT,DELETE,OPTIONS
```

---

## 🧰 Build and Run

### Build

```bash
./gradlew clean build
```

### Run Locally

```bash
./gradlew bootRun
```

or run the jar directly:

```bash
java -jar build/libs/gw-delivery-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=local
```

---

## 🧪 Health and Monitoring

Spring Boot Actuator is typically enabled.
Access via:

```
GET /actuator/health
GET /actuator/info
```

---

## 🐳 Docker (optional)

Example minimal `Dockerfile`:

```dockerfile
FROM openjdk:11-jre-slim
WORKDIR /app
COPY build/libs/gw-delivery-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 🧭 Troubleshooting

| Issue                        | Possible Cause                   | Fix                                                                    |
| ---------------------------- | -------------------------------- | ---------------------------------------------------------------------- |
| Gateway not routing requests | Service not registered in Eureka | Ensure dependent services are up                                       |
| `401 Unauthorized`           | Missing or invalid JWT           | Check token validity and secret                                        |
| Config not loaded            | Config Server not available      | Use local `application.yml` or set `spring.cloud.config.enabled=false` |
| CORS blocked                 | Missing CORS config              | Add allowed origins in gateway config                                  |

---

## 👥 Contributors

* **Eldar Novruzov** – Lead Developer
* Contributions welcome via pull requests!

---

## 📄 License

Licensed under the **MIT License**.
See the [LICENSE](./LICENSE) file for details.

---
