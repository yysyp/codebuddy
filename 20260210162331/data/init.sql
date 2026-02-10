-- H2 Database Initialization Script for Transaction Data
-- This script creates the transactions table and inserts sample data

-- Drop table if exists
DROP TABLE IF EXISTS transactions;

-- Create transactions table
CREATE TABLE transactions (
    transaction_id VARCHAR(100) PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,
    source_account VARCHAR(50) NOT NULL,
    destination_account VARCHAR(50) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    location_country VARCHAR(2),
    merchant_category VARCHAR(50),
    ip_address VARCHAR(45),
    device_id VARCHAR(50),
    status VARCHAR(20) DEFAULT 'PENDING',
    risk_score INT DEFAULT 0
);

-- Create indexes for better query performance
CREATE INDEX idx_customer_id ON transactions(customer_id);
CREATE INDEX idx_timestamp ON transactions(timestamp);
CREATE INDEX idx_transaction_type ON transactions(transaction_type);
CREATE INDEX idx_amount ON transactions(amount);

-- Insert sample transaction data
INSERT INTO transactions (transaction_id, customer_id, source_account, destination_account, amount, currency, transaction_type, timestamp, location_country, merchant_category, ip_address, device_id, status, risk_score) VALUES
('TXN-001', 'CUST-001', 'ACC-001', 'ACC-002', 15000.00, 'USD', 'TRANSFER', CURRENT_TIMESTAMP - INTERVAL '1' HOUR, 'US', 'RETAIL', '192.168.1.1', 'DEV-001', 'PENDING', 10),
('TXN-002', 'CUST-002', 'ACC-003', 'ACC-004', 2500.50, 'USD', 'PAYMENT', CURRENT_TIMESTAMP - INTERVAL '2' HOURS, 'UK', 'FOOD', '192.168.1.2', 'DEV-002', 'PENDING', 15),
('TXN-003', 'CUST-003', 'ACC-005', 'ACC-006', 75000.00, 'USD', 'TRANSFER', CURRENT_TIMESTAMP - INTERVAL '3' HOURS, 'DE', 'SERVICES', '192.168.1.3', 'DEV-003', 'PENDING', 20),
('TXN-004', 'CUST-004', 'ACC-007', 'ACC-008', 500.00, 'USD', 'WITHDRAWAL', CURRENT_TIMESTAMP - INTERVAL '4' HOURS, 'BR', 'TRAVEL', '192.168.1.4', 'DEV-004', 'PENDING', 25),
('TXN-005', 'CUST-005', 'ACC-009', 'ACC-010', 3200.75, 'USD', 'PAYMENT', CURRENT_TIMESTAMP - INTERVAL '5' HOURS, 'US', 'HEALTH', '192.168.1.5', 'DEV-005', 'PENDING', 30),
('TXN-006', 'CUST-006', 'ACC-011', 'ACC-012', 1000.00, 'USD', 'TRANSFER', CURRENT_TIMESTAMP - INTERVAL '6' HOURS, 'JP', 'ENTERTAINMENT', '192.168.1.6', 'DEV-006', 'PENDING', 35),
('TXN-007', 'CUST-007', 'ACC-013', 'ACC-014', 100000.00, 'USD', 'TRANSFER', CURRENT_TIMESTAMP - INTERVAL '7' HOURS, 'CN', 'RETAIL', '192.168.1.7', 'DEV-007', 'PENDING', 40),
('TXN-008', 'CUST-008', 'ACC-015', 'ACC-016', 85.00, 'USD', 'PAYMENT', CURRENT_TIMESTAMP - INTERVAL '8' HOURS, 'AU', 'FOOD', '192.168.1.8', 'DEV-008', 'PENDING', 5),
('TXN-009', 'CUST-009', 'ACC-017', 'ACC-018', 55000.00, 'USD', 'TRANSFER', CURRENT_TIMESTAMP - INTERVAL '9' HOURS, 'FR', 'TRAVEL', '192.168.1.9', 'DEV-009', 'PENDING', 45),
('TXN-010', 'CUST-010', 'ACC-019', 'ACC-020', 12500.00, 'USD', 'WITHDRAWAL', CURRENT_TIMESTAMP - INTERVAL '10' HOURS, 'IN', 'RETAIL', '192.168.1.10', 'DEV-010', 'PENDING', 50),
('TXN-011', 'CUST-011', 'ACC-021', 'ACC-022', 100.00, 'USD', 'DEPOSIT', CURRENT_TIMESTAMP - INTERVAL '11' HOURS, 'CA', 'SERVICES', '192.168.1.11', 'DEV-011', 'PENDING', 10),
('TXN-012', 'CUST-012', 'ACC-023', 'ACC-024', 2000.00, 'USD', 'TRANSFER', CURRENT_TIMESTAMP - INTERVAL '12' HOURS, 'US', 'RETAIL', '192.168.1.12', 'DEV-012', 'PENDING', 15),
('TXN-013', 'CUST-013', 'ACC-025', 'ACC-026', 750.25, 'USD', 'PAYMENT', CURRENT_TIMESTAMP - INTERVAL '13' HOURS, 'UK', 'FOOD', '192.168.1.13', 'DEV-013', 'PENDING', 20),
('TXN-014', 'CUST-014', 'ACC-027', 'ACC-028', 60000.00, 'USD', 'TRANSFER', CURRENT_TIMESTAMP - INTERVAL '14' HOURS, 'DE', 'TRAVEL', '192.168.1.14', 'DEV-014', 'PENDING', 60),
('TXN-015', 'CUST-015', 'ACC-029', 'ACC-030', 50.00, 'USD', 'WITHDRAWAL', CURRENT_TIMESTAMP - INTERVAL '15' HOURS, 'BR', 'HEALTH', '192.168.1.15', 'DEV-015', 'PENDING', 25),
('TXN-016', 'CUST-016', 'ACC-031', 'ACC-032', 3000.00, 'USD', 'PAYMENT', CURRENT_TIMESTAMP - INTERVAL '16' HOURS, 'US', 'ENTERTAINMENT', '192.168.1.16', 'DEV-016', 'PENDING', 30),
('TXN-017', 'CUST-017', 'ACC-033', 'ACC-034', 150000.00, 'USD', 'TRANSFER', CURRENT_TIMESTAMP - INTERVAL '17' HOURS, 'JP', 'RETAIL', '192.168.1.17', 'DEV-017', 'PENDING', 70),
('TXN-018', 'CUST-018', 'ACC-035', 'ACC-036', 200.00, 'USD', 'DEPOSIT', CURRENT_TIMESTAMP - INTERVAL '18' HOURS, 'CN', 'FOOD', '192.168.1.18', 'DEV-018', 'PENDING', 10),
('TXN-019', 'CUST-019', 'ACC-037', 'ACC-038', 4500.00, 'USD', 'TRANSFER', CURRENT_TIMESTAMP - INTERVAL '19' HOURS, 'AU', 'SERVICES', '192.168.1.19', 'DEV-019', 'PENDING', 35),
('TXN-020', 'CUST-020', 'ACC-039', 'ACC-040', 99.99, 'USD', 'PAYMENT', CURRENT_TIMESTAMP - INTERVAL '20' HOURS, 'FR', 'TRAVEL', '192.168.1.20', 'DEV-020', 'PENDING', 15);

-- Verify data insertion
SELECT COUNT(*) as total_transactions FROM transactions;
