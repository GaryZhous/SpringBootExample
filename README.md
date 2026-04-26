# Disaster Relief – Spring Boot Web Application

A Spring Boot prototype for coordinating disaster relief resource requests. Users can submit requests for essential supplies (blankets, water, food, etc.) through a simple web interface, and an email confirmation is automatically sent upon submission.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [Building & Running](#building--running)
- [Usage](#usage)
- [Authentication](#authentication)
- [API Reference](#api-reference)
- [User Management API](#user-management-api)
- [Caching & Idempotency](#caching--idempotency)
- [Validation](#validation)
- [Project Structure](#project-structure)
- [Known Limitations & Future Work](#known-limitations--future-work)

---

## Overview

This application provides a minimal web front end and REST back end for managing disaster relief supply requests, targeted at flood-impacted regions. When a user submits a request, the backend sends an HTML-formatted confirmation email via Gmail's SMTP service. The application also includes a JPA-backed subscription store with Caffeine caching, idempotent request processing via a client-supplied `Idempotency-Key` header, a full user management API, and **JWT-based authentication with role-based access control**.

---

## Tech Stack

| Layer         | Technology                                           |
|---------------|------------------------------------------------------|
| Language      | Java 17                                              |
| Framework     | Spring Boot 3.4.1                                    |
| Templating    | Thymeleaf                                            |
| Email         | Spring Boot Mail + Jakarta Mail (Gmail SMTP)         |
| Data          | Spring Data JPA + H2 (in-memory, default) — switchable to MySQL / PostgreSQL |
| Caching       | Spring Cache + Caffeine (managed by Spring Boot BOM) |
| Security      | Spring Security 6 + JWT (JJWT 0.12.6)               |
| Build         | Maven (Maven Wrapper included)                       |

---

## Prerequisites

- **Java 17** or later
- **Maven 3.6+** (or use the included `./mvnw` / `mvnw.cmd` wrapper)
- A **Gmail account** with [App Passwords](https://support.google.com/accounts/answer/185833) enabled (required for SMTP authentication)

---

## Configuration

Before running the application, update `src/main/resources/application.properties` with your own credentials:

```properties
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

> **Note:** Use a Gmail [App Password](https://support.google.com/accounts/answer/185833), not your regular account password. Never commit real credentials to source control – consider using environment variables or a secrets manager for production deployments.

### JWT configuration

```properties
# Secret key used to sign JWT tokens – MUST be changed in production (min 32 characters)
jwt.secret=your-strong-secret-key-at-least-32-chars
# Token validity in milliseconds (default: 24 h)
jwt.expiration-ms=86400000
```

> **Environment variable override:** Spring Boot's [relaxed binding](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.typesafe-configuration-properties.relaxed-binding) maps environment variables to properties automatically. For example, set `JWT_SECRET=my-secret` to override `jwt.secret` without modifying the properties file.

### Recipient email

The confirmation email is sent to the address configured via:

```properties
app.recipient-email=authorities@example.com
```

Override this with the real address of your local coordination team, or set `APP_RECIPIENT_EMAIL` as an environment variable.

### Admin account bootstrap

To automatically create an ADMIN account on first start-up, set all three properties (or the corresponding environment variables):

```properties
app.admin.username=admin
app.admin.email=admin@example.com
app.admin.password=ChangeMe123!
```

| Environment variable   | Equivalent property      |
|------------------------|--------------------------|
| `APP_ADMIN_USERNAME`   | `app.admin.username`     |
| `APP_ADMIN_EMAIL`      | `app.admin.email`        |
| `APP_ADMIN_PASSWORD`   | `app.admin.password`     |

If a user with the configured username already exists, the bootstrap is silently skipped.

> **Security note:** Supply the admin password via an environment variable or a secrets manager in production – never commit it to source control.

### Database

By default the application uses an **H2 in-memory database** that requires no external server. Data is lost when the application stops. To persist data across restarts or use a production-grade RDBMS, switch to MySQL or PostgreSQL:

**MySQL:**

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/disaster_relief?useSSL=false&serverTimezone=UTC
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=your_mysql_user
spring.datasource.password=your_mysql_password
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
```

Add the MySQL driver to `pom.xml` (already present as a commented dependency):

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

**PostgreSQL:**

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/disaster_relief
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=your_pg_user
spring.datasource.password=your_pg_password
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
```

Add the PostgreSQL driver to `pom.xml` (already present as a commented dependency):

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

To change the default port (8080), add the following line:

```properties
server.port=8081
```

---

## Building & Running

**Build the project:**

```bash
# Linux / macOS
./mvnw clean package

# Windows
mvnw.cmd clean package
```

**Run the application:**

```bash
# Using the Maven wrapper
./mvnw spring-boot:run

# Or run the packaged JAR directly
java -jar target/DisasterRelief-0.0.1-SNAPSHOT.jar
```

Once started, open your browser at `http://localhost:8080`.

---

## Usage

1. **Home page** (`/`) – Displays a welcome message with a "Begin Request" button.
2. **Request page** (`/request`) – Fill in your name, address, and the quantity of each supply type (towels/blankets, instant noodles, tissue paper, bottled water), then click **Send Request**.
3. A confirmation email is sent to the configured recipient, and a success message is shown in the browser.

---

## Authentication

The API uses **stateless JWT-based authentication**. All requests to protected endpoints must include a signed JWT in the `Authorization` header.

### Roles

| Role    | Description                                                    |
|---------|----------------------------------------------------------------|
| `USER`  | Can submit disaster relief requests (`POST /api/send-request`) |
| `ADMIN` | Full access to all user management endpoints                   |

### Login flow

1. **Register** (self-registration, public): `POST /api/users`
2. **Login**: `POST /api/auth/login` → receive a JWT
3. **Use token**: include `Authorization: Bearer <token>` in every protected request

### `POST /api/auth/login`

**Request body (JSON):**

```json
{
  "username": "alice",
  "password": "secret"
}
```

**Success response (`200 OK`):**

```json
{
  "token": "<signed-jwt>"
}
```

**Failure response (`401 Unauthorized`):**

```json
{
  "error": "Invalid username or password"
}
```

### Endpoint access matrix

| Endpoint                     | Method | `USER` | `ADMIN` | Public |
|------------------------------|--------|--------|---------|--------|
| `/`                          | GET    | ✓      | ✓       | ✓      |
| `/request`                   | GET    | ✓      | ✓       | ✓      |
| `/api/auth/login`            | POST   | —      | —       | ✓      |
| `/api/users`                 | POST   | —      | —       | ✓ (registration) |
| `/api/send-request`          | POST   | ✓      | ✓       | ✗      |
| `/api/users`                 | GET    | ✗      | ✓       | ✗      |
| `/api/users/{id}`            | GET    | ✗      | ✓       | ✗      |
| `/api/users/{id}`            | PUT    | ✗      | ✓       | ✗      |
| `/api/users/{id}`            | DELETE | ✗      | ✓       | ✗      |

---

## API Reference

| Method | Endpoint            | Description                                            |
|--------|---------------------|--------------------------------------------------------|
| GET    | `/`                 | Renders the home page                                  |
| GET    | `/request`          | Renders the resource-request form                      |
| POST   | `/api/auth/login`   | Authenticate and receive a JWT                         |
| POST   | `/api/send-request` | Accepts a JSON request body and sends a confirmation email (requires authentication) |

### `POST /api/send-request`

**Required header:**

| Header          | Description                                      |
|-----------------|--------------------------------------------------|
| `Authorization` | `Bearer <jwt>` — token obtained from `/api/auth/login` |

**Optional request header:**

| Header            | Description                                                                                     |
|-------------------|-------------------------------------------------------------------------------------------------|
| `Idempotency-Key` | A unique client-generated key (e.g. UUID). If supplied, duplicate requests with the same key return the cached response without resending the email. |

**Request body (JSON):**

```json
{
  "name": "Jane Doe",
  "address": "123 Main St",
  "towel": 2,
  "instantNoodles": 5,
  "tissuePaper": 3,
  "water": 10
}
```

**Success response (`200 OK`):**

```json
{
  "message": "Request received successfully!"
}
```

---

## User Management API

User data is persisted in a JPA-managed database (H2 by default, switchable to MySQL / PostgreSQL) and cached in the `users` Caffeine cache (10-minute TTL, up to 1 000 entries). Each user has a server-assigned UUID, a username, an email address, a role (`USER` or `ADMIN`), and a BCrypt-hashed password. **Passwords are never included in API responses.**

| Method | Endpoint            | Auth required | Description                |
|--------|---------------------|---------------|----------------------------|
| GET    | `/api/users`        | ADMIN         | List all users             |
| GET    | `/api/users/{id}`   | ADMIN         | Get a user by id           |
| POST   | `/api/users`        | None          | Self-register a new user   |
| PUT    | `/api/users/{id}`   | ADMIN         | Update an existing user    |
| DELETE | `/api/users/{id}`   | ADMIN         | Delete a user              |

### `POST /api/users` — Self-registration

**Request body (JSON):**

```json
{
  "username": "jane",
  "email": "jane@example.com",
  "password": "mypassword"
}
```

> `username`, `email`, and `password` are required. New registrations are always assigned the `USER` role.

**Success response (`200 OK`):**

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "username": "jane",
  "email": "jane@example.com",
  "role": "USER"
}
```

**Error response (`400 Bad Request`):**

```json
{
  "error": "username, email and password are required"
}
```

### `PUT /api/users/{id}` — Update a user (ADMIN only)

Accepts the same JSON body as the create endpoint except `password`. Returns `404 Not Found` if no user with the given id exists.

### `DELETE /api/users/{id}` (ADMIN only)

**Success response (`200 OK`):**

```json
{
  "message": "User deleted successfully"
}
```

Returns `404 Not Found` if no user with the given id exists.

---

## Caching & Idempotency

Spring Cache is enabled via `@EnableCaching` on the main application class and is backed by [Caffeine](https://github.com/ben-manes/caffeine) caches configured in `CacheConfig`:

| Cache name      | TTL    | Max entries | Purpose                                              |
|-----------------|--------|-------------|------------------------------------------------------|
| `subscriptions` | 10 min | 500         | Caches `DataBaseService.getAllSubscriptions()` results; evicted on write or delete. |
| `idempotency`   | 24 h   | 10 000      | Stores responses keyed by `Idempotency-Key` headers to prevent duplicate email sends. |
| `users`         | 10 min | 1 000       | Caches `UserService.getAllUsers()` results; evicted on create, update, or delete. |

**How idempotency works:**

1. The client includes an `Idempotency-Key: <uuid>` header in the `POST /api/send-request` request.
2. If the key has been seen before (and has not yet expired), the cached response is returned immediately and no email is sent.
3. If the key is new, the request is processed normally, the email is sent, and the response is stored in the `idempotency` cache for 24 hours.
4. After the 24-hour TTL elapses the key is evicted, so a subsequent request using the same key will be treated as a brand-new request and will trigger a new email.
5. Requests without an `Idempotency-Key` header are always processed (no deduplication).

---

## Validation

All API endpoints enforce server-side validation via **`jakarta.validation`** annotations on dedicated DTO classes. When a request body fails validation, the API returns `400 Bad Request` with a structured error body:

```json
{
  "error": "Validation failed",
  "details": [
    "name: Name is required",
    "address: Address is required"
  ]
}
```

### Validation rules per endpoint

#### `POST /api/send-request`

| Field           | Constraint                             |
|-----------------|----------------------------------------|
| `name`          | Required, max 100 characters           |
| `address`       | Required, max 200 characters           |
| `towel`         | Integer ≥ 0                            |
| `instantNoodles`| Integer ≥ 0                            |
| `tissuePaper`   | Integer ≥ 0                            |
| `water`         | Integer ≥ 0                            |

#### `POST /api/users` (self-registration)

| Field      | Constraint                                      |
|------------|-------------------------------------------------|
| `username` | Required, 3–50 characters                       |
| `email`    | Required, must be a valid email address         |
| `password` | Required, at least 8 characters                 |

#### `PUT /api/users/{id}` (ADMIN only)

| Field      | Constraint                                      |
|------------|-------------------------------------------------|
| `username` | Required, 3–50 characters                       |
| `email`    | Required, must be a valid email address         |
| `role`     | Must be `USER` or `ADMIN`                       |

#### `POST /api/auth/login`

| Field      | Constraint |
|------------|------------|
| `username` | Required   |
| `password` | Required   |

---

## Project Structure

```
src/
├── main/
│   └── java/com/example/DisasterRelief/
│       ├── DisasterReliefApplication.java   # Application entry point; enables caching (@EnableCaching)
│       ├── Entity/
│       │   ├── Resources.java               # Placeholder entity class
│       │   ├── Subscription.java            # JPA entity (name, email, address) – persisted to DB
│       │   └── User.java                    # JPA entity (id, username, email, role, password)
│       ├── config/
│       │   ├── CacheConfig.java             # Caffeine cache configuration (subscriptions + idempotency + users)
│       │   └── SecurityConfig.java          # Spring Security filter chain, RBAC rules, JWT integration
│       ├── controller/
│       │   ├── AuthController.java          # POST /api/auth/login – returns a signed JWT
│       │   ├── HomeController.java          # GET /
│       │   ├── RequestController.java       # GET /request
│       │   ├── ResourcesController.java     # POST /api/send-request (with idempotency support)
│       │   └── UserController.java          # CRUD /api/users endpoints (role-protected)
│       ├── dto/
│       │   ├── CreateUserRequest.java       # Validated DTO for POST /api/users
│       │   ├── LoginRequest.java            # Validated DTO for POST /api/auth/login
│       │   ├── ReliefRequestDto.java        # Validated DTO for POST /api/send-request
│       │   └── UpdateUserRequest.java       # Validated DTO for PUT /api/users/{id}
│       ├── exception/
│       │   └── GlobalExceptionHandler.java  # Translates validation errors to 400 Bad Request responses
│       ├── repository/
│       │   ├── SubscriptionRepository.java  # Spring Data JPA repository for Subscription entities
│       │   └── UserRepository.java          # Spring Data JPA repository for User entities
│       ├── security/
│       │   ├── AppUserDetailsService.java   # Spring Security UserDetailsService adapter
│       │   ├── JwtAuthenticationFilter.java # Per-request JWT validation filter
│       │   └── JwtUtil.java                 # JWT generation and validation (JJWT 0.12.6)
│       └── service/
│           ├── AdminBootstrapService.java   # Creates an ADMIN account on start-up (if configured)
│           ├── DataBaseService.java         # Subscription persistence with @Cacheable/@CacheEvict
│           ├── EmailService.java            # Sends HTML emails via Gmail SMTP
│           ├── IdempotencyService.java      # Idempotency key lookup and storage
│           └── UserService.java             # User CRUD logic backed by JPA
└── resources/
    ├── application.properties               # App configuration, email, JWT & database credentials
    ├── templates/
    │   ├── index.html                       # Home page template
    │   └── request.html                     # Request form template
    └── static/
        ├── style.css                        # Home page styles
        ├── style2.css                       # Request page styles
        ├── background.jpg                   # Background image
        └── images/                          # Supply images

src/
└── test/
    └── java/com/example/DisasterRelief/
        ├── DisasterReliefApplicationTests.java  # Context load smoke test
        ├── CachingTest.java                     # Verifies @Cacheable/@CacheEvict behaviour
        ├── IdempotencyTest.java                 # Verifies idempotency key deduplication
        ├── SecurityTest.java                    # Verifies JWT auth and RBAC rules
        ├── UserManagementTest.java              # Verifies user CRUD and cache behaviour
        └── ValidationTest.java                  # Verifies jakarta.validation constraints on all endpoints
```

---

## Known Limitations & Future Work
- **Port forwarding:** To expose the application publicly without sharing your IP, you can use a tunneling service such as [ngrok](https://ngrok.com/) or [Serveo](https://serveo.net/).

