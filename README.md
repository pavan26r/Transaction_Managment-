# Personal Finance Manager

A Spring Boot 3.x REST API for managing personal finances — track income, expenses, savings goals, and generate reports.

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security (Session-based) |
| Database | H2 In-Memory |
| Build | Maven |
| Testing | JUnit 5, Mockito |

---

## Setup & Run

### Prerequisites
- Java 17+
- Maven 3.8+

### Run Locally

```bash
git clone <repo-url>
cd personal-finance-manager
mvn spring-boot:run
```

API will be available at: `http://localhost:8080`

H2 Console: `http://localhost:8080/h2-console`  
(JDBC URL: `jdbc:h2:mem:financedb`, User: `sa`, Password: empty)

### Run Tests

```bash
mvn test
```

### Build JAR

```bash
mvn clean package
java -jar target/personal-finance-manager-1.0.0.jar
```

---

## Architecture

```
Controller → Service → Repository → Database
```

- **Controller**: Handles HTTP requests, validates input, delegates to service
- **Service**: Business logic (AuthService, TransactionService, CategoryService, SavingsGoalService, ReportService)
- **Repository**: Spring Data JPA interfaces
- **DTOs**: Request/Response objects separate from entities
- **Exception Handling**: Global `@ControllerAdvice` with proper HTTP status codes

---

## API Documentation

### Base URL: `/api`

---

### 1. Authentication

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
Response `201`: `{ "message": "User registered successfully", "userId": 1 }`

#### Login
```
POST /api/auth/login
```
```json
{ "username": "user@example.com", "password": "password123" }
```
Response `200`: `{ "message": "Login successful" }` + sets session cookie

#### Logout
```
POST /api/auth/logout
```
Response `200`: `{ "message": "Logout successful" }`

---

### 2. Transactions

> All endpoints require authentication.

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
GET /api/transactions?startDate=2024-01-01&endDate=2024-01-31&category=Salary
```

#### Update Transaction
```
PUT /api/transactions/{id}
```
```json
{ "amount": 60000.00, "description": "Updated Salary" }
```

#### Delete Transaction
```
DELETE /api/transactions/{id}
```

---

### 3. Categories

#### Get All Categories
```
GET /api/categories
```
Returns default categories (Salary, Food, Rent, Transportation, Entertainment, Healthcare, Utilities) + user's custom categories.

#### Create Custom Category
```
POST /api/categories
```
```json
{ "name": "FreelanceWork", "type": "INCOME" }
```

#### Delete Custom Category
```
DELETE /api/categories/{name}
```

---

### 4. Savings Goals

#### Create Goal
```
POST /api/goals
```
```json
{
  "goalName": "Emergency Fund",
  "targetAmount": 5000.00,
  "targetDate": "2026-01-01",
  "startDate": "2025-01-01"
}
```

#### Get All Goals / Get Goal
```
GET /api/goals
GET /api/goals/{id}
```

#### Update Goal
```
PUT /api/goals/{id}
```
```json
{ "targetAmount": 6000.00, "targetDate": "2026-06-01" }
```

#### Delete Goal
```
DELETE /api/goals/{id}
```

---

### 5. Reports

#### Monthly Report
```
GET /api/reports/monthly/{year}/{month}
```
Example: `GET /api/reports/monthly/2024/1`

#### Yearly Report
```
GET /api/reports/yearly/{year}
```
Example: `GET /api/reports/yearly/2024`

---

## Error Responses

| Code | Meaning |
|------|---------|
| 400 | Validation error / bad input |
| 401 | Not authenticated |
| 403 | Accessing other user's data / deleting default category |
| 404 | Resource not found |
| 409 | Duplicate username / category name |

---

## Design Decisions

1. **Session-based auth**: Used `HttpSession` with Spring Security context storage — simple, stateless-friendly with cookies.
2. **H2 in-memory DB**: Easy to run without external setup; can swap for PostgreSQL by changing `application.properties`.
3. **Default categories as enum**: Predefined categories stored as a Java enum — no DB rows needed, always available, can't be deleted.
4. **Category validation on transaction**: Both create and update validate that the referenced category exists (default or user's custom).
5. **Savings goal progress**: Calculated dynamically as `(total income - total expenses) since startDate`. Deleted transactions naturally exclude themselves.
6. **Data isolation**: All queries filter by `user.id` from the authenticated session — users never see each other's data.

---

## Deploy on Render

1. Push to GitHub
2. Create a new **Web Service** on [Render](https://render.com)
3. Connect your GitHub repo
4. Set:
   - **Build Command**: `mvn clean package -DskipTests`
   - **Start Command**: `java -jar target/personal-finance-manager-1.0.0.jar`
5. Deploy!

Then run the test script:
```bash
bash financial_manager_tests.sh https://your-app.onrender.com/api
```
