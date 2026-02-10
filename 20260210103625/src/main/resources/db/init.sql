-- H2 Database Initialization Script
-- This script creates the initial database schema

-- Enable PostgreSQL compatibility mode
SET MODE PostgreSQL;

-- Create transactions table if not exists (JPA will create it, but keeping for reference)
-- CREATE TABLE IF NOT EXISTS transactions (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     transaction_id VARCHAR(100) NOT NULL UNIQUE,
--     account_number VARCHAR(100) NOT NULL,
--     amount DECIMAL(19,2) NOT NULL,
--     currency VARCHAR(50) NOT NULL,
--     transaction_type VARCHAR(50),
--     merchant_category VARCHAR(100),
--     location VARCHAR(100),
--     country_code VARCHAR(3),
--     risk_score DECIMAL(19,4),
--     status VARCHAR(20),
--     description VARCHAR(100),
--     processed_at TIMESTAMP,
--     created_at TIMESTAMP NOT NULL,
--     created_by VARCHAR(100),
--     updated_at TIMESTAMP,
--     updated_by VARCHAR(100)
-- );

-- Create rules table if not exists (JPA will create it, but keeping for reference)
-- CREATE TABLE IF NOT EXISTS rules (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     rule_name VARCHAR(100) NOT NULL UNIQUE,
--     rule_content TEXT NOT NULL,
--     rule_category VARCHAR(50) NOT NULL,
--     priority INTEGER,
--     active BOOLEAN,
--     description VARCHAR(500),
--     created_at TIMESTAMP NOT NULL,
--     created_by VARCHAR(100),
--     updated_at TIMESTAMP,
--     updated_by VARCHAR(100)
-- );

-- Create processing_logs table if not exists (JPA will create it, but keeping for reference)
-- CREATE TABLE IF NOT EXISTS processing_logs (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     operation_name VARCHAR(255) NOT NULL,
--     status VARCHAR(50) NOT NULL,
--     records_processed BIGINT,
--     execution_time_ms BIGINT,
--     error_message TEXT,
--     details TEXT,
--     start_time TIMESTAMP NOT NULL,
--     end_time TIMESTAMP,
--     created_at TIMESTAMP NOT NULL
-- );

-- Create transaction_labels table if not exists (JPA will create it, but keeping for reference)
-- CREATE TABLE IF NOT EXISTS transaction_labels (
--     transaction_id BIGINT NOT NULL,
--     label VARCHAR(255) NOT NULL,
--     PRIMARY KEY (transaction_id, label),
--     FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE
-- );

-- Indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_transaction_id ON transactions(transaction_id);
CREATE INDEX IF NOT EXISTS idx_account_number ON transactions(account_number);
CREATE INDEX IF NOT EXISTS idx_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_processed_at ON transactions(processed_at);
CREATE INDEX IF NOT EXISTS idx_created_at ON transactions(created_at);

CREATE INDEX IF NOT EXISTS idx_rule_name ON rules(rule_name);
CREATE INDEX IF NOT EXISTS idx_rule_category ON rules(rule_category);
CREATE INDEX IF NOT EXISTS idx_rule_active ON rules(active);

CREATE INDEX IF NOT EXISTS idx_operation_name ON processing_logs(operation_name);
CREATE INDEX IF NOT EXISTS idx_status ON processing_logs(status);
CREATE INDEX IF NOT EXISTS idx_start_time ON processing_logs(start_time);
