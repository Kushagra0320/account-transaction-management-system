# 🏦 Account Transaction Management System

> Enterprise-grade Banking Transaction Backend built with Java 17, Spring Boot, Oracle Database, Spring Security, JPA, AOP, and Design Patterns.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen)
![Oracle](https://img.shields.io/badge/Oracle-Database-red)
![Spring Security](https://img.shields.io/badge/Security-Spring%20Security-success)
![JPA](https://img.shields.io/badge/JPA-Hibernate-blue)
![Maven](https://img.shields.io/badge/Build-Maven-blueviolet)

---

## 📌 Overview

Account Transaction Management System is a secure and scalable banking backend application that supports:

- Deposit Funds
- Withdraw Funds
- Transfer Funds
- Account Lookup
- Transaction History

The project demonstrates enterprise backend engineering concepts including transaction management, optimistic locking, idempotency, authentication, audit logging, exception handling, and layered architecture.

---

## ✨ Key Features

### 🔒 Security

- Spring Security Integration
- Basic Authentication
- Session Management
- BCrypt Password Encryption
- Endpoint Authorization


### 💳 Banking Operations

- Deposit
- Withdraw
- Transfer
- Account Lookup
- Transaction History

### ⚡ Reliability

- ACID Transactions
- Automatic Rollbacks
- Optimistic Locking
- Duplicate Transaction Prevention
- Centralized Error Handling

### 📈 Observability

- AOP-Based Logging
- Execution Time Monitoring
- Exception Tracking
- Structured Logs

---

# 🏗️ Architecture

```text
Client
   │
   ▼
Spring Security
   │
   ▼
Controller Layer
   │
   ▼
AOP Logging Layer
   │
   ▼
Service Layer
   │
   ▼
Strategy Layer
   │
   ▼
Repository Layer
   │
   ▼
Oracle Database
```

---

## 📂 Project Structure

```text
src/main/java
│
├── controller
├── service
│   ├── impl
│   └── strategy
├── repository
├── entity
├── dto
├── security
├── config
├── exception
├── aop
└── util
```

---

# 🧩 Design Patterns & Enterprise Concepts

### Strategy Pattern

Transaction processing logic is separated into:

- DepositStrategy
- WithdrawStrategy
- TransferStrategy

Benefits:

- Extensible
- Testable
- Clean Separation of Concerns

---

### AOP (Aspect-Oriented Programming)

Automatically logs:

- Method Start
- Method End
- Execution Time
- Exceptions

without polluting business logic.

---

### Optimistic Locking

Implemented using:

```java
@Version
private Long version;
```

Prevents:

- Race Conditions
- Lost Updates
- Concurrent Balance Corruption

---

### Idempotency Pattern

Protects against duplicate transaction execution.

Example:

```text
User retries Deposit Request
        │
Same Idempotency Key
        │
Previous Response Returned
        │
No Duplicate Deposit
```

---

# 🔄 Banking Transaction Flow

```text
Client Request
      │
      ▼
Security Validation
      │
      ▼
Controller Validation
      │
      ▼
Service Layer
      │
      ▼
Transaction Strategy
      │
      ▼
Database Operations
      │
      ▼
Commit Transaction
      │
      ▼
Response
```

---

# 🗄️ Database Schema

## CUSTOMER

| Column | Type |
|----------|----------|
| id | NUMBER |
| first_name | VARCHAR2 |
| last_name | VARCHAR2 |
| email | VARCHAR2 |
| phone_number | VARCHAR2 |

---

## ACCOUNT

| Column | Type |
|----------|----------|
| id | NUMBER |
| account_number | VARCHAR2 |
| customer_id | NUMBER |
| balance | NUMBER(15,2) |
| status | VARCHAR2 |
| version | NUMBER |

---

## TRANSACTION

| Column | Type |
|----------|----------|
| id | NUMBER |
| transaction_ref | VARCHAR2 |
| account_id | NUMBER |
| type | VARCHAR2 |
| amount | NUMBER(15,2) |
| balance_before | NUMBER(15,2) |
| balance_after | NUMBER(15,2) |
| transaction_date | TIMESTAMP |

---

# 📡 REST API Endpoints

## Account APIs

| Method | Endpoint |
|----------|----------|
| GET | /api/accounts/{accountNumber} |

---

## Transaction APIs

| Method | Endpoint |
|----------|----------|
| POST | /api/transactions/deposit |
| POST | /api/transactions/withdraw |
| POST | /api/transactions/transfer |
| GET | /api/transactions/history/{accountNumber} |

---

# 📝 Sample Requests

## Deposit

```json
{
  "accountNumber": "ACC-001",
  "amount": 500,
  "description": "Salary Credit"
}
```

---

## Withdraw

```json
{
  "accountNumber": "ACC-001",
  "amount": 200,
  "description": "ATM Withdrawal"
}
```

---

## Transfer

```json
{
  "sourceAccountNumber": "ACC-001",
  "destinationAccountNumber": "ACC-002",
  "amount": 1000,
  "description": "Rent Payment"
}
```

---

# 🚨 Exception Handling

Implemented using:

```java
@RestControllerAdvice
```

Supported Exceptions:

| Exception | Status |
|------------|------------|
| AccountNotFoundException | 404 |
| AccountNotActiveException | 403 |
| InsufficientFundsException | 400 |
| IllegalArgumentException | 400 |
| OptimisticLockingFailureException | 409 |
| ValidationException | 400 |
| GenericException | 500 |

---

# 🧪 Testing




# 📚 Swagger Documentation

After startup:

```text
http://localhost:8080/swagger-ui.html
```

---

# 🎯 Enterprise Concepts Demonstrated

- Spring Boot REST APIs
- Spring Security
- Oracle Database Integration
- Spring Data JPA
- Transaction Management
- ACID Properties
- Optimistic Locking
- Idempotency
- Strategy Pattern
- AOP Logging
- Dependency Injection
- Exception Handling
- DTO Pattern
- Layered Architecture


---

# 👨‍💻 Author

**Kushagra Sharma**

B.Tech Computer Science

Java Backend Developer | Spring Boot | SQL | Banking Domain

---

