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

    known_allergies TEXT, -- JSON array of strings
    current_medications TEXT, -- JSON array of strings
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
(1, '["Penicillin", "Peanuts"]', '["Metformin 500mg"]'),
(2, '[]', '[]');

-- Create indexes for performance
CREATE INDEX idx_patient_mobile ON patients(mobile_number);
CREATE INDEX idx_patient_aadhar ON patients(aadhar_number);
CREATE INDEX idx_patient_id ON patients(patient_id);
CREATE INDEX idx_patient_status ON patients(status);

-- ============================================
-- APPOINTMENT MANAGEMENT SYSTEM
-- ============================================

-- Appointments table (OPD & IPD)
CREATE TABLE appointments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    appointment_number VARCHAR(20) UNIQUE NOT NULL, -- Auto-generated: APT-YYYYMMDD-XXXXX

    -- Patient & Type
    patient_id BIGINT NOT NULL,
    appointment_type VARCHAR(10) NOT NULL DEFAULT 'OPD', -- OPD, IPD

    -- Scheduling Details
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    duration INT DEFAULT 30, -- In minutes

    -- Resource Allocation
    department_id BIGINT, -- References dropdown_lookup (type='DEPARTMENT')
    physician_id BIGINT NOT NULL,
    consultation_room VARCHAR(50),

    -- Priority & Status
    urgency_level VARCHAR(20) DEFAULT 'NORMAL', -- NORMAL, URGENT, EMERGENCY
    status VARCHAR(30) DEFAULT 'DRAFT', -- DRAFT, CONFIRMED, WAITING, IN_TRIAGE, IN_CONSULTATION, TO_INVOICE, COMPLETED, CANCELLED, NO_SHOW

    -- Clinical Info
    chief_complaint TEXT,
    notes TEXT,

    -- Referral (if applicable)
    referred_from_appointment_id BIGINT,
    referred_to_department_id BIGINT,
    referred_to_physician_id BIGINT,

    -- IPD Specific (nullable for OPD)
    bed_id BIGINT, -- For IPD appointments
    admission_date DATETIME,
    discharge_date DATETIME,

    -- Audit
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_appointment_physician FOREIGN KEY (physician_id) REFERENCES staff(id),
    CONSTRAINT fk_appointment_department FOREIGN KEY (department_id) REFERENCES dropdown_lookup(id),
    CONSTRAINT fk_appointment_referred_to_physician FOREIGN KEY (referred_to_physician_id) REFERENCES staff(id),
    CONSTRAINT fk_appointment_referred_to_department FOREIGN KEY (referred_to_department_id) REFERENCES dropdown_lookup(id)
);

-- Create indexes for appointments table
CREATE INDEX idx_appointment_date ON appointments(appointment_date);
CREATE INDEX idx_appointment_patient ON appointments(patient_id);
CREATE INDEX idx_appointment_physician ON appointments(physician_id);
CREATE INDEX idx_appointment_status ON appointments(status);
CREATE INDEX idx_appointment_number ON appointments(appointment_number);

-- Appointment Triage (Pre-consultation assessment)
CREATE TABLE appointment_triage (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    appointment_id BIGINT NOT NULL UNIQUE,

    -- Performed by
    recorded_by BIGINT NOT NULL, -- Staff ID (nurse/junior doctor)
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- General Information
    chief_complaints TEXT,
    history_present_illness TEXT,
    past_medical_history TEXT,
    family_history TEXT,
    allergies TEXT,
    current_medications TEXT,
    social_history TEXT,
    notes TEXT,

    CONSTRAINT fk_triage_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    CONSTRAINT fk_triage_recorded_by FOREIGN KEY (recorded_by) REFERENCES staff(id)
);

-- Appointment Vitals (Clinical measurements)
CREATE TABLE appointment_vitals (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    appointment_id BIGINT NOT NULL,

    -- Vitals
    weight DECIMAL(5,2), -- kg
    height DECIMAL(5,2), -- cm
    head_circumference DECIMAL(5,2), -- cm (for pediatric)
    temperature DECIMAL(4,2), -- Fahrenheit or Celsius
    temperature_unit VARCHAR(1) DEFAULT 'F', -- F or C
    heart_rate INT, -- bpm
    respiratory_rate INT, -- breaths per minute
    systolic_bp INT, -- mmHg
    diastolic_bp INT, -- mmHg
    spo2 DECIMAL(5,2), -- Oxygen saturation %
    random_blood_sugar DECIMAL(6,2), -- mg/dL
    bmi DECIMAL(4,2), -- Calculated
    bmi_status VARCHAR(20), -- Underweight, Normal, Overweight, Obese
    pain_level INT, -- 0-10 scale

    -- Symptoms (JSON array)
    symptoms JSON,

    -- Audit
    recorded_by BIGINT NOT NULL,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_vitals_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    CONSTRAINT fk_vitals_recorded_by FOREIGN KEY (recorded_by) REFERENCES staff(id)
);

-- Create index for appointment_vitals table
CREATE INDEX idx_vitals_appointment ON appointment_vitals(appointment_id);

-- Appointment Examination (Doctor's examination notes)
CREATE TABLE appointment_examination (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    appointment_id BIGINT NOT NULL UNIQUE,

    -- General Examination
    general_appearance TEXT,

    -- Systemic Examination
    cardiovascular_system TEXT,
    respiratory_system TEXT,
    gastrointestinal_system TEXT,
    central_nervous_system TEXT,
    musculoskeletal_system TEXT,

    -- Findings
    examination_findings TEXT,

    -- Diagnosis
    primary_diagnosis VARCHAR(255),
    primary_diagnosis_icd10 VARCHAR(10),
    differential_diagnosis TEXT,

    -- Treatment Plan
    treatment_plan TEXT,
    advice TEXT,
    follow_up_date DATE,
    follow_up_instructions TEXT,

    -- Doctor
    examined_by BIGINT NOT NULL,
    examined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_examination_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    CONSTRAINT fk_examination_examined_by FOREIGN KEY (examined_by) REFERENCES staff(id)
);

-- Prescriptions
CREATE TABLE prescriptions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    prescription_number VARCHAR(20) UNIQUE NOT NULL, -- RX-YYYYMMDD-XXXXX
    appointment_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    physician_id BIGINT NOT NULL,

    -- Details
    diagnosis VARCHAR(500),
    prescription_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'DRAFT', -- DRAFT, PRESCRIBED, DISPENSED

    -- Medication Group (configurable category)
    medication_group VARCHAR(100),

    -- General Instructions
    instructions TEXT,

    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    dispensed_at TIMESTAMP,
    dispensed_by BIGINT,

    CONSTRAINT fk_prescription_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    CONSTRAINT fk_prescription_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_prescription_physician FOREIGN KEY (physician_id) REFERENCES staff(id),
    CONSTRAINT fk_prescription_dispensed_by FOREIGN KEY (dispensed_by) REFERENCES staff(id)
);

-- Create indexes for prescriptions table
CREATE INDEX idx_prescription_patient ON prescriptions(patient_id);
CREATE INDEX idx_prescription_date ON prescriptions(prescription_date);
CREATE INDEX idx_prescription_number ON prescriptions(prescription_number);

-- Prescription Items (Individual medications)
CREATE TABLE prescription_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    prescription_id BIGINT NOT NULL,

    -- Medicine Details
    medicine_name VARCHAR(255) NOT NULL,
    generic_name VARCHAR(255),
    dosage VARCHAR(50), -- e.g., 500mg
    form VARCHAR(50), -- Tablet, Syrup, Injection, Capsule

    -- Dosing Instructions
    frequency VARCHAR(50), -- e.g., 1-0-1 (Morning-Afternoon-Night)
    duration INT, -- Number of days
    duration_unit VARCHAR(10) DEFAULT 'DAYS', -- DAYS, WEEKS, MONTHS
    route VARCHAR(50), -- Oral, IV, IM, Topical

    -- Instructions
    instructions TEXT, -- e.g., "After meals", "On empty stomach"

    -- Quantity
    quantity INT,

    CONSTRAINT fk_prescription_item_prescription FOREIGN KEY (prescription_id) REFERENCES prescriptions(id) ON DELETE CASCADE
);

-- Create index for prescription_items table
CREATE INDEX idx_prescription_item_prescription ON prescription_items(prescription_id);

-- Investigation Orders (Pathology & Radiology)
CREATE TABLE investigation_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_number VARCHAR(20) UNIQUE NOT NULL, -- INV-YYYYMMDD-XXXXX
    appointment_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    ordered_by BIGINT NOT NULL, -- Physician ID

    -- Order Details
    order_type VARCHAR(20) NOT NULL, -- PATHOLOGY, RADIOLOGY
    test_name VARCHAR(255) NOT NULL,
    test_code VARCHAR(50),

    -- Status
    status VARCHAR(30) DEFAULT 'ORDERED', -- ORDERED, SAMPLE_COLLECTED, IN_PROGRESS, COMPLETED, CANCELLED

    -- Clinical Info
    clinical_notes TEXT,
    urgency VARCHAR(20) DEFAULT 'ROUTINE', -- ROUTINE, URGENT, STAT

    -- Results
    result_value TEXT,
    result_unit VARCHAR(50),
    result_status VARCHAR(20), -- NORMAL, ABNORMAL, CRITICAL
    result_report_url VARCHAR(500), -- Link to uploaded report
    result_notes TEXT,

    -- Dates
    ordered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sample_collected_at TIMESTAMP,
    result_reported_at TIMESTAMP,
    reported_by BIGINT, -- Lab technician/Radiologist ID

    CONSTRAINT fk_investigation_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    CONSTRAINT fk_investigation_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_investigation_ordered_by FOREIGN KEY (ordered_by) REFERENCES staff(id),
    CONSTRAINT fk_investigation_reported_by FOREIGN KEY (reported_by) REFERENCES staff(id)
);

-- Create indexes for investigation_orders table
CREATE INDEX idx_investigation_patient ON investigation_orders(patient_id);
CREATE INDEX idx_investigation_status ON investigation_orders(status);
CREATE INDEX idx_investigation_order_number ON investigation_orders(order_number);

-- Appointment Services (Billable items consumed)
CREATE TABLE appointment_services (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    appointment_id BIGINT NOT NULL,

    -- Service Details
    service_type VARCHAR(50), -- CONSULTATION, PROCEDURE, INVESTIGATION, MEDICATION, BED_CHARGES
    service_name VARCHAR(255),
    service_code VARCHAR(50),

    -- Quantity & Pricing
    quantity INT DEFAULT 1,
    unit_price DECIMAL(10,2),
    total_price DECIMAL(10,2),
    discount DECIMAL(10,2) DEFAULT 0,

    -- Billing
    is_billable BOOLEAN DEFAULT TRUE,
    is_billed BOOLEAN DEFAULT FALSE,

    -- Audit
    added_by BIGINT,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_service_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    CONSTRAINT fk_service_added_by FOREIGN KEY (added_by) REFERENCES staff(id)
);

-- Create indexes for appointment_services table
CREATE INDEX idx_service_appointment ON appointment_services(appointment_id);
CREATE INDEX idx_service_billed ON appointment_services(is_billed);

-- IPD Admission Logs (Audit trail for OPD to IPD conversion)
CREATE TABLE ipd_admission_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    appointment_id BIGINT NOT NULL,
    previous_type VARCHAR(10),
    previous_status VARCHAR(30),
    admitted_by BIGINT,
    bed_id BIGINT,
    admission_notes TEXT,
    estimated_discharge_date DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_admission_log_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    CONSTRAINT fk_admission_log_admitted_by FOREIGN KEY (admitted_by) REFERENCES staff(id)
);

-- Create index for ipd_admission_logs table
CREATE INDEX idx_admission_log_appointment ON ipd_admission_logs(appointment_id);

-- ============================================
-- SEED DATA FOR APPOINTMENTS
-- ============================================

-- Add department dropdown data
INSERT INTO dropdown_lookup (type, code, description, display_order) VALUES
('DEPARTMENT', 'GENERAL_MEDICINE', 'General Medicine', 1),
('DEPARTMENT', 'PEDIATRICS', 'Pediatrics', 2),
('DEPARTMENT', 'ORTHOPEDICS', 'Orthopedics', 3),
('DEPARTMENT', 'CARDIOLOGY', 'Cardiology', 4),
('DEPARTMENT', 'GYNECOLOGY', 'Gynecology', 5),
('DEPARTMENT', 'DERMATOLOGY', 'Dermatology', 6),
('DEPARTMENT', 'ENT', 'ENT (Ear, Nose, Throat)', 7),
('DEPARTMENT', 'OPHTHALMOLOGY', 'Ophthalmology', 8),
('DEPARTMENT', 'PSYCHIATRY', 'Psychiatry', 9),
('DEPARTMENT', 'DENTISTRY', 'Dentistry', 10);

-- Add consultation room data
INSERT INTO dropdown_lookup (type, code, description, display_order) VALUES
('CONSULTATION_ROOM', 'ROOM_101', 'Consultation Room 101', 1),
('CONSULTATION_ROOM', 'ROOM_102', 'Consultation Room 102', 2),
('CONSULTATION_ROOM', 'ROOM_103', 'Consultation Room 103', 3),
('CONSULTATION_ROOM', 'ROOM_201', 'Consultation Room 201', 4),
('CONSULTATION_ROOM', 'ROOM_202', 'Consultation Room 202', 5);

-- Sample Appointments
INSERT INTO appointments (appointment_number, patient_id, appointment_type, appointment_date, appointment_time,
                         duration, department_id, physician_id, consultation_room, urgency_level, status,
                         chief_complaint, notes, created_by) VALUES
('APT-20251111-00001', 1, 'OPD', '2025-11-11', '10:00:00', 30,
 (SELECT id FROM dropdown_lookup WHERE type='DEPARTMENT' AND code='GENERAL_MEDICINE' LIMIT 1),
 1, 'Room 101', 'NORMAL', 'CONFIRMED',
 'Fever and cough for 3 days', 'Patient has history of asthma', 1),

('APT-20251111-00002', 2, 'OPD', '2025-11-11', '10:30:00', 30,
 (SELECT id FROM dropdown_lookup WHERE type='DEPARTMENT' AND code='GENERAL_MEDICINE' LIMIT 1),
 2, 'Room 102', 'NORMAL', 'WAITING',
 'Headache and nausea', 'Patient arrived early', 2),

('APT-20251111-00003', 1, 'OPD', '2025-11-11', '11:00:00', 45,
 (SELECT id FROM dropdown_lookup WHERE type='DEPARTMENT' AND code='CARDIOLOGY' LIMIT 1),
 1, 'Room 201', 'URGENT', 'IN_CONSULTATION',
 'Chest pain and breathlessness', 'Requires ECG and echo', 1);

-- Sample Triage Data
INSERT INTO appointment_triage (appointment_id, recorded_by, chief_complaints, history_present_illness,
                                past_medical_history, allergies, current_medications, notes) VALUES
(2, 3, 'Headache and nausea for 2 days',
 'Headache started 2 days ago, progressively worsening. Nausea since this morning.',
 'No significant past medical history',
 'No known allergies',
 'None',
 'Patient appears comfortable but reports severe headache');

-- Sample Vitals Data
INSERT INTO appointment_vitals (appointment_id, weight, height, temperature, temperature_unit,
                                heart_rate, respiratory_rate, systolic_bp, diastolic_bp, spo2,
                                random_blood_sugar, bmi, bmi_status, pain_level, symptoms, recorded_by) VALUES
(2, 65.5, 162, 98.6, 'F', 78, 16, 120, 80, 98, 95, 24.95, 'Normal', 7,
 '["Headache", "Nausea"]', 3),

(3, 75.0, 175, 99.2, 'F', 95, 20, 140, 90, 96, 110, 24.49, 'Normal', 5,
 '["Chest pain", "Breathlessness", "Sweating"]', 3);

-- Sample Examination Data
INSERT INTO appointment_examination (appointment_id, general_appearance, cardiovascular_system,
                                    respiratory_system, examination_findings, primary_diagnosis,
                                    primary_diagnosis_icd10, differential_diagnosis, treatment_plan,
                                    advice, follow_up_date, examined_by) VALUES
(3, 'Patient is anxious, appears distressed',
 'S1 S2 heard, tachycardia present, no murmurs',
 'Bilateral air entry equal, no added sounds',
 'Mild tachycardia, elevated BP, chest examination normal',
 'Acute Coronary Syndrome - suspected',
 'I24.9',
 'Angina, GERD, Musculoskeletal pain',
 'ECG and Cardiac enzymes ordered, Monitor vitals, IV access',
 'Admit for observation, cardiology consult',
 '2025-11-12',
 1);

-- Sample Prescriptions
INSERT INTO prescriptions (prescription_number, appointment_id, patient_id, physician_id,
                          diagnosis, prescription_date, status, medication_group, instructions) VALUES
('RX-20251111-00001', 1, 1, 1,
 'Upper Respiratory Tract Infection', '2025-11-11', 'PRESCRIBED',
 'Antibiotics & Antipyretics',
 'Complete the full course of antibiotics. Take medications after meals.');

-- Sample Prescription Items
INSERT INTO prescription_items (prescription_id, medicine_name, generic_name, dosage, form,
                               frequency, duration, duration_unit, route, instructions, quantity) VALUES
(1, 'Amoxicillin', 'Amoxicillin', '500mg', 'Tablet', '1-0-1', 5, 'DAYS', 'Oral', 'After meals', 10),
(1, 'Paracetamol', 'Paracetamol', '650mg', 'Tablet', '1-1-1', 3, 'DAYS', 'Oral', 'After meals when fever', 9),
(1, 'Cetirizine', 'Cetirizine', '10mg', 'Tablet', '0-0-1', 5, 'DAYS', 'Oral', 'Before sleep', 5);

-- Sample Investigation Orders
INSERT INTO investigation_orders (order_number, appointment_id, patient_id, ordered_by,
                                 order_type, test_name, test_code, clinical_notes, urgency, status) VALUES
('INV-20251111-00001', 3, 1, 1,
 'PATHOLOGY', 'Complete Blood Count', 'CBC',
 'Fever investigation - rule out infection', 'ROUTINE', 'ORDERED'),

('INV-20251111-00002', 3, 1, 1,
 'RADIOLOGY', 'Chest X-Ray', 'CXR',
 'Chest pain - rule out cardiac/pulmonary pathology', 'URGENT', 'ORDERED'),

('INV-20251111-00003', 3, 1, 1,
 'PATHOLOGY', 'Cardiac Enzymes (Troponin I)', 'TROP-I',
 'Suspected ACS - cardiac marker', 'STAT', 'SAMPLE_COLLECTED');

-- Sample Appointment Services (for billing)
INSERT INTO appointment_services (appointment_id, service_type, service_name, service_code,
                                 quantity, unit_price, total_price, is_billable, added_by) VALUES
(1, 'CONSULTATION', 'General Medicine Consultation', 'CONS-GM', 1, 500.00, 500.00, TRUE, 1),
(3, 'CONSULTATION', 'Cardiology Consultation', 'CONS-CARD', 1, 1000.00, 1000.00, TRUE, 1),
(3, 'INVESTIGATION', 'ECG', 'ECG-12LEAD', 1, 200.00, 200.00, TRUE, 1),
(3, 'INVESTIGATION', 'Complete Blood Count', 'CBC', 1, 300.00, 300.00, TRUE, 1),
(3, 'INVESTIGATION', 'Chest X-Ray', 'CXR', 1, 400.00, 400.00, TRUE, 1);

