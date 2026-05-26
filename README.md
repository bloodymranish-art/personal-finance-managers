# Personal Finance Manager API

A comprehensive REST API for managing personal finances, built with Spring Boot. This system enables users to track income, expenses, savings goals, and generate financial reports.

## Live Demo

**Base URL:** https://personal-finance-managers.onrender.com/api

> **Note:** The free tier on Render may take 50-60 seconds to wake up on the first request.

---

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Security | Spring Boot Starter Security (Session-based) |
| Database | H2 (In-memory) |
| ORM | Spring Data JPA / Hibernate |
| Validation | Jakarta Validation |
| Build Tool | Maven |
| Testing | JUnit 5, Mockito |
| Deployment | Render (Docker) |

---

## Features

- **User Management** — Register, login, logout with session-based authentication
- **Transaction Management** — Full CRUD with filtering by date range and category
- **Category Management** — Default + custom categories with validation
- **Savings Goals** — Track progress with percentage completion
- **Reports** — Monthly and yearly financial summaries by category
- **Data Isolation** — Each user only accesses their own data
- **Input Validation** — Comprehensive validation with descriptive error messages

---

## Architecture

```
src/main/java/com/syfe/
├── config/
│   ├── SecurityConfig.java        # Spring Security configuration
│   └── DataInitializer.java       # Seeds default categories on startup
├── controller/
│   ├── AuthController.java        # Register, login, logout
│   ├── CategoryController.java    # Category CRUD
│   ├── TransactionController.java # Transaction CRUD
│   ├── GoalController.java        # Savings goal CRUD
│   └── ReportController.java      # Monthly/yearly reports
├── service/
│   ├── AuthService.java
│   ├── CategoryService.java
│   ├── TransactionService.java
│   ├── GoalService.java
│   └── ReportService.java
├── repository/
│   ├── UserRepository.java
│   ├── CategoryRepository.java
│   ├── TransactionRepository.java
│   └── SavingsGoalRepository.java
├── entity/
│   ├── User.java
│   ├── Category.java
│   ├── Transaction.java
│   └── SavingsGoal.java
├── dto/
│   ├── request/                   # Input DTOs with validation
│   └── response/                  # Output DTOs
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── ConflictException.java
│   └── ForbiddenException.java
└── enums/
    └── CategoryType.java          # INCOME / EXPENSE
```

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+

### Run Locally

```bash
# Clone the repository
git clone https://github.com/bloodymranish-art/personal-finance-managers.git
cd personal-finance-managers/financemanager

# Run the application
mvn spring-boot:run
```

The API will be available at `http://localhost:8080/api`

H2 Console: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:financedb`
- Username: `sa`
- Password: *(leave empty)*

### Build JAR

```bash
mvn clean package -DskipTests
java -jar target/financemanager-0.0.1-SNAPSHOT.jar
```

---

## API Documentation

### Authentication

#### Register
```
POST /api/auth/register
```
```json
{
  "username": "user@example.com",
  "password": "password123",
  "fullName": "John Doe",
  "phoneNumber": "+1234567890"
}
```
**Response (201):**
```json
{
  "message": "User registered successfully",
  "userId": 1
}
```

#### Login
```
POST /api/auth/login
```
```json
{
  "username": "user@example.com",
  "password": "password123"
}
```
**Response (200):** Sets session cookie for subsequent requests
```json
{ "message": "Login successful" }
```

#### Logout
```
POST /api/auth/logout
```
**Response (200):**
```json
{ "message": "Logout successful" }
```

---

### Transactions

#### Create Transaction
```
POST /api/transactions
```
```json
{
  "amount": 50000.00,
  "date": "2024-01-15",
  "category": "Salary",
  "description": "January Salary"
}
```

#### Get Transactions
```
GET /api/transactions?startDate=2024-01-01&endDate=2024-01-31&categoryId=1
```

#### Update Transaction
```
PUT /api/transactions/{id}
```
```json
{
  "amount": 60000.00,
  "description": "Updated Salary"
}
```

#### Delete Transaction
```
DELETE /api/transactions/{id}
```

---

### Categories

#### Get All Categories
```
GET /api/categories
```

#### Create Custom Category
```
POST /api/categories
```
```json
{
  "name": "Freelance",
  "type": "INCOME"
}
```

#### Delete Custom Category
```
DELETE /api/categories/{name}
```

**Default Categories (cannot be deleted):**
- INCOME: `Salary`
- EXPENSE: `Food`, `Rent`, `Transportation`, `Entertainment`, `Healthcare`, `Utilities`

---

### Savings Goals

#### Create Goal
```
POST /api/goals
```
```json
{
  "goalName": "Emergency Fund",
  "targetAmount": 5000.00,
  "targetDate": "2027-01-01",
  "startDate": "2024-01-01"
}
```

#### Get All Goals
```
GET /api/goals
```

#### Get Single Goal
```
GET /api/goals/{id}
```

#### Update Goal
```
PUT /api/goals/{id}
```
```json
{
  "targetAmount": 6000.00,
  "targetDate": "2027-06-01"
}
```

#### Delete Goal
```
DELETE /api/goals/{id}
```

---

### Reports

#### Monthly Report
```
GET /api/reports/monthly/{year}/{month}
```
**Example:** `GET /api/reports/monthly/2024/1`

**Response:**
```json
{
  "month": 1,
  "year": 2024,
  "totalIncome": { "Salary": 50000.00 },
  "totalExpenses": { "Rent": 1200.00, "Food": 400.00 },
  "netSavings": 48400.00
}
```

#### Yearly Report
```
GET /api/reports/yearly/{year}
```

---

## Error Handling

| Status Code | Description |
|-------------|-------------|
| 400 | Bad Request — validation errors, invalid input |
| 401 | Unauthorized — not logged in or session expired |
| 403 | Forbidden — accessing another user's data |
| 404 | Not Found — resource doesn't exist |
| 409 | Conflict — duplicate username or category name |

**Error Response Format:**
```json
{
  "error": "Category not found",
  "status": 404
}
```

---

## Design Decisions

### Session-based Authentication
Used Spring Security's session-based authentication with HTTP cookies. This is simpler than JWT for a web application where the client can store cookies, and aligns with the assignment requirements.

### H2 In-memory Database
Chosen for simplicity and ease of deployment. No external database setup required. Data resets on each restart which is acceptable for this use case.

### Layered Architecture
Strict separation of concerns: Controller → Service → Repository. DTOs are used to decouple the API contract from internal entities, making the code more maintainable.

### BigDecimal for Money
All monetary values use `BigDecimal` instead of `double` to avoid floating-point precision issues common in financial applications.

### Data Isolation
Every query filters by the authenticated user, ensuring complete data segregation between accounts.

---

## Running Tests

```bash
mvn test
```

---

## Deployment

The application is containerized using Docker and deployed on Render.

**Dockerfile:**
```dockerfile
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Author

Developed as part of the Syfe Backend Intern Assignment.
