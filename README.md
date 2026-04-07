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
- [API Reference](#api-reference)
- [User Management API](#user-management-api)
- [Caching & Idempotency](#caching--idempotency)
- [Project Structure](#project-structure)
- [Known Limitations & Future Work](#known-limitations--future-work)

---

## Overview

This application provides a minimal web front end and REST back end for managing disaster relief supply requests, targeted at flood-impacted regions. When a user submits a request, the backend sends an HTML-formatted confirmation email via Gmail's SMTP service. The application also includes a JSON-file-backed subscription store with Caffeine caching, idempotent request processing via a client-supplied `Idempotency-Key` header, and a full user management API.

---

## Tech Stack

| Layer         | Technology                                           |
|---------------|------------------------------------------------------|
| Language      | Java 17                                              |
| Framework     | Spring Boot 3.4.1                                    |
| Templating    | Thymeleaf                                            |
| Email         | Spring Boot Mail + Jakarta Mail (Gmail SMTP)         |
| Data (mock)   | Jackson ObjectMapper backed by a local JSON file     |
| Caching       | Spring Cache + Caffeine (managed by Spring Boot BOM) |
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

## API Reference

| Method | Endpoint            | Description                                            |
|--------|---------------------|--------------------------------------------------------|
| GET    | `/`                 | Renders the home page                                  |
| GET    | `/request`          | Renders the resource-request form                      |
| POST   | `/api/send-request` | Accepts a JSON request body and sends a confirmation email |

### `POST /api/send-request`

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

User data is persisted in a local `users.json` file and cached in the `users` Caffeine cache (10-minute TTL, up to 1 000 entries). Each user has a server-assigned UUID, a username, an email address, and a role (`USER` or `ADMIN`).

| Method | Endpoint            | Description              |
|--------|---------------------|--------------------------|
| GET    | `/api/users`        | List all users           |
| GET    | `/api/users/{id}`   | Get a user by id         |
| POST   | `/api/users`        | Create a new user        |
| PUT    | `/api/users/{id}`   | Update an existing user  |
| DELETE | `/api/users/{id}`   | Delete a user            |

### `POST /api/users` — Create a user

**Request body (JSON):**

```json
{
  "username": "jane",
  "email": "jane@example.com",
  "role": "USER"
}
```

> `username` and `email` are required. `role` defaults to `USER` when omitted.

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
  "error": "username and email are required"
}
```

### `PUT /api/users/{id}` — Update a user

Accepts the same JSON body as the create endpoint. Returns `404 Not Found` if no user with the given id exists.

### `DELETE /api/users/{id}`

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

## Project Structure

```
src/
├── main/
│   └── java/com/example/DisasterRelief/
│       ├── DisasterReliefApplication.java   # Application entry point; enables caching (@EnableCaching)
│       ├── Entity/
│       │   ├── Resources.java               # Placeholder entity class
│       │   ├── Subscription.java            # Subscription entity (name, email, address)
│       │   └── User.java                    # User entity (id, username, email, role)
│       ├── config/
│       │   └── CacheConfig.java             # Caffeine cache configuration (subscriptions + idempotency + users)
│       ├── controller/
│       │   ├── HomeController.java          # GET /
│       │   ├── RequestController.java       # GET /request
│       │   ├── ResourcesController.java     # POST /api/send-request (with idempotency support)
│       │   └── UserController.java          # CRUD /api/users endpoints
│       └── service/
│           ├── DataBaseService.java         # JSON-file persistence with @Cacheable/@CacheEvict
│           ├── EmailService.java            # Sends HTML emails via Gmail SMTP
│           ├── IdempotencyService.java      # Idempotency key lookup and storage
│           └── UserService.java             # User CRUD logic backed by users.json
└── resources/
    ├── application.properties               # App configuration & email credentials
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
        └── UserManagementTest.java              # Verifies user CRUD and cache behaviour
```

---

## Known Limitations & Future Work

- **Mock database:** The current persistence layer writes to local JSON files (`data.json`, `users.json`). Replace `DataBaseService` and `UserService` with a real database (e.g., MySQL / PostgreSQL) and configure the data source in `application.properties`.
- **Hardcoded recipient email:** The confirmation email is sent to a hardcoded recipient address inside `ResourcesController`. This should be made configurable (e.g., driven by the request payload or an `application.properties` value).
- **Basic input sanitisation:** User-supplied fields are sanitised with `HtmlUtils.htmlEscape` to prevent XSS. Full server-side validation (e.g., via `jakarta.validation`) is not yet implemented.
- **No authentication:** The user management API is currently open. Adding Spring Security (JWT / session-based auth) and role-based access control is recommended before any production use.
- **Port forwarding:** To expose the application publicly without sharing your IP, you can use a tunneling service such as [ngrok](https://ngrok.com/) or [Serveo](https://serveo.net/).
