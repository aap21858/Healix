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

-- Patient Registration Tables
CREATE TABLE patients (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id VARCHAR(20) UNIQUE NOT NULL, -- e.g., SNG2025001

    -- Personal Information
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10) NOT NULL, -- MALE, FEMALE, OTHER
    blood_group VARCHAR(5), -- A_POSITIVE, B_NEGATIVE, etc.
    aadhar_number VARCHAR(12) UNIQUE,
    photo_url VARCHAR(500),

    -- Contact Information
    mobile_number VARCHAR(13) NOT NULL UNIQUE,
    email_id VARCHAR(100),
    preferred_contact_method VARCHAR(20), -- SMS, WHATSAPP, EMAIL, PHONE_CALL

    -- Address
    address_line1 VARCHAR(510),
    city VARCHAR(100) NOT NULL,
    district VARCHAR(100),
    state VARCHAR(50) DEFAULT 'Maharashtra',
    pin_code VARCHAR(6) NOT NULL,

    -- Audit fields
    status VARCHAR(15) DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,

    CONSTRAINT fk_patient_created_by FOREIGN KEY (created_by) REFERENCES Staff(id)
);

-- Emergency Contact Information
CREATE TABLE patient_emergency_contacts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id BIGINT NOT NULL,
    contact_person_name VARCHAR(100) NOT NULL,
    relationship VARCHAR(20) NOT NULL, -- SPOUSE, PARENT, CHILD, SIBLING, FRIEND, OTHER
    contact_number VARCHAR(13) NOT NULL,
    is_primary BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_emergency_contact_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);
-- ============================================
-- REFERENCE TABLES (Admin-manageable)
-- ============================================

-- Dropdown lookup table for generic reference data (replaces insurance_schemes)
CREATE TABLE dropdown_lookup (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(50) NOT NULL,
    code VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    active BOOLEAN DEFAULT TRUE,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT uq_dropdown_type_code UNIQUE (type, code)
);

-- Insurance Information (references dropdown_lookup instead of insurance_schemes)
CREATE TABLE patient_insurance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id BIGINT NOT NULL,

    has_insurance BOOLEAN NOT NULL DEFAULT FALSE,
    insurance_type VARCHAR(20), -- GOVERNMENT, PRIVATE, NONE
    scheme_id BIGINT, -- Foreign key to dropdown_lookup table (type = 'INSURANCE_SCHEME')
    policy_card_number VARCHAR(50),

    policy_holder_name VARCHAR(100),
    relationship_to_holder VARCHAR(20), -- SELF, SPOUSE, CHILD, PARENT
    policy_expiry_date DATE,

    insurance_card_front_url VARCHAR(500),
    insurance_card_back_url VARCHAR(500),
    pmjay_card_url VARCHAR(500),

    claim_amount_limit DECIMAL(10, 2),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_insurance_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_insurance_scheme FOREIGN KEY (scheme_id) REFERENCES dropdown_lookup(id)
);

-- Medical History
CREATE TABLE patient_medical_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id BIGINT NOT NULL,

    known_allergies TEXT, -- JSON array or comma-separated
    current_medications TEXT,
    past_surgeries TEXT,
    chronic_conditions TEXT,
    family_medical_history TEXT,
    disability TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_medical_history_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

-- ============================================
-- SEED DATA
-- ============================================

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
INSERT INTO staff_roles (staff_id, role) VALUES('2', 'DOCTOR');
INSERT INTO staff_roles (staff_id, role) VALUES('2', 'RECEPTIONIST');
INSERT INTO staff_roles (staff_id, role) VALUES('3', 'DOCTOR');

-- Dropdown lookup seed data for insurance schemes (type = 'INSURANCE_SCHEME')
INSERT INTO dropdown_lookup (type, code, description, display_order) VALUES
('INSURANCE_SCHEME', 'MJPJAY', 'Mahatma Jyotiba Phule Jan Arogya Yojana (MJPJAY)', 1),
('INSURANCE_SCHEME', 'PMJAY', 'Pradhan Mantri Jan Arogya Yojana (PM-JAY/Ayushman Bharat)', 2),
('INSURANCE_SCHEME', 'ESI', 'Employee State Insurance (ESI)', 3),
('INSURANCE_SCHEME', 'CGHS', 'Central Government Health Scheme (CGHS)', 4),
('INSURANCE_SCHEME', 'RGJAY', 'Rajiv Gandhi Jeevandayee Arogya Yojana (RGJAY)', 5),
('INSURANCE_SCHEME', 'OTHER', 'Other', 6),
('INSURANCE_PROVIDER', 'STAR_HEALTH', 'Star Health', 7),
('INSURANCE_PROVIDER', 'ICICI_LOMBARD', 'ICICI Lombard', 8),
('INSURANCE_PROVIDER', 'HDFC_ERGO', 'HDFC Ergo', 9),
('INSURANCE_PROVIDER', 'MAX_BUPA', 'Max Bupa', 10),
('INSURANCE_PROVIDER', 'CARE_HEALTH', 'Care Health', 11),
('INSURANCE_PROVIDER', 'OTHER', 'Other', 12);

-- Sample Patient Data
INSERT INTO patients (patient_id, first_name, last_name, date_of_birth, gender, blood_group,
                     mobile_number, email_id, city, pin_code, created_by) VALUES
('SNG2025001', 'Rahul', 'Patil', '1985-03-15', 'MALE', 'O_',
 '9876543210', 'rahul.patil@gmail.com', 'Sangli', '416416', 1),
('SNG2025002', 'Priya', 'Deshmukh', '1990-07-22', 'FEMALE', 'A_',
 '9876543211', 'priya.d@gmail.com', 'Sangli', '416416', 1);

-- Sample Emergency Contacts
INSERT INTO patient_emergency_contacts (patient_id, contact_person_name, relationship, contact_number) VALUES
(1, 'Sunita Patil', 'SPOUSE', '9876543220'),
(2, 'Vijay Deshmukh', 'SPOUSE', '9876543221');

-- Sample Insurance
INSERT INTO patient_insurance (patient_id, has_insurance, insurance_type, scheme_id, policy_card_number) VALUES
(1, TRUE, 'GOVERNMENT', 1, 'MJPJAY123456'),
(2, TRUE, 'GOVERNMENT', 2, 'PMJAY789012');

-- Sample Medical History
INSERT INTO patient_medical_history (patient_id, known_allergies, current_medications) VALUES
(1, 'Penicillin, Peanuts', 'Metformin 500mg'),
(2, 'None Known', 'None');

-- Create indexes for performance
CREATE INDEX idx_patient_mobile ON patients(mobile_number);
CREATE INDEX idx_patient_aadhar ON patients(aadhar_number);
CREATE INDEX idx_patient_id ON patients(patient_id);
CREATE INDEX idx_patient_status ON patients(status);
