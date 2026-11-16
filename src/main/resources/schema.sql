
-- =====================================================
-- PROJECT: UNIKART
-- DATABASE: unikart_db
-- PURPOSE: Complete database structure
-- =====================================================

CREATE DATABASE IF NOT EXISTS unikart_db;
USE unikart_db;


-- =====================================================
-- 1. USERS TABLE
-- Stores customers, sellers and admin accounts
-- =====================================================

CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(15),
    role ENUM('CUSTOMER', 'SELLER', 'ADMIN') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- =====================================================
-- 2. SELLER APPLICATIONS TABLE
-- Stores seller registration applications
-- =====================================================

CREATE TABLE IF NOT EXISTS seller_applications (
    application_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    college_name VARCHAR(150) NOT NULL,
    reason TEXT,
    status ENUM('PENDING', 'APPROVED', 'REJECTED')
        DEFAULT 'PENDING',
    rejection_reason TEXT,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id)
);


-- =====================================================
-- 3. CATEGORIES TABLE
-- Stores product categories
-- =====================================================

CREATE TABLE IF NOT EXISTS categories (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);


-- =====================================================
-- 4. PRODUCTS TABLE
-- Stores products sold by Unikart and sellers
-- =====================================================

CREATE TABLE IF NOT EXISTS products (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    seller_id INT,
    category_id INT NOT NULL,
    product_name VARCHAR(150) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    status ENUM('PENDING', 'APPROVED', 'REJECTED')
        DEFAULT 'PENDING',
    rejection_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (seller_id) REFERENCES users(user_id),
    FOREIGN KEY (category_id) REFERENCES categories(category_id),

    CHECK (price >= 0),
    CHECK (stock >= 0)
);


-- =====================================================
-- 5. CARTS TABLE
-- Stores one shopping cart per customer
-- =====================================================

CREATE TABLE IF NOT EXISTS carts (
    cart_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id)
);


-- =====================================================
-- 6. CART ITEMS TABLE
-- Stores products added to shopping carts
-- =====================================================

CREATE TABLE IF NOT EXISTS cart_items (
    cart_item_id INT PRIMARY KEY AUTO_INCREMENT,
    cart_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (cart_id) REFERENCES carts(cart_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id),

    CHECK (quantity > 0),
    UNIQUE (cart_id, product_id)
);


-- =====================================================
-- 7. ORDERS TABLE
-- Stores the main customer order
-- =====================================================

CREATE TABLE IF NOT EXISTS orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    order_status ENUM(
        'PLACED',
        'PROCESSING',
        'SHIPPED',
        'DELIVERED',
        'CANCELLED'
    ) DEFAULT 'PLACED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (customer_id) REFERENCES users(user_id),

    CHECK (total_amount >= 0)
);


-- =====================================================
-- 8. SELLER ORDER GROUPS TABLE
-- Separates an order according to individual sellers
-- =====================================================

CREATE TABLE IF NOT EXISTS seller_order_groups (
    group_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    seller_id INT,
    group_status ENUM(
        'PLACED',
        'PROCESSING',
        'SHIPPED',
        'DELIVERED',
        'CANCELLED'
    ) DEFAULT 'PLACED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (seller_id) REFERENCES users(user_id)
);


-- =====================================================
-- 9. ORDER ITEMS TABLE
-- Stores products purchased in each seller order group
-- =====================================================

CREATE TABLE IF NOT EXISTS order_items (
    order_item_id INT PRIMARY KEY AUTO_INCREMENT,
    group_id INT NOT NULL,
    product_id INT NOT NULL,
    product_name VARCHAR(150) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,

    FOREIGN KEY (group_id)
        REFERENCES seller_order_groups(group_id),

    FOREIGN KEY (product_id)
        REFERENCES products(product_id),

    CHECK (quantity > 0),
    CHECK (unit_price >= 0),
    CHECK (subtotal >= 0)
);


-- =====================================================
-- 10. PAYMENTS TABLE
-- Stores checkout payment information
-- =====================================================

CREATE TABLE IF NOT EXISTS payments (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    payment_status ENUM(
        'PENDING',
        'SUCCESS',
        'FAILED'
    ) DEFAULT 'PENDING',
    transaction_reference VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (order_id) REFERENCES orders(order_id),

    CHECK (amount >= 0)
);


-- =====================================================
-- INITIAL DATA: UNIKART PRODUCT CATEGORIES
-- =====================================================

INSERT IGNORE INTO categories (category_name, description)
VALUES
('Stationery', 'Notebooks, pens and other study supplies'),
('Electronics', 'Electronic gadgets and accessories'),
('Books', 'Academic and non-academic books'),
('Clothing', 'Clothing and wearable products'),
('Accessories', 'Everyday personal accessories'),
('Daily Essentials', 'Products for everyday needs');


-- =====================================================
-- END OF UNIKART DATABASE SCRIPT
-- =====================================================
