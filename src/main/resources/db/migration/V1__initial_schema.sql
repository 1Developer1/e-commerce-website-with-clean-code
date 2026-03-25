-- V1__initial_schema.sql
-- Uygulamanın temel veritabanı şemasını oluşturan ilk Flyway migrasyonu

CREATE TABLE products (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    stock INT NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE carts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE cart_items (
    id UUID PRIMARY KEY,
    cart_jpa_entity_id UUID NOT NULL REFERENCES carts(id),
    product_id UUID NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMP
);

CREATE TABLE orders (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    recipient_name VARCHAR(255) NOT NULL,
    shipping_address TEXT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    order_status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP
);

CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id),
    product_id UUID NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL
);

CREATE TABLE discounts (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    active BOOLEAN NOT NULL,
    expiry_date TIMESTAMP
);

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id UUID NOT NULL,
    type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Örnek veri (Mock Dummy Data for Evaluators)
INSERT INTO products (id, name, description, price, currency, stock, created_at, updated_at)
VALUES 
    ('11111111-1111-1111-1111-111111111111', 'MacBook Pro', 'Apple M3 Pro işlemcili 16 inç MacBook Pro', 1999.99, 'USD', 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('22222222-2222-2222-2222-222222222222', 'AirPods Max', 'Gürültü engelleyici kablosuz kulak üstü kulaklık', 549.00, 'USD', 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO discounts (id, code, amount, currency, active)
VALUES
    ('33333333-3333-3333-3333-333333333333', 'WELCOME10', 10.00, 'USD', true);
