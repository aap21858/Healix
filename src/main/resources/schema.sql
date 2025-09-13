-- Users table (linked to a specific clinic)
CREATE TABLE staff_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email_id VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255),
    role VARCHAR(50) NOT NULL, -- e.g., ADMIN, DOCTOR, STAFF
    full_name VARCHAR(255),
    contact_number VARCHAR(13) UNIQUE,
    status VARCHAR(15),
    token VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    token_created_at TIMESTAMP
);

-- Default admin user for testing (password: admin123)
INSERT INTO staff_details (email_id, password, role, full_name, contact_number, status)
VALUES
('aap@gmail.com',
'$2a$10$n98HtAx/KlW3U07Q4vD2ueZNupPsPduJLB/8mc0rlVF/Q3SooFxwq',
'ADMIN', 'Aap Shaikh', '9821284739', 'ACTIVE');

INSERT INTO staff_details (email_id, password, role, full_name, contact_number, status)
VALUES
('arman@gmail.com',
'$2a$10$n98HtAx/KlW3U07Q4vD2ueZNupPsPduJLB/8mc0rlVF/Q3SooFxwq',
'DOCTOR', 'Arman Shaikh', '9821482504', 'ACTIVE');

INSERT INTO staff_details (email_id, password, role, full_name, contact_number, status)
VALUES
('aliya@gmail.com',
'$2a$10$n98HtAx/KlW3U07Q4vD2ueZNupPsPduJLB/8mc0rlVF/Q3SooFxwq',
'RECEPTIONIST', 'Aliya Shaikh', '9894384807', 'ACTIVE');