# 🛍️ EyeShop Backend

### An E-Commerce Platform for Local People and Businesses

**EyeShop Backend** is a Spring Boot REST API powering an e-commerce platform designed for local customers and businesses. Built with Java 21 and Spring Boot 3.3.5, it follows a feature-based modular architecture with a strong focus on authentication, security, maintainability, and extensibility.

The backend provides authentication and authorization, product catalog management, and order processing, with a security system featuring JWT authentication, refresh token rotation, two-factor authentication (2FA), OAuth2 social login, and role- and permission-based access control.

---

## 📑 Table of Contents

* [Overview](#-overview)
* [Features](#-features)
* [Tech Stack](#️-tech-stack)
* [Architecture](#️-architecture)
* [Project Structure](#-project-structure)
* [Getting Started](#-getting-started)
* [API Documentation](#-api-documentation)
* [Authentication & Security](#-authentication--security)
* [Database](#️-database)
* [Modules](#-modules)
* [Testing](#-testing)
* [Roadmap](#️-roadmap)
* [Contributing](#-contributing)
* [License](#-license)
* [Contact](#-contact)

---

## 🧭 Overview

EyeShop Backend is the server-side component of the EyeShop e-commerce platform. It exposes RESTful APIs that allow clients to authenticate users, manage products, and create and track orders.

The application uses a **package-by-feature architecture**, keeping related controllers, services, repositories, entities, and DTOs organized within their respective modules.

### Key Highlights

* Secure authentication using JWT access and refresh tokens.
* Refresh token rotation with HttpOnly cookies.
* TOTP-based two-factor authentication with backup codes.
* OAuth2 social login.
* Granular role- and permission-based authorization.
* Email verification and password recovery.
* Account lockout and rate limiting.
* PostgreSQL database with Flyway migrations.
* Interactive API documentation with Swagger UI.

## ✨ Features

### 🔐 Authentication & Security

* **JWT Authentication:** Short-lived access tokens and long-lived refresh tokens.
* **Refresh Token Rotation:** Refresh tokens managed through HttpOnly cookies.
* **Two-Factor Authentication (2FA):** TOTP authentication compatible with authenticator apps.
* **Backup Codes:** Recovery codes for two-factor authentication.
* **OAuth2 Social Login:** Google and other configured OAuth2 providers.
* **Email Verification:** Verify user email addresses during registration.
* **Password Recovery:** Forgot-password and reset-password workflows.
* **Account Lockout:** Temporarily lock accounts after repeated failed login attempts.
* **Rate Limiting:** Protect sensitive endpoints using Bucket4j.
* **Role-Based Access Control (RBAC):** Assign roles to users.
* **Granular Permissions:** Control access to specific operations and resources.
* **Password Hashing:** Secure password storage using BCrypt.
* **Session Management:** Manage and revoke refresh-token-based sessions.
* **Sensitive Response Protection:** Apply no-store headers where appropriate.

### 🛍️ Product Management

* Create, read, update, and delete products.
* Retrieve product catalog information through REST APIs.
* Organize product-related logic in a dedicated module.

### 📦 Order Management

* Create and manage customer orders.
* Maintain order items and their associated information.
* Track order status through a defined lifecycle, including `PENDING`, `CONFIRMED`, `SHIPPED`, and `DELIVERED`.

### ⚙️ Platform Features

* Feature-based modular architecture.
* PostgreSQL database integration.
* Automated database migrations with Flyway.
* API documentation using Swagger UI and OpenAPI.
* Email templates powered by Thymeleaf.
* Environment-specific configuration.
* Maven-based build and dependency management.

---

## 🛠️ Tech Stack

| Category                  | Technology                     |
| ------------------------- | ------------------------------ |
| Language                  | Java 21                        |
| Backend Framework         | Spring Boot 3.3.5              |
| Security                  | Spring Security                |
| Authentication            | JWT (JJWT), OAuth2 Client      |
| Two-Factor Authentication | `dev.samstevens.totp`          |
| Rate Limiting             | Bucket4j                       |
| Database                  | PostgreSQL                     |
| ORM                       | Spring Data JPA / Hibernate    |
| Database Migrations       | Flyway                         |
| Email                     | Spring Mail                    |
| Email Templates           | Thymeleaf                      |
| API Documentation         | SpringDoc OpenAPI / Swagger UI |
| Password Hashing          | BCrypt                         |
| Boilerplate Reduction     | Lombok                         |
| Build Tool                | Maven                          |

---

## 🏗️ Architecture

EyeShop Backend follows a **feature-based modular architecture (package by feature)**. Instead of grouping every controller, service, and repository into application-wide packages, each feature keeps its related components together.

### Architectural Principles

* **Separation of Concerns:** Controllers handle HTTP requests, services implement business logic, and repositories manage database access.
* **Modularity:** Authentication, products, and orders are maintained in separate feature modules.
* **Maintainability:** Related classes are grouped together for easier navigation and modification.
* **Extensibility:** New features can be introduced as separate modules without unnecessarily coupling existing functionality.

### Typical Feature Module

```text
<feature>/
├── controller/     # REST API endpoints
├── dto/            # Request and response objects
├── entity/         # JPA entities
├── repository/     # Database access
├── service/        # Business logic
│   └── impl/       # Service implementations
├── mapper/         # Entity-to-DTO mapping
└── exception/      # Feature-specific exceptions
```

---

## 📂 Project Structure

```text
EyeShop_backend/
├── pom.xml
├── mvnw
├── mvnw.cmd
├── src/
│   ├── main/
│   │   ├── java/com/eyeshop/
│   │   │   ├── BackendApplication.java
│   │   │   │
│   │   │   ├── auth/
│   │   │   │   ├── config/
│   │   │   │   ├── controllers/
│   │   │   │   ├── dto/
│   │   │   │   ├── entity/
│   │   │   │   ├── exception/
│   │   │   │   ├── helper/
│   │   │   │   ├── mapper/
│   │   │   │   ├── ratelimit/
│   │   │   │   ├── repositories/
│   │   │   │   ├── security/
│   │   │   │   └── services/
│   │   │   │
│   │   │   ├── product/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   ├── entity/
│   │   │   │   ├── repository/
│   │   │   │   └── service/
│   │   │   │
│   │   │   └── order/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       ├── repository/
│   │   │       └── service/
│   │   │
│   │   └── resources/
│   │       ├── application.yaml
│   │       ├── application-template.yaml
│   │       ├── db/
│   │       │   └── migration/
│   │       └── templates/
│   │
│   └── test/
│       └── java/com/eyeshop/
│           └── BackendApplicationTests.java
│
└── README.md
```

> **Note:** This structure reflects the documented project organization. Actual filenames and directories may vary as the codebase evolves.

---

## 🚀 Getting Started

Follow these instructions to run EyeShop Backend on your local machine.

### Prerequisites

Make sure you have the following installed:

* **Java 21 or later**
* **PostgreSQL 14 or later**
* **Git**
* **Maven 3.9+**, if you intend to use a system Maven installation

The repository includes Maven Wrapper scripts (`mvnw` and `mvnw.cmd`), which you can use instead of installing Maven separately, provided the wrapper files are present and configured correctly.

### 1. Clone the Repository

```bash
git clone https://github.com/shahry-03/EyeShop_backend.git
cd EyeShop_backend
```

### 2. Create the PostgreSQL Database

Open PostgreSQL using your preferred database client and execute:

```sql
CREATE DATABASE eyeshop;
```

Make sure your PostgreSQL server is running before starting the application.

### 3. Configure the Application

Create your local configuration file from the provided template:

**Linux / macOS:**

```bash
cp src/main/resources/application-template.yaml src/main/resources/application.yaml
```

**Windows PowerShell:**

```powershell
Copy-Item src/main/resources/application-template.yaml src/main/resources/application.yaml
```

Update the configuration with your local database credentials and other required environment-specific values.

### 4. Configure Application Properties

The following YAML illustrates the main settings you may need to configure. Adapt the property names to match your actual application configuration.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/eyeshop
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  mail:
    host: ${MAIL_HOST}
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}

  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}

app:
  jwt:
    secret: ${JWT_SECRET}
    access-token-expiry: 900000
    refresh-token-expiry: 604800000

  account-lockout:
    max-attempts: 5
    lockout-duration: 900000
```

**Configuration notes:**

* `DB_USERNAME` and `DB_PASSWORD` are your PostgreSQL credentials.
* `MAIL_HOST`, `MAIL_USERNAME`, and `MAIL_PASSWORD` configure email delivery.
* `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` are required for Google OAuth2 when enabled.
* `JWT_SECRET` must be a securely generated secret of sufficient strength for the signing algorithm used by your application.
* `access-token-expiry: 900000` represents 15 minutes in milliseconds.
* `refresh-token-expiry: 604800000` represents 7 days in milliseconds.
* `lockout-duration: 900000` represents 15 minutes in milliseconds.

**Important:** These environment variable placeholders work only when the application configuration supports them and the variables are actually provided. Ensure that the configured property names match your source code.

Never commit real credentials, OAuth2 client secrets, email passwords, or JWT signing secrets to version control.

### 5. Build the Project

Using Maven Wrapper on Linux or macOS:

```bash
./mvnw clean install
```

Using Maven Wrapper on Windows:

```powershell
.\mvnw.cmd clean install
```

### 6. Run the Application

Start the application using Maven Wrapper.

**Linux / macOS:**

```bash
./mvnw spring-boot:run
```

**Windows:**

```powershell
.\mvnw.cmd spring-boot:run
```

Alternatively, package the application and run the generated JAR:

```bash
./mvnw clean package
java -jar target/<generated-jar-name>.jar
```

Replace `<generated-jar-name>.jar` with the actual JAR filename produced by your build.

By default, the application is expected to run at:

```text
http://localhost:8080
```

The actual port may differ if it is overridden in your configuration.

---

## 📘 API Documentation

EyeShop Backend uses SpringDoc OpenAPI to document its REST APIs.

Once the application is running, open:

| Resource     | URL                                   |
| ------------ | ------------------------------------- |
| Swagger UI   | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs     |

The Swagger UI allows you to explore documented endpoints, inspect request and response schemas, and test API operations.

### Main API Groups

| Module                    | Base Path                   | Description                                                                   |
| ------------------------- | --------------------------- | ----------------------------------------------------------------------------- |
| Authentication            | `/api/v1/auth`              | Registration, login, email verification, token refresh, and password recovery |
| Two-Factor Authentication | `/api/v1/2fa`               | Configure and manage 2FA                                                      |
| Sessions                  | `/api/v1/sessions`          | Manage authentication sessions                                                |
| Admin Users               | `/api/v1/admin/users`       | User administration                                                           |
| Admin Roles               | `/api/v1/admin/roles`       | Role administration                                                           |
| Admin Permissions         | `/api/v1/admin/permissions` | Permission administration                                                     |
| Products                  | `/api/v1/products`          | Product catalog operations                                                    |
| Orders                    | `/api/v1/orders`            | Order management                                                              |

> Endpoint paths are based on the documented API groups. Consult the running Swagger UI or controller mappings for the exact routes and supported HTTP methods.

---

## 🔐 Authentication & Security

EyeShop Backend uses JWT-based authentication, with refresh-token handling designed to support secure session renewal.

### Authentication Flow

1. **Registration:** A user submits the registration details.
2. **Email Verification:** The user receives a verification email and follows the verification link.
3. **Login:** After successful authentication, the server returns a short-lived access token and sets a refresh token in an HttpOnly cookie.
4. **Authenticated Requests:** The client sends the access token in the `Authorization` header.
5. **Token Refresh:** When the access token expires, the client calls the refresh endpoint. The server validates the refresh token and issues a new access token according to the configured flow.
6. **Logout:** The server revokes the relevant refresh token and clears the refresh-token cookie.

### Authorization Header

Include the access token in authenticated requests:

```http
Authorization: Bearer <access-token>
```

### Two-Factor Authentication (2FA)

The 2FA implementation supports:

* TOTP-based authentication.
* Compatibility with supported authenticator applications.
* Backup codes for account recovery.
* Enabling and disabling 2FA through the available account endpoints.

### Account Lockout

Repeated failed login attempts can trigger a temporary account lockout.

The attempt threshold and lockout duration are configurable. For example:

| Setting                 | Example Value |
| ----------------------- | ------------- |
| Maximum failed attempts | 5             |
| Lockout duration        | 15 minutes    |

These are example configuration values, not a guarantee of the current runtime settings.

### Rate Limiting

Bucket4j is used to apply configurable rate limits to sensitive endpoints.

Example documented limits:

| Endpoint     | Example Limit             |
| ------------ | ------------------------- |
| Registration | 3 requests per hour       |
| Login        | 5 requests per 15 minutes |

Actual enforcement depends on the application's configured rate-limit rules and storage strategy.

### Roles & Permissions

EyeShop Backend supports role- and permission-based access control.

Example roles:

* `ROLE_ADMIN`
* `ROLE_USER`

Example permissions:

* `user:read`
* `user:write`

Users can be assigned roles, and roles can be associated with permissions. The resulting authorities are used to determine access to protected resources.

### Public Endpoints

Public endpoints, such as registration, login, and email verification, are configured through the application's security configuration and endpoint allowlist.

Only endpoints explicitly intended to be public should bypass authentication.

---

## 🗄️ Database

EyeShop Backend uses **PostgreSQL** as its primary relational database and **Flyway** to manage database schema migrations.

### Database Features

* Relational data storage with PostgreSQL.
* Object-relational mapping through Spring Data JPA and Hibernate.
* Version-controlled database migrations with Flyway.
* Automated migration execution during application startup, when enabled in the configuration.

### Main Tables

The documented database design includes the following tables:

| Table                 | Purpose                          |
| --------------------- | -------------------------------- |
| `users`               | User account information         |
| `roles`               | Available roles                  |
| `permissions`         | Available permissions            |
| `user_roles`          | User-to-role relationships       |
| `role_permissions`    | Role-to-permission relationships |
| `refresh_tokens`      | Refresh-token records            |
| `verification_tokens` | Email verification tokens        |
| `backup_codes`        | 2FA recovery codes               |
| `products`            | Product catalog records          |
| `orders`              | Customer orders                  |
| `order_items`         | Items associated with orders     |

Migration files are located under:

```text
src/main/resources/db/migration/
```

---

## 🧩 Modules

### 1. Authentication Module (`auth`)

Responsible for identity, authentication, and access control.

Responsibilities include:

* User registration and login.
* JWT access and refresh tokens.
* Email verification and password recovery.
* OAuth2 social login.
* Two-factor authentication.
* Session management.
* Role and permission management.
* Account lockout and rate limiting.

### 2. Product Module (`product`)

Responsible for product catalog operations.

Responsibilities include:

* Creating products.
* Retrieving product information.
* Updating product details.
* Deleting products.

### 3. Order Module (`order`)

Responsible for order processing and order lifecycle management.

Responsibilities include:

* Creating customer orders.
* Managing order items.
* Maintaining order information.
* Tracking order status.

---

## 🧪 Testing

The current documented test structure includes the default Spring Boot application context test:

```text
src/test/java/com/eyeshop/BackendApplicationTests.java
```

### Run Tests

**Linux / macOS:**

```bash
./mvnw test
```

**Windows:**

```powershell
.\mvnw.cmd test
```

### Testing Roadmap

Test coverage is a work in progress. Future testing improvements should include:

* Unit tests for service-layer business logic.
* Controller tests for REST API behavior.
* Repository tests for persistence operations.
* Integration tests for authentication and authorization.
* Tests for token refresh, revocation, and expiration.
* Tests for account lockout and rate limiting.
* Tests for product and order workflows.

---

## 🗺️ Roadmap

The following features are planned or identified for future development.

* [ ] Increase unit and integration test coverage.
* [ ] Add a CI/CD pipeline using GitHub Actions.
* [ ] Dockerize the application.
* [ ] Provide a `docker-compose.yml` for local development.
* [ ] Integrate a payment gateway.
* [ ] Add product categories and tags.
* [ ] Implement product search, filtering, and sorting.
* [ ] Add shopping cart functionality.
* [ ] Add a wishlist module.
* [ ] Improve order tracking and notifications.
* [ ] Implement seller and vendor onboarding.
* [ ] Add admin analytics APIs.
* [ ] Implement audit logging.
* [ ] Add localization and internationalization (i18n).

---

## 🤝 Contributing

Contributions, suggestions, and bug reports are welcome.

### Contribution Workflow

1. Fork the repository.
2. Create a feature branch.
3. Implement your changes.
4. Run the available tests.
5. Commit your changes using a clear commit message.
6. Push your branch and open a Pull Request.

### Example Commands

```bash
git checkout -b feature/product-search

git add .

git commit -m "feat: add product search endpoint"

git push origin feature/product-search
```

Please follow the existing coding conventions, keep changes focused, and add tests whenever possible.

---

## 📄 License

No license is currently specified in the documented project configuration.

Until a license is added, other developers should not assume that they have permission to reuse, distribute, or modify the project under an open-source license.

If you intend to publish the project for open-source or commercial collaboration, consider selecting an appropriate license, such as MIT or Apache-2.0, based on your requirements.

---

## 📬 Contact

**Author:** [shahry-03](https://github.com/shahry-03)

**GitHub Repository:** [EyeShop Backend](https://github.com/shahry-03/EyeShop_backend)

For questions, suggestions, or bug reports, please open an issue in the repository.

---

<p align="center">
  Built with ❤️ using Java and Spring Boot
</p>
