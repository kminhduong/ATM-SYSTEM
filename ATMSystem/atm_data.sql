drop database testatm_db;
create database testatm_db;
USE testatm_db;

-- Bảng User
CREATE TABLE User (
    user_id VARCHAR(12) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    create_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Bảng Account
CREATE TABLE Account (
    account_number VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(12) NOT NULL,
    account_type ENUM('SAVINGS', 'CHECKING') NOT NULL,
    status ENUM('ACTIVE', 'CLOSED', 'FROZEN', 'BLOCKED', 'PENDING') NOT NULL,
    last_updated DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    username VARCHAR(50) NOT NULL,
    full_name VARCHAR(100),
    role VARCHAR(255) DEFAULT NULL,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE,
    INDEX idx_account_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Bảng Balance
CREATE TABLE Balance (
    account_number VARCHAR(50) PRIMARY KEY,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    last_updated DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_account_balance FOREIGN KEY (account_number) REFERENCES Account(account_number) ON DELETE CASCADE,
    CONSTRAINT chk_non_negative_balance CHECK (balance >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Bảng Credential
CREATE TABLE Credential (
    account_number VARCHAR(50) PRIMARY KEY,
    pin VARCHAR(255) NOT NULL,
    failed_attempts INT DEFAULT 0,
    lock_time DATETIME NULL,
    update_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_account_credential FOREIGN KEY (account_number) REFERENCES Account(account_number) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Bảng ATM
CREATE TABLE ATM (
    atm_id VARCHAR(50) PRIMARY KEY,
    cash_500 INT DEFAULT 0,
    cash_200 INT DEFAULT 0,
    cash_100 INT DEFAULT 0,
    cash_50 INT DEFAULT 0,
    total_amount DECIMAL(15,2) GENERATED ALWAYS AS ((cash_500*500) + (cash_200*200) + (cash_100*100) + (cash_50*50)) STORED,
    status ENUM('Active', 'OutOfService', 'Maintenance', 'LowCash') DEFAULT 'Active',
    last_updated DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_non_negative_cash CHECK (cash_500 >= 0 AND cash_200 >= 0 AND cash_100 >= 0 AND cash_50 >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Bảng Transaction
CREATE TABLE Transaction (
    transaction_id VARCHAR(50) PRIMARY KEY,
    atm_id VARCHAR(50),
    account_number VARCHAR(50) NOT NULL,
    type ENUM('Withdrawal', 'Deposit', 'Transfer', 'Withdrawal_OTP') NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_atm_transaction FOREIGN KEY (atm_id) REFERENCES ATM(atm_id) ON DELETE CASCADE,
    CONSTRAINT fk_account_transaction FOREIGN KEY (account_number) REFERENCES Account(account_number) ON DELETE CASCADE,
    CONSTRAINT chk_positive_amount CHECK (amount > 0),
    INDEX idx_transaction_account (account_number),
    INDEX idx_transaction_atm (atm_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO ATM (atm_id, cash_500, cash_200, cash_100, cash_50, status, last_updated)
VALUES ('ATM001', 50, 100, 200, 300, 'Active', NOW());
SELECT * FROM user;
SELECT * FROM account;
SELECT * FROM credential;
SELECT * FROM Transaction;
SELECT * FROM balance;
SELECT * FROM atm;
SHOW CREATE TABLE ATM;

