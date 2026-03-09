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
- [Project Structure](#project-structure)
- [Known Limitations & Future Work](#known-limitations--future-work)

---

## Overview

This application provides a minimal web front end and REST back end for managing disaster relief supply requests, targeted at flood-impacted regions. When a user submits a request, the backend sends an HTML-formatted confirmation email via Gmail's SMTP service.

---

## Tech Stack

| Layer       | Technology                                      |
|-------------|-------------------------------------------------|
| Language    | Java 17                                         |
| Framework   | Spring Boot 3.4.1                               |
| Templating  | Thymeleaf                                       |
| Email       | Spring Boot Mail + Jakarta Mail (Gmail SMTP)    |
| Data (mock) | Jackson ObjectMapper backed by a local JSON file|
| Build       | Maven (Maven Wrapper included)                  |

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
  "message": "Request sent successfully!"
}
```

---

## Project Structure

```
src/main/
├── java/com/example/DisasterRelief/
│   ├── DisasterReliefApplication.java   # Application entry point
│   ├── Entity/
│   │   └── Resources.java               # Placeholder entity class
│   ├── controller/
│   │   ├── HomeController.java          # GET /
│   │   ├── RequestController.java       # GET /request
│   │   ├── ResourcesController.java     # POST /api/send-request
│   │   └── UserController.java          # Skeleton (future user management)
│   └── service/
│       ├── EmailService.java            # Sends HTML emails via Gmail SMTP
│       ├── DataBaseService.java         # Mock JSON-file persistence layer
│       └── UserService.java             # Skeleton (future user logic)
└── resources/
    ├── application.properties           # App configuration & email credentials
    ├── templates/
    │   ├── index.html                   # Home page template
    │   └── request.html                 # Request form template
    └── static/
        ├── style.css                    # Home page styles
        ├── style2.css                   # Request page styles
        ├── background.jpg               # Background image
        └── images/                      # Supply images
```

---

## Known Limitations & Future Work

- **Mock database:** The current persistence layer writes to a local `data.json` file. Replace `DataBaseService` with a real database (e.g., MySQL / PostgreSQL) and configure the data source in `application.properties`.
- **Hardcoded email values:** The confirmation email currently contains hardcoded resource amounts and a recipient address. These should be driven by the actual request data and made configurable.
- **No input validation:** Add server-side validation (e.g., via `jakarta.validation`) to sanitize user inputs before processing.
- **User management:** `UserController` and `UserService` are empty stubs intended for future authentication / user profile features.
- **Port forwarding:** To expose the application publicly without sharing your IP, you can use a tunneling service such as [ngrok](https://ngrok.com/) or [Serveo](https://serveo.net/).
