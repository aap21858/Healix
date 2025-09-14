-- Users table (linked to a specific clinic)
CREATE TABLE Staff (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email_id VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255),
    role VARCHAR(50), -- e.g., ADMIN, DOCTOR, STAFF
    full_name VARCHAR(255),
    contact_number VARCHAR(13) UNIQUE,
    status VARCHAR(15),
    token VARCHAR(255),
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,
    token_created_at TIMESTAMP
);

-- StaffRoles junction table
CREATE TABLE staff_roles (
    staff_id BIGINT NOT NULL,
    role VARCHAR(255) NOT NULL,
    CONSTRAINT fk_staff_roles_staff
        FOREIGN KEY (staff_id)
        REFERENCES Staff(id)
        ON DELETE CASCADE
);

-- Default admin user for testing (password: admin123)
INSERT INTO Staff (email_id, password, full_name, contact_number, status, created_by)
VALUES
('aap@gmail.com',
'$2a$10$n98HtAx/KlW3U07Q4vD2ueZNupPsPduJLB/8mc0rlVF/Q3SooFxwq', 'Aap Shaikh', '9821284739', 'ACTIVE', 1);

INSERT INTO Staff (email_id, password, full_name, contact_number, status, created_by)
VALUES
('arman@gmail.com',
'$2a$10$n98HtAx/KlW3U07Q4vD2ueZNupPsPduJLB/8mc0rlVF/Q3SooFxwq', 'Arman Shaikh', '9821482504', 'ACTIVE', 1);

INSERT INTO Staff (email_id, password, full_name, contact_number, status, created_by)
VALUES
('aliya@gmail.com',
'$2a$10$n98HtAx/KlW3U07Q4vD2ueZNupPsPduJLB/8mc0rlVF/Q3SooFxwq', 'Aliya Shaikh', '9894384807', 'ACTIVE', 1);


INSERT INTO staff_roles (staff_id, role) VALUES('1', 'ADMIN');
INSERT INTO staff_roles (staff_id, role) VALUES('1', 'DOCTOR');

