# Inventory Management System


Basic smart inventory management system for 7-Eleven pilot stores. Focus on tracking stock, recording sales, and
running core algorithms:
- 📦 Smart Reorder Suggestions
- 📊
ABC Product Classification

User Roles
- **Store Operator**: View inventory, record sales
- **Store Manager**: All above + view reorder suggestions & ABC analysis
- **API Client**: Push sales, get inventory data

## Quick setup and build

1. Configure database in `src/main/resources/application.properties`.
2. Build backend (required before running): `mvn clean install`.
3. Install frontend deps (first time): `cd frontend/inventory-ui && npm install`.

## How to Start Application

### Start Backend
```bash
cd /path/to/project
# build once or after code changes
mvn clean install
# start app
mvn spring-boot:run
```
Backend runs on: http://localhost:8080

### Start Frontend
```bash
cd frontend/inventory-ui
# install deps first time
npm install
# start dev server
npm start
```
Frontend runs on: http://localhost:3000

## Test Users

| Username | Password | Role |
|----------|----------|------|
| admin | password123 | STORE_MANAGER |
| manager1 | password123 | STORE_MANAGER |
| operator1 | password123 | STORE_OPERATOR |
| operator2 | password123 | STORE_OPERATOR |
| api_client1 | password123 | API_CLIENT |
| pos_system | password123 | API_CLIENT |

## Random Data

When application starts, it automatically creates:
- 3 stores (Downtown, Mall, Airport)
- 30 products (drinks, snacks, food, electronics, etc.)
- 3 months of sales history
- Initial stock for all products

## Main APIs

### Login
- POST /api/auth/login

### Sales
- POST /api/sales/transaction - Record new sale
- GET /api/sales/store/{storeId} - Get sales data

### Inventory
- GET /api/inventory/{storeId} - Get inventory for store
- PUT /api/inventory/{inventoryId} - Update stock

### Smart Features
- GET /api/algorithms/reorder-recommendations/{storeId} - Get reorder suggestions
- GET /api/algorithms/abc-analysis/{storeId} - Get ABC analysis

## Common Errors

### Authentication Errors
- 401 Unauthorized - Need to login first
- 403 Forbidden - Wrong user role

### Data Errors
- 404 Not Found - Store/Product doesn't exist
- 400 Bad Request - Invalid input data

### Database Errors
- 500 Internal Server Error - Database connection issues

## API Documentation

View all APIs: http://localhost:8080/swagger-ui.html

## Requirements

- Java 17
- MySQL database
- Node.js (for frontend)

## Backend (Spring Boot 3.2, Java 17)

- Framework: Spring Boot 3.2.x
- Java: 17
- Modules: Web, Security (JWT), Data JPA, Validation, Actuator, OpenAPI (Swagger)
- Build: Maven (`mvn spring-boot:run`)

## Frontend (React)

- Stack: React + react-scripts, Axios, Bootstrap
- Dev server: `npm start` (proxy to backend at `http://localhost:8080`)
- Auth: Stores JWT in localStorage; sends `Authorization: Bearer <token>`

## application.properties (simple)

```properties
# App
server.port=8080
spring.application.name=Fidenz

# DB (MySQL)
spring.datasource.url=jdbc:mysql://localhost:3306/seven_eleven_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD
spring.jpa.hibernate.ddl-auto=create
spring.jpa.show-sql=true

# JWT
jwt.secret=change_this_to_secure_64_char_min_secret
jwt.expiration=86400000

# Swagger
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```
