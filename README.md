# 💰 Personal Finance Manager
A **Spring Boot 3.x REST API** for managing personal finances — track income, monitor expenses, set savings goals, and generate detailed reports.
---
## 🛠 Tech Stack
| Component  | Technology                      |
|------------|---------------------------------|
| Language   | Java 17                         |
| Framework  | Spring Boot 3.2                 |
| Security   | Spring Security (Session-based) |
| Database   | H2 In-Memory                    |
| Build Tool | Maven                           |
| Testing    | JUnit 5, Mockito                |
---
## ⚙️ Setup & Run
### Prerequisites
- Java 17+
- Maven 3.8+
### Run Locally
```bash
git clone https://github.com/pavan26r/Transaction_Managment-.git
cd Transaction_Managment-
mvn spring-boot:run
```

- **API Base URL:** `http://localhost:8080`
- **H2 Console:** `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:financedb`
  - User: `sa` | Password: *(empty)*

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

## 🏗 Architecture

```
Controller → Service → Repository → Database
```

| Layer      | Responsibility                                                        |
|------------|-----------------------------------------------------------------------|
| Controller | Handles HTTP requests, validates input, delegates to service layer    |
| Service    | Business logic (Auth, Transaction, Category, Goals, Reports)          |
| Repository | Spring Data JPA interfaces for database operations                    |
| DTOs       | Request/Response objects kept separate from entity classes            |
| Exception  | Global `@ControllerAdvice` with proper HTTP status codes              |

---

## 📡 API Documentation

**Base URL:** `/api`

---

### 🔐 1. Authentication

#### Register
```http
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
**Response 201:** `{ "message": "User registered successfully", "userId": 1 }`

#### Login
```http
POST /api/auth/login
```
```json
{ "username": "user@example.com", "password": "password123" }
```
**Response 200:** `{ "message": "Login successful" }` + sets session cookie

#### Logout
```http
POST /api/auth/logout
```
**Response 200:** `{ "message": "Logout successful" }`

---

### 💸 2. Transactions

> ⚠️ All endpoints require authentication.

#### Create Transaction
```http
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

#### Get Transactions (with filters)
```http
GET /api/transactions?startDate=2024-01-01&endDate=2024-01-31&category=Salary
```

#### Update Transaction
```http
PUT /api/transactions/{id}
```
```json
{ "amount": 60000.00, "description": "Updated Salary" }
```

#### Delete Transaction
```http
DELETE /api/transactions/{id}
```

---

### 🏷 3. Categories

#### Get All Categories
```http
GET /api/categories
```
Returns default categories: `Salary, Food, Rent, Transportation, Entertainment, Healthcare, Utilities` — plus user's custom categories.

#### Create Custom Category
```http
POST /api/categories
```
```json
{ "name": "FreelanceWork", "type": "INCOME" }
```

#### Delete Custom Category
```http
DELETE /api/categories/{name}
```
*(Default categories cannot be deleted)*

---

### 🎯 4. Savings Goals

#### Create Goal
```http
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

#### Get Goals
```http
GET /api/goals
GET /api/goals/{id}
```

#### Update Goal
```http
PUT /api/goals/{id}
```
```json
{ "targetAmount": 6000.00, "targetDate": "2026-06-01" }
```

#### Delete Goal
```http
DELETE /api/goals/{id}
```

---

### 📊 5. Reports

#### Monthly Report
```http
GET /api/reports/monthly/{year}/{month}
```
**Example:** `GET /api/reports/monthly/2024/1`

#### Yearly Report
```http
GET /api/reports/yearly/{year}
```
**Example:** `GET /api/reports/yearly/2024`

---

## ❌ Error Response Codes

| Code | Meaning                                              |
|------|------------------------------------------------------|
| 400  | Validation error / bad input                         |
| 401  | Not authenticated                                    |
| 403  | Accessing another user's data / deleting default category |
| 404  | Resource not found                                   |
| 409  | Duplicate username or category name                  |

---

## 🧠 Design Decisions

1. **Session-based Auth** — Uses `HttpSession` with Spring Security context storage. Simple and cookie-friendly.
2. **H2 In-Memory DB** — Easy to run without any external setup. Swap to PostgreSQL by updating `application.properties`.
3. **Default Categories as Enum** — Predefined categories stored as a Java enum — no DB rows needed, always available, and cannot be deleted.
4. **Category Validation** — Both create and update operations validate that the referenced category exists (default or user-created).
5. **Savings Goal Progress** — Dynamically calculated as `(total income - total expenses)` since the goal's `startDate`.
6. **Data Isolation** — All queries filter by `user.id` from the authenticated session — users can never access each other's data.

---

## 🚀 Deploy on Render
 (https://transaction-managment-ykcg.onrender.com/api)
## 📁 Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/finance/
│   │       ├── controller/    # REST Controllers
│   │       ├── service/       # Business Logic
│   │       ├── repository/    # JPA Repositories
│   │       ├── model/         # Entity Classes
│   │       ├── dto/           # Request/Response DTOs
│   │       └── exception/     # Global Exception Handler
│   └── resources/
│       └── application.properties
└── test/                      # JUnit 5 Tests
```

---

## 👨‍💻 Author

**Pavan** — [github.com/pavan26r](https://github.com/pavan26r)
