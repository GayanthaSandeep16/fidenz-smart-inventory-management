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

## How to Start Application

### Start Backend
```bash
cd /path/to/project
mvn spring-boot:run
```
Backend runs on: http://localhost:8080

### Start Frontend
```bash
cd frontend/inventory-ui
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

- Java 17 or higher
- MySQL database
- Node.js (for frontend)
