-- ============================================================================
-- REPO 3 SPECIFIC TABLES SCHEMA (Used ONLY by repo3-payment-service)
-- Tables: payment_transactions, payment_invoices
-- Database: shared_app_db
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Repo 3 Specific Table 1: payment_transactions
-- Payment gateway transactions; references common 'app_users(id)' and Repo 2 'customer_orders(id)'
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    transaction_reference VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES app_users(id)
);

-- ----------------------------------------------------------------------------
-- Repo 3 Specific Table 2: payment_invoices
-- Invoices and tax breakdowns; references 'payment_transactions(id)'
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payment_invoices (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL UNIQUE,
    invoice_number VARCHAR(100) NOT NULL UNIQUE,
    tax_amount NUMERIC(12, 2) NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL,
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_invoice_payment FOREIGN KEY (payment_id) REFERENCES payment_transactions(id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------------------
-- Sample Seed Data for Repo 3 Specific Tables
-- ----------------------------------------------------------------------------
INSERT INTO payment_transactions (id, order_id, user_id, amount, payment_method, payment_status, transaction_reference)
VALUES (1, 1, 1, 1249.98, 'CREDIT_CARD', 'SUCCESS', 'TXN-ABC-001')
ON CONFLICT (id) DO NOTHING;

INSERT INTO payment_invoices (id, payment_id, invoice_number, tax_amount, total_amount)
VALUES (1, 1, 'INV-2026-001', 99.99, 1349.97)
ON CONFLICT (id) DO NOTHING;
