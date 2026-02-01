# Secure Expense Manager API

A production-ready Spring Boot backend for secure expense management with JWT authentication.

## Technology Stack

- **Java 21**
- **Spring Boot 3.2.1**
- **Spring Data JPA (Hibernate)**
- **Spring Security with JWT**
- **PostgreSQL Database**
- **Lombok**
- **Springdoc OpenAPI (Swagger UI)**
- **JJWT 0.11.5** (JWT Library)
- **Maven**

## Project Structure

```
expense-manager-api/
├── src/main/java/com/expensemanager/
│   ├── api/
│   │   ├── controller/
│   │   │   ├── AuthController.java          # Authentication endpoints
│   │   │   ├── UserController.java          # User management endpoints
│   │   │   ├── ExpenseController.java       # Expense management endpoints
│   │   │   ├── AnalyticsController.java     # Analytics endpoints
│   │   │   └── BaseController.java          # Base controller for all controllers
│   │   └── exception/
│   │       ├── GlobalExceptionHandler.java  # Exception handling
│   │       ├── ErrorResponse.java           # Error response DTO
│   │       ├── ResourceNotFoundException.java
│   │       └── ValidationException.java
│   ├── application/
│   │   ├── dto/
│   │   │   ├── AuthRequestDto.java
│   │   │   ├── AuthResponseDto.java
│   │   │   ├── UserRequestDto.java
│   │   │   ├── UserResponseDto.java
│   │   │   ├── ExpenseRequestDto.java
│   │   │   ├── ExpenseResponseDto.java
│   │   │   └── CategorySummaryDto.java
│   │   ├── mapper/
│   │   │   └── EntityMapper.java            # Entity to DTO mapping
│   │   └── service/
│   │       ├── AuthService.java
│   │       ├── UserService.java
│   │       ├── ExpenseService.java
│   │       ├── AnalyticsService.java
│   │       └── impl/
│   │           ├── AuthServiceImpl.java
│   │           ├── UserServiceImpl.java
│   │           ├── ExpenseServiceImpl.java
│   │           └── AnalyticsServiceImpl.java
│   ├── domain/
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   └── Expense.java
│   │   └── enums/
│   │       ├── ExpenseCategory.java
│   │       └── Role.java
│   ├── infrastructure/
│   │   ├── config/
│   │   │   ├── CorsConfig.java              # CORS configuration
│   │   │   ├── OpenApiConfig.java           # Swagger/OpenAPI configuration
│   │   │   └── SecurityConfig.java          # Spring Security configuration
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   └── ExpenseRepository.java
│   │   └── security/
│   │       ├── JwtTokenProvider.java        # JWT token generation and validation
│   │       ├── JwtAuthenticationFilter.java # JWT authentication filter
│   │       └── JwtAuthDetails.java          # JWT authentication details
│   └── ExpenseManagerApplication.java       # Application entry point
├── src/main/resources/
│   ├── application.yml                      # Main application configuration
│   └── application-test.yml                 # Test profile configuration
├── pom.xml                                   # Maven dependencies
└── README.md
```

## Architecture Layers

1. **API Layer** (`api.controller`, `api.exception`)
   - REST endpoint definitions with comprehensive Swagger documentation
   - HTTP request/response handling
   - Global exception handling with standardized error responses
   - Input validation using Jakarta Validation

2. **Application Layer** (`application.service`, `application.dto`)
   - Service interfaces and implementations for business logic
   - Data Transfer Objects (DTOs) for API contracts
   - Entity-to-DTO mapping utilities
   - Business logic implementation

3. **Domain Layer** (`domain.entity`, `domain.enums`)
   - JPA entities with proper relationships and indexes
   - Domain enums (Role, ExpenseCategory)
   - Data model definitions with validation

4. **Infrastructure Layer** (`infrastructure.repository`, `infrastructure.config`, `infrastructure.security`)
   - Repository interfaces using Spring Data JPA
   - Configuration classes (Security, OpenAPI, CORS)
   - JWT token provider and authentication filters
   - Database configuration and connection pooling

## Database Schema

### Users Table
```
id (Long) - Primary Key
username (String) - Unique, max 50 chars
email (String) - Unique, max 100 chars
password (String) - Encrypted with BCrypt
role (Enum: ADMIN, USER, VIEWER)
is_active (Boolean) - Default: true
created_at (LocalDateTime) - Auto-set on creation
updated_at (LocalDateTime) - Auto-updated on modification
```

### Expenses Table
```
id (Long) - Primary Key
user_id (Long) - Foreign Key to Users table
amount (BigDecimal) - Precision 19, Scale 2
category (Enum: FOOD, TRANSPORTATION, UTILITIES, ENTERTAINMENT, HEALTHCARE, SHOPPING, EDUCATION, TRAVEL, OTHER)
description (String) - Max 500 chars
expense_date (LocalDate)
created_at (LocalDateTime) - Auto-set on creation
updated_at (LocalDateTime) - Auto-updated on modification
is_deleted (Boolean) - Soft delete flag, default false
deleted_at (LocalDateTime) - Timestamp when soft-deleted (null if not deleted)
```

**Important:** Soft Delete Implementation
- Deleting an expense sets `is_deleted = true` and `deleted_at = timestamp`
- Physical deletion never occurs - all historical data is retained
- All queries automatically exclude deleted records via `@Where(clause = "is_deleted = false")`
- Deleted expenses are invisible to API consumers

## API Endpoints

### Authentication Endpoints
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - Login user and get JWT token

### User Management Endpoints
- `GET /api/v1/users` - Get all users with pagination
- `GET /api/v1/users/{id}` - Get user by ID
- `POST /api/v1/users` - Create new user
- `PUT /api/v1/users/{id}` - Update user information
- `DELETE /api/v1/users/{id}` - Delete user

### Expense Management Endpoints
- `GET /api/v1/expenses` - Get all expenses for a user (with pagination)
- `GET /api/v1/expenses/{id}` - Get expense by ID
- `GET /api/v1/expenses/category/{category}` - Get expenses filtered by category
- `GET /api/v1/expenses/range` - Get expenses within a date range
- `POST /api/v1/expenses` - Create new expense
- `PUT /api/v1/expenses/{id}` - Update expense
- `DELETE /api/v1/expenses/{id}` - Delete expense

### Analytics Endpoints
- `GET /api/v1/analytics/category-summary` - Get expense summary by category (Requires JWT authentication)
- `GET /api/v1/analytics/monthly-summary` - Get monthly expense totals (Requires JWT authentication)
  - Optional filters: `?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD`

## Configuration

### Database Configuration
Edit `application.yml` to configure your PostgreSQL database:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/expense_manager_db
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
```

### JWT Configuration
Update the JWT secret in `application.yml`:

```yaml
app:
  jwt:
    secret: your-super-secret-key-change-in-production
    expiration: 86400000 # 24 hours in milliseconds
```

### CORS Configuration
CORS is configured to allow requests from:
- `localhost:3000` (React dev server)
- `localhost:4200` (Angular dev server)
- `localhost:8080` (Local API testing)

Edit `CorsConfig.java` to modify allowed origins.

### Application Configuration
```yaml
server:
  port: 8080
  servlet:
    context-path: /api/v1

logging:
  level:
    root: INFO
    com.expensemanager: DEBUG
    org.springframework.security: DEBUG
```

## Building and Running

### Prerequisites
- Java 21+
- Maven 3.6+
- PostgreSQL 12+

### Build the project
```bash
mvn clean install
```

### Run the application
```bash
# Set environment variables
export DB_URL=jdbc:postgresql://localhost:5432/expense_manager_db
export DB_USERNAME=postgres
export DB_PASSWORD=postgres

mvn spring-boot:run
```

The API will be available at `http://localhost:8080/api/v1`

### Swagger UI Documentation
Access the interactive API documentation at:
```
http://localhost:8080/api/v1/swagger-ui.html
```

## Security

### Ownership Enforcement

**CRITICAL SECURITY FEATURE**: Strict expense ownership enforcement is implemented at the service layer.

#### How It Works:
1. **User ID Extraction**: User ID is extracted from JWT token in `BaseController.getAuthenticatedUserId()`
2. **No Request Parameters**: User ID is **NEVER** accepted from request parameters (removed from all endpoints)
3. **Service Layer Verification**: All service methods verify ownership via `verifyExpenseOwnership(expense, userId)`
4. **Unauthorized Access Response**: HTTP 403 Forbidden with `AccessDeniedException`

#### Example: Getting an Expense
```java
@GetMapping("/{expenseId}")
public ResponseEntity<ExpenseResponseDto> getExpenseById(
    @PathVariable Long expenseId,
    Authentication authentication) {
    // Step 1: Extract userId from JWT token (NOT from request parameter)
    Long userId = getAuthenticatedUserId(authentication);
    
    // Step 2: Pass both expenseId and userId to service
    ExpenseResponseDto expense = expenseService.getExpenseById(expenseId, userId);
    
    // Step 3: If expense doesn't belong to user, service throws AccessDeniedException → 403
    return ResponseEntity.ok(expense);
}
```

#### Service Layer Security Check:
```java
private void verifyExpenseOwnership(Expense expense, Long userId) {
    if (!expense.getUser().getId().equals(userId)) {
        log.warn("SECURITY: User {} attempted to access expense {} which belongs to user {}",
            userId, expense.getId(), expense.getUser().getId());
        throw new AccessDeniedException("You do not have permission to access this expense");
    }
}
```

**Security Guarantee**: A user can ONLY access, modify, or delete their own expenses. Attempting to access another user's expense always returns HTTP 403 Forbidden.

### Role-Based Access Control (RBAC)

All endpoints are now protected with role-based authorization using Spring Security method-level security.

#### JWT Token Structure:
```json
{
  "sub": "username",
  "userId": 123,
  "role": "USER",
  "iat": 1704067200,
  "exp": 1704153600
}
```

#### Role Definitions:

**USER** - Can manage their own expenses
- ✅ Create expenses
- ✅ Read own expenses
- ✅ Update own expenses
- ✅ Delete own expenses (soft delete)
- ✅ View analytics (own expenses only)
- ❌ Create/update/delete other users' expenses
- ❌ Access admin endpoints

**VIEWER** - Read-only access to own data
- ✅ View own expenses
- ✅ View analytics (read-only, own expenses only)
- ❌ Create, update, or delete expenses
- ❌ Access admin endpoints

**ADMIN** - System administration
- ✅ Manage users (list, activate/deactivate, delete)
- ✅ View system-wide analytics (optional)
- ✅ All USER permissions
- ❌ User account creation limited to registration endpoints

#### Authentication Flow:

1. **Registration**:
   - Default role = `USER`
   - No role escalation via request
   - Returns JWT with role claim
   - Auto-login behavior

2. **Login**:
   - Available WITHOUT JWT (public endpoint)
   - Issues new JWT regardless of token expiry
   - Role from database (server-side only)

#### Endpoint Authorization Examples:

**Expense Creation** (USER role only):
```java
@PostMapping
@PreAuthorize("hasRole('USER')")
public ResponseEntity<ExpenseResponseDto> createExpense(...) { }
```

**Expense Reading** (USER or VIEWER):
```java
@GetMapping("/{expenseId}")
@PreAuthorize("hasAnyRole('USER', 'VIEWER')")
public ResponseEntity<ExpenseResponseDto> getExpenseById(...) { }
```

**Analytics Access** (USER or VIEWER):
```java
@GetMapping("/category-summary")
@PreAuthorize("hasAnyRole('USER', 'VIEWER')")
public ResponseEntity<CategorySummaryDto> getCategorySummary(...) { }
```

#### Testing Role Authorization:

```bash
# Retrieve token (USER role)
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john_doe","password":"securePassword123"}'

# Response includes JWT with role claim
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "USER",
  "expiresIn": 86400000
}

# Access USER-protected endpoint with JWT
curl -X POST http://localhost:8080/api/v1/expenses \
  -H "Authorization: Bearer <jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{"amount":25.50,"category":"FOOD","description":"Lunch"}'

# Response: 201 Created (USER role allows creation)

# Create VIEWER token, attempt expense creation (fails)
# Response: 403 Forbidden - role authorization violation
```

### Soft Delete & Data Retention

All expense deletions are **soft deletes**:
- Records are marked as deleted but never physically removed
- Deleted expenses are automatically excluded from all queries
- Full audit trail is maintained for compliance
- Analytics automatically exclude deleted records

**Implementation Details:**
- `isDeleted` boolean field (default: false)
- `deletedAt` timestamp field (set when expense is deleted)
- All repository queries filter out deleted records via WHERE clause
- No physical deletion ever occurs
- Soft-deleted data remains in database for audit and legal purposes

**Example - Delete Lifecycle:**
```
Before Delete:
  id=5, amount=100.00, isDeleted=false, deletedAt=null

After DELETE /api/v1/expenses/5:
  id=5, amount=100.00, isDeleted=true, deletedAt=2025-12-28T10:35:20

Result:
  - GET /api/v1/expenses/5 returns 403 Forbidden (invisible to user)
  - Analytics exclude from calculations
  - Record preserved in database for compliance
```

### Other Security Features

- **JWT Authentication**: All endpoints (except auth) require a valid JWT token in the `Authorization: Bearer <token>` header
- **Password Encryption**: Passwords are encrypted using BCrypt
- **Input Validation**: All request DTOs are validated using Jakarta Validation annotations
- **CORS Protection**: Configured for specific origins only
- **Exception Handling**: Standardized error responses with proper HTTP status codes

## Authentication Flow

1. User registers via `/auth/register` with username, email, and password
2. User logs in via `/auth/login` with username and password
3. Server returns JWT token in `AuthResponseDto.accessToken`
4. User includes token in all subsequent requests: `Authorization: Bearer <token>`
5. Server validates token using `JwtTokenProvider`
6. User ID and role are extracted from JWT for authorization

## Request/Response Examples

### Register User
**Request:**
```json
POST /api/v1/auth/register
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePassword123"
}
```

**Response (201 Created):**
```json
{
  "userId": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "role": "USER",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000
}
```

### Create Expense

**Request:**
```json
POST /api/v1/expenses
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

{
  "amount": 25.50,
  "category": "FOOD",
  "description": "Lunch at restaurant",
  "expenseDate": "2025-12-28"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "amount": 25.50,
  "category": "FOOD",
  "description": "Lunch at restaurant",
  "expenseDate": "2025-12-28",
  "createdAt": "2025-12-28T10:30:45",
  "updatedAt": "2025-12-28T10:30:45"
}
```

**Note**: User ID is automatically extracted from JWT token. No `userId` parameter needed.

### Get Expense (With Ownership Verification)

**Request:**
```
GET /api/v1/expenses/1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK) - If owner:**
```json
{
  "id": 1,
  "amount": 25.50,
  "category": "FOOD",
  "description": "Lunch at restaurant",
  "expenseDate": "2025-12-28",
  "createdAt": "2025-12-28T10:30:45",
  "updatedAt": "2025-12-28T10:30:45"
}
```

**Response (403 Forbidden) - If not owner:**
```json
{
  "timestamp": "2025-12-28T10:35:20.123Z",
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have permission to access this expense"
}
```

### Update Expense (With Ownership Verification)

**Request:**
```json
PUT /api/v1/expenses/1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

{
  "amount": 30.00,
  "category": "FOOD",
  "description": "Updated lunch expense",
  "expenseDate": "2025-12-28"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "amount": 30.00,
  "category": "FOOD",
  "description": "Updated lunch expense",
  "expenseDate": "2025-12-28",
  "createdAt": "2025-12-28T10:30:45",
  "updatedAt": "2025-12-28T10:35:20"
}
```

### Delete Expense (Soft Delete with Ownership Verification)

**Request:**
```
DELETE /api/v1/expenses/1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (204 No Content)** - Expense marked as deleted (not physically removed)

**After Deletion:**
- Record in database has `is_deleted = true` and `deleted_at = timestamp`
- GET requests for this expense return HTTP 403 or 404 (invisible to user)
- Analytics automatically exclude from calculations
- Full audit trail maintained for compliance

### List User Expenses

**Request:**
```
GET /api/v1/expenses?page=0&size=10&sort=expenseDate,desc
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "amount": 25.50,
      "category": "FOOD",
      "description": "Lunch at restaurant",
      "expenseDate": "2025-12-28",
      "createdAt": "2025-12-28T10:30:45",
      "updatedAt": "2025-12-28T10:30:45"
    }
  ],
  "pageable": {
    "sort": {
      "empty": false,
      "unsorted": false,
      "sorted": true
    },
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 10,
    "paged": true,
    "unpaged": false
  },
  "last": true,
  "totalPages": 1,
  "totalElements": 1,
  "first": true,
  "size": 10,
  "number": 0,
  "sort": {
    "empty": false,
    "unsorted": false,
    "sorted": true
  },
  "numberOfElements": 1,
  "empty": false
}
```

**Note**: Results automatically scoped to authenticated user only.

### Get Monthly Expense Summary

**Request (All months):**
```
GET /api/v1/analytics/monthly-summary
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "2025-10": 1250.75,
  "2025-11": 980.50,
  "2025-12": 1430.00
}
```

**Request (With date range filter):**
```
GET /api/v1/analytics/monthly-summary?startDate=2025-10-01&endDate=2025-12-31
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "2025-10": 1250.75,
  "2025-11": 980.50,
  "2025-12": 1430.00
}
```

**Note**: 
- Monthly summary excludes soft-deleted expenses automatically
- Date range is inclusive (both start and end dates included)
- Months are formatted as YYYY-MM and sorted in descending order
- Results scoped to authenticated user only

### Get Category Summary
**Request:**
```
GET /api/v1/analytics/category-summary
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "categoryTotals": {
    "FOOD": 150.75,
    "TRANSPORTATION": 85.00,
    "UTILITIES": 120.00
  },
  "grandTotal": 355.75
}
```

## Expense Categories

- **FOOD** - Food & Dining
- **TRANSPORTATION** - Transportation
- **UTILITIES** - Utilities
- **ENTERTAINMENT** - Entertainment
- **HEALTHCARE** - Healthcare
- **SHOPPING** - Shopping
- **EDUCATION** - Education
- **TRAVEL** - Travel
- **OTHER** - Other

## User Roles

- **ADMIN** - Full system access, can manage all users and expenses
- **USER** - Can manage own expenses and view own profile
- **VIEWER** - Read-only access to own data

## Status

✅ **Fully Implemented and Production Ready**

- All endpoints implemented with complete business logic
- JWT authentication and authorization configured
- Database schema with proper indexes and relationships
- Comprehensive error handling and validation
- OpenAPI/Swagger documentation
- Logging and security best practices

## Author

- **Kamesh Rajaram**
