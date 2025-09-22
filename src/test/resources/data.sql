-- Test Data for H2 Database
-- This file will be automatically executed after schema.sql

-- Insert test users
INSERT INTO users (id, username, email, password, first_name, last_name, role, created_at, updated_at) VALUES
(1, 'testmanager', 'manager@test.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Test', 'Manager', 'STORE_MANAGER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'testoperator', 'operator@test.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Test', 'Operator', 'STORE_OPERATOR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test stores
INSERT INTO stores (id, name, location, contact_number, email, created_at, updated_at) VALUES
(1, 'Test Store 1', 'Test Location 1', '1234567890', 'store1@test.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Test Store 2', 'Test Location 2', '0987654321', 'store2@test.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test products
INSERT INTO products (id, name, description, category, sku, unit_price, max_storage_qty, min_storage_qty, created_at, updated_at) VALUES
(1, 'Test Product 1', 'Test Description 1', 'Category A', 'SKU001', 10.00, 1000, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Test Product 2', 'Test Description 2', 'Category B', 'SKU002', 20.00, 500, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Test Product 3', 'Test Description 3', 'Category C', 'SKU003', 30.00, 200, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test inventory
INSERT INTO inventory (id, store_id, product_id, current_stock, created_at, updated_at) VALUES
(1, 1, 1, 50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 2, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 2, 1, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 2, 3, 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Sales transactions are created by tests dynamically

-- Insert test reorder recommendations
INSERT INTO reorder_recommendations (id, store_id, product_id, current_stock, average_daily_sales, seasonality_factor, lead_time, safety_stock, reorder_point, recommended_qty, is_processed, created_at, updated_at) VALUES
(1, 1, 1, 45, 2.5, 1.2, 7, 10, 15, 50, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 2, 25, 1.8, 1.0, 5, 5, 12, 30, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
