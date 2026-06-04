-- WebFlux Reactive API Database Schema
-- R2DBC compatible schema for H2 database

-- Drop tables if they exist (for clean restart)
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS customers;

-- Customers table
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    address VARCHAR(200),
    city VARCHAR(50),
    state VARCHAR(50),
    zip_code VARCHAR(10),
    country VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Orders table
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- Order Items table
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Payments table
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    transaction_id VARCHAR(100),
    payment_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_order_number ON orders(order_number);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);

-- Insert sample data for testing
INSERT INTO customers (first_name, last_name, email, phone, address, city, state, zip_code, country) VALUES
('John', 'Doe', 'john.doe@example.com', '+1234567890', '123 Main St', 'Springfield', 'IL', '62701', 'USA'),
('Jane', 'Smith', 'jane.smith@example.com', '+1234567891', '456 Oak Ave', 'Chicago', 'IL', '60601', 'USA'),
('Bob', 'Johnson', 'bob.johnson@example.com', '+1234567892', '789 Pine Rd', 'Naperville', 'IL', '60540', 'USA');

INSERT INTO orders (order_number, customer_id, status, total_amount, notes) VALUES
('ORD-20240604-000001', 1, 'PENDING', 299.99, 'First order'),
('ORD-20240604-000002', 1, 'CONFIRMED', 149.99, 'Second order'),
('ORD-20240604-000003', 2, 'SHIPPED', 599.99, 'Express shipping'),
('ORD-20240604-000004', 3, 'DELIVERED', 99.99, 'Standard shipping');

INSERT INTO order_items (order_id, product_name, quantity, unit_price, total_price) VALUES
(1, 'Laptop', 1, 299.99, 299.99),
(2, 'Mouse', 2, 24.99, 49.99),
(2, 'Keyboard', 1, 99.99, 99.99),
(3, 'Monitor', 1, 599.99, 599.99),
(4, 'USB Cable', 2, 9.99, 19.99),
(4, 'HDMI Cable', 1, 79.99, 79.99);

INSERT INTO payments (order_id, payment_method, amount, status, transaction_id, payment_date) VALUES
(1, 'CREDIT_CARD', 299.99, 'PENDING', 'TXN-001', CURRENT_TIMESTAMP),
(2, 'CREDIT_CARD', 149.99, 'COMPLETED', 'TXN-002', CURRENT_TIMESTAMP),
(3, 'PAYPAL', 599.99, 'COMPLETED', 'TXN-003', CURRENT_TIMESTAMP),
(4, 'DEBIT_CARD', 99.99, 'COMPLETED', 'TXN-004', CURRENT_TIMESTAMP);

-- Made with Bob
