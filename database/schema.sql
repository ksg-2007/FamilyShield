CREATE DATABASE IF NOT EXISTS familyshield CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE familyshield;

CREATE TABLE IF NOT EXISTS scam_reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    risk_level VARCHAR(10) NOT NULL,
    risk_score INT NOT NULL,
    category VARCHAR(80) NOT NULL,
    sender VARCHAR(255) NOT NULL DEFAULT '',
    message_text TEXT NOT NULL,
    warning_signs TEXT,
    recommendations TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
