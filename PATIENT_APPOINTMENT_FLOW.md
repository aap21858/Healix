# Patient Appointment Management System

## Table of Contents
1. [Overview](#overview)
2. [Appointment Types](#appointment-types)
3. [System Workflow](#system-workflow)
4. [Status Lifecycle](#status-lifecycle)
5. [Database Schema](#database-schema)
6. [API Specifications](#api-specifications)
7. [Business Rules](#business-rules)

---

## Overview

The **Patient Appointment Management System** is a comprehensive solution for managing both **Outpatient (OPD)** and **Inpatient (IPD)** appointments in a healthcare facility. The system supports the entire patient journey from appointment scheduling to consultation, prescription, and integration with ancillary services like pharmacy, pathology, and radiology.

### Key Features
- **Multi-channel booking**: Phone, walk-in, online portal
- **Intelligent patient search**: Auto-complete with patient matching
- **Role-based access**: Receptionist, Doctor, Nurse, Admin
- **Complete patient journey tracking**: From registration to discharge
- **Clinical documentation**: Vitals, symptoms, examination notes
- **Integrated services**: Pharmacy, Pathology, Radiology
- **Prescription management**: Digital prescriptions with medication tracking
- **Referral system**: Inter-department and external referrals

---

## Appointment Types

### 1. OPD (Outpatient Department)
- **Purpose**: Consultation, diagnosis, and treatment without hospitalization
- **Duration**: 15-60 minutes (configurable)
- **Resources**: Doctor, consultation room
- **Billing**: Consultation fee + services rendered

### 2. IPD (Inpatient Department)
- **Purpose**: Hospitalization for extended treatment and monitoring
- **Duration**: Days/weeks (bed occupancy)
- **Resources**: Doctor, bed, nursing staff, ward
- **Billing**: Daily bed charges + procedures + medications + services
- **Special handling**: Bed allocation, admission notes, discharge summary

---

## System Workflow

### **Phase 1: Appointment Scheduling**

#### 1.1 Booking Channels
- **Phone Call**: Receptionist creates appointment
- **Walk-in**: Direct registration at front desk
- **Online Portal** *(future)**: Self-service booking

#### 1.2 Appointment Creation Process

```
┌─────────────────────────────────────────────────────────────┐
│  1. Patient Search/Selection                                 │
│     • Type: Name / Contact / Patient ID                     │
│     • System shows matching patients (autocomplete)         │
│     • Select existing OR click "Register New Patient"       │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ├─── Existing Patient ──► Auto-fill details
                   │
                   └─── New Patient ──► Open registration form
                            │
                            └──► Complete patient registration
                                 (Personal, Emergency Contact,
                                  Insurance, Medical History)
                                  
┌─────────────────────────────────────────────────────────────┐
│  2. Appointment Details Entry                               │
│     ┌─────────────────────────────────────────────────┐   │
│     │ • Patient: [Auto-filled/Selected]               │   │
│     │ • Appointment Type: [OPD / IPD]                 │   │
│     │ • Department: [Dropdown from lookup]            │   │
│     │ • Physician: [Filtered by department]           │   │
│     │ • Date: [Calendar picker]                       │   │
│     │ • Time Slot: [Available slots only]             │   │
│     │ • Duration: [15/30/45/60 minutes]              │   │
│     │ • Consultation Room: [Dropdown]                 │   │
│     │ • Urgency Level: [Normal/Urgent/Emergency]      │   │
│     │ • Chief Complaint: [Text area]                  │   │
│     │ • Notes: [Optional text]                        │   │
│     └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                   │
                   └──► Save ──► Status: CONFIRMED
```

**Default Values:**
- Current date + nearest available slot
- Current logged-in physician (if doctor)
- Default duration: 30 minutes
- Urgency: Normal

---

### **Phase 2: Patient Arrival & Check-in**

```
Patient arrives ──► Receptionist searches appointment
                    (by Name/ID/Contact)
                         │
                         └──► Marks as "ARRIVED"
                                  │
                                  └──► Status: WAITING
```

**Actions:**
- Update appointment status
- Queue management (display on waiting room screen)
- Notify assigned doctor/nurse

---

### **Phase 3: Pre-Consultation (Triage)**

**Performed by**: Junior Doctor / Nurse / Medical Assistant

#### 3.1 General Information Capture
- **Chief Complaints**: Primary reason for visit
- **History of Present Illness (HPI)**: Duration, severity, progression
- **Past Medical History**: Previous conditions, surgeries
- **Family History**: Hereditary conditions
- **Allergies**: Drug allergies, food allergies
- **Current Medications**: Ongoing treatments
- **Social History**: Smoking, alcohol, occupation
- **Notes**: Additional observations

#### 3.2 Clinical Assessments (Vitals)
| Parameter | Unit | Normal Range |
|-----------|------|--------------|
| Weight | kg | - |
| Height | cm | - |
| Head Circumference | cm | Pediatric only |
| Temperature | °F / °C | 97-99°F |
| Heart Rate (HR) | bpm | 60-100 |
| Respiratory Rate (RR) | breaths/min | 12-20 |
| Blood Pressure (BP) | mmHg | 120/80 |
| SpO2 | % | 95-100 |
| Random Blood Sugar (RBS) | mg/dL | 70-140 |
| BMI | kg/m² | 18.5-24.9 |
| Pain Level | 0-10 | Visual Analog Scale |

#### 3.3 Symptoms Checklist
- Fever, Cough, Breathlessness
- Nausea, Vomiting, Diarrhea
- Headache, Dizziness
- Chest pain, Palpitations
- *(Customizable based on specialty)*

**Status Update**: `WAITING` → `IN_TRIAGE`

---

### **Phase 4: Doctor Consultation**

**Performed by**: Consulting Physician

```
Doctor calls patient ──► Status: IN_CONSULTATION
         │
         ├──► Reviews triage data
         │
         ├──► Physical Examination
         │    ┌────────────────────────────────────┐
         │    │ • General Appearance               │
         │    │ • Systemic Examination:            │
         │    │   - Cardiovascular System (CVS)    │
         │    │   - Respiratory System (RS)        │
         │    │   - Gastrointestinal (GIT)         │
         │    │   - Central Nervous System (CNS)   │
         │    │   - Musculoskeletal                │
         │    │ • Examination Findings             │
         │    └────────────────────────────────────┘
         │
         ├──► Provisional Diagnosis
         │    ┌────────────────────────────────────┐
         │    │ • Primary Diagnosis (ICD-10 code)  │
         │    │ • Differential Diagnosis (D/D)     │
         │    └────────────────────────────────────┘
         │
         ├──► Treatment Plan
         │    ┌────────────────────────────────────┐
         │    │ A. Prescription (Medications)      │
         │    │ B. Investigation Orders:           │
         │    │    - Pathology (Blood, Urine, etc) │
         │    │    - Radiology (X-ray, CT, MRI)    │
         │    │ C. Procedures (if any)             │
         │    │ D. Follow-up Date                  │
         │    └────────────────────────────────────┘
         │
         ├──► Special Actions (Optional)
         │    ┌────────────────────────────────────┐
         │    │ • Refer to Another Department      │
         │    │ • Refer to External Specialist     │
         │    │ • Admit to IPD (Hospitalization)   │
         │    └────────────────────────────────────┘
         │
         └──► Consultation Complete
                    │
                    └──► Status: TO_INVOICE
```

#### 4.1 Prescription Management

**Structure:**
```
Prescription
  ├── Prescription Header
  │   ├── Patient ID
  │   ├── Prescribing Doctor
  │   ├── Date & Time
  │   ├── Diagnosis
  │   └── Status: [DRAFT / PRESCRIBED / DISPENSED]
  │
  └── Medication Lines
      ├── Medicine Name
      ├── Dosage (e.g., 500mg)
      ├── Form (Tablet/Syrup/Injection)
      ├── Frequency (e.g., 1-0-1)
      ├── Duration (e.g., 5 days)
      ├── Route (Oral/IV/IM)
      ├── Instructions (Before/After meals)
      └── Quantity
```

**Status Flow:**
- `DRAFT`: Doctor is still editing
- `PRESCRIBED`: Finalized and sent to pharmacy
- `DISPENSED`: Medicines issued by pharmacy

---

### **Phase 5: Ancillary Services**

#### 5.1 Pathology Orders (Lab Tests)
- Blood tests (CBC, LFT, RFT, etc.)
- Urine analysis
- Cultures & sensitivity
- Biopsy

**Flow:**
```
Doctor orders test ──► Lab receives order
                         │
                         ├──► Sample collection
                         ├──► Testing
                         ├──► Result entry
                         └──► Report sent to Doctor
```

#### 5.2 Radiology Orders (Imaging)
- X-Ray
- Ultrasound (USG)
- CT Scan
- MRI
- Mammography

**Flow:**
```
Doctor orders imaging ──► Radiology receives order
                            │
                            ├──► Patient scheduled
                            ├──► Imaging performed
                            ├──► Report by Radiologist
                            └──► Sent to Doctor
```

---

### **Phase 6: Billing & Checkout**

```
Consultation complete ──► Status: TO_INVOICE
         │
         └──► Generate Bill
                ├── Consultation Fee
                ├── Procedures
                ├── Investigations ordered
                ├── Medications (if dispensed)
                └── Other services
                    │
                    ├──► Payment received
                    │       │
                    │       └──► Status: COMPLETED
                    │
                    └──► Partial payment
                            └──► Status: PENDING_PAYMENT
```

---

## Status Lifecycle

### Appointment Status Flow

```
         ┌─────────┐
         │  DRAFT  │ (Appointment being created)
         └────┬────┘
              │ save
              ▼
       ┌───────────┐
       │ CONFIRMED │ (Appointment scheduled)
       └─────┬─────┘
             │ patient arrives
             ▼
       ┌──────────┐
       │ WAITING  │ (Waiting for consultation)
       └────┬─────┘
            │ nurse/staff calls
            ▼
      ┌────────────┐
      │ IN_TRIAGE  │ (Vitals & pre-assessment)
      └──────┬─────┘
             │ doctor calls
             ▼
   ┌────────────────────┐
   │ IN_CONSULTATION    │ (Doctor examining)
   └──────┬─────────────┘
          │ consultation complete
          ▼
    ┌─────────────┐
    │ TO_INVOICE  │ (Ready for billing)
    └──────┬──────┘
           │ payment done
           ▼
     ┌───────────┐
     │ COMPLETED │ (Appointment finished)
     └───────────┘
     
     
     ANY STATUS ──► CANCELLED (If patient doesn't show or cancels)
     ANY STATUS ──► NO_SHOW (If patient confirmed but didn't arrive)
```

### IPD-Specific Status
```
CONFIRMED ──► ADMITTED ──► IN_TREATMENT ──► DISCHARGE_READY ──► DISCHARGED
```

---

## Database Schema

### Table: `appointments`

```sql
CREATE TABLE appointments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    appointment_number VARCHAR(20) UNIQUE NOT NULL, -- Auto-generated: APT-YYYYMMDD-XXXXX
    
    -- Patient & Type
    patient_id BIGINT NOT NULL,
    appointment_type VARCHAR(10) NOT NULL, -- OPD, IPD
    
    -- Scheduling Details
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    duration INT DEFAULT 30, -- In minutes
    
    -- Resource Allocation
    department_id BIGINT,
    physician_id BIGINT NOT NULL,
    consultation_room VARCHAR(50),
    
    -- Priority & Status
    urgency_level VARCHAR(20) DEFAULT 'NORMAL', -- NORMAL, URGENT, EMERGENCY
    status VARCHAR(30) DEFAULT 'DRAFT', -- As per lifecycle above
    
    -- Clinical Info
    chief_complaint TEXT,
    notes TEXT,
    
    -- Referral (if applicable)
    referred_from_appointment_id BIGINT,
    referred_to_department_id BIGINT,
    referred_to_physician_id BIGINT,
    
    -- IPD Specific
    bed_id BIGINT, -- For IPD appointments
    admission_date DATETIME,
    discharge_date DATETIME,
    
    -- Audit
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (physician_id) REFERENCES staff(id),
    FOREIGN KEY (department_id) REFERENCES dropdown_lookup(id),
    FOREIGN KEY (referred_to_physician_id) REFERENCES staff(id),
    INDEX idx_appointment_date (appointment_date),
    INDEX idx_patient (patient_id),
    INDEX idx_physician (physician_id),
    INDEX idx_status (status)
);
```

### Table: `appointment_triage`

```sql
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
    
    FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    FOREIGN KEY (recorded_by) REFERENCES staff(id)
);
```

### Table: `appointment_vitals`

```sql
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
    spo2 DECIMAL(4,2), -- Oxygen saturation %
    random_blood_sugar DECIMAL(5,2), -- mg/dL
    bmi DECIMAL(4,2), -- Calculated
    bmi_status VARCHAR(20), -- Underweight, Normal, Overweight, Obese
    pain_level INT, -- 0-10 scale
    
    -- Symptoms (comma-separated or JSON)
    symptoms JSON,
    
    -- Audit
    recorded_by BIGINT NOT NULL,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    FOREIGN KEY (recorded_by) REFERENCES staff(id)
);
```

### Table: `appointment_examination`

```sql
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
    
    FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    FOREIGN KEY (examined_by) REFERENCES staff(id)
);
```

### Table: `prescriptions`

```sql
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
    
    FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (physician_id) REFERENCES staff(id),
    INDEX idx_prescription_patient (patient_id),
    INDEX idx_prescription_date (prescription_date)
);
```

### Table: `prescription_items`

```sql
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
    
    FOREIGN KEY (prescription_id) REFERENCES prescriptions(id) ON DELETE CASCADE
);
```

### Table: `investigation_orders`

```sql
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
    
    FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (ordered_by) REFERENCES staff(id),
    FOREIGN KEY (reported_by) REFERENCES staff(id)
);
```

### Table: `appointment_services` (Consumed Products/Services)

```sql
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
    
    FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);
```

---

## API Specifications

### Base URL
```
http://localhost:8080/api
```

### Authentication
All endpoints require JWT Bearer token except login.
```
Authorization: Bearer <token>
```

---

### **1. Appointment Management APIs**

#### **1.1 Create Appointment**

**Endpoint:** `POST /api/appointments`

**Request Body:**
```json
{
  "patientId": 1,
  "appointmentType": "OPD",
  "appointmentDate": "2025-11-15",
  "appointmentTime": "10:30:00",
  "duration": 30,
  "departmentId": 101,
  "physicianId": 5,
  "consultationRoom": "Room 203",
  "urgencyLevel": "NORMAL",
  "chiefComplaint": "Fever and cough for 3 days",
  "notes": "Patient has a history of asthma"
}
```

**Response:** `201 Created`
```json
{
  "id": 1001,
  "appointmentNumber": "APT-20251115-00001",
  "patientId": 1,
  "patientName": "John Doe",
  "appointmentType": "OPD",
  "appointmentDate": "2025-11-15",
  "appointmentTime": "10:30:00",
  "duration": 30,
  "physicianName": "Dr. Smith",
  "departmentName": "General Medicine",
  "status": "CONFIRMED",
  "createdAt": "2025-11-11T09:00:00"
}
```

---

#### **1.2 Search Appointments**

**Endpoint:** `GET /api/appointments/search`

**Query Parameters:**
- `patientName` (optional): Partial match
- `patientId` (optional)
- `contactNumber` (optional)
- `appointmentDate` (optional)
- `physicianId` (optional)
- `status` (optional)
- `page` (default: 0)
- `size` (default: 20)

**Example:**
```
GET /api/appointments/search?patientName=John&appointmentDate=2025-11-15
```

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1001,
      "appointmentNumber": "APT-20251115-00001",
      "patientName": "John Doe",
      "patientContact": "+91 9876543210",
      "appointmentDate": "2025-11-15",
      "appointmentTime": "10:30:00",
      "physicianName": "Dr. Smith",
      "status": "CONFIRMED"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "currentPage": 0
}
```

---

#### **1.3 Update Appointment Status**

**Endpoint:** `PATCH /api/appointments/{id}/status`

**Request Body:**
```json
{
  "status": "WAITING"
}
```

**Response:** `200 OK`

---

#### **1.4 Get Appointment Details**

**Endpoint:** `GET /api/appointments/{id}`

**Response:** `200 OK`
```json
{
  "id": 1001,
  "appointmentNumber": "APT-20251115-00001",
  "patient": {
    "id": 1,
    "name": "John Doe",
    "age": 35,
    "gender": "MALE",
    "contactNumber": "+91 9876543210"
  },
  "appointmentType": "OPD",
  "appointmentDate": "2025-11-15",
  "appointmentTime": "10:30:00",
  "physician": {
    "id": 5,
    "name": "Dr. Smith",
    "specialization": "General Medicine"
  },
  "department": "General Medicine",
  "status": "CONFIRMED",
  "chiefComplaint": "Fever and cough for 3 days",
  "triage": null,
  "examination": null,
  "prescriptions": []
}
```

---

### **2. Triage Management APIs**

#### **2.1 Create/Update Triage**

**Endpoint:** `POST /api/appointments/{appointmentId}/triage`

**Request Body:**
```json
{
  "chiefComplaints": "High fever and dry cough",
  "historyPresentIllness": "Fever started 3 days ago, cough since yesterday",
  "pastMedicalHistory": "Asthma diagnosed 5 years ago",
  "allergies": "Penicillin",
  "currentMedications": "Salbutamol inhaler",
  "notes": "Patient appears distressed"
}
```

**Response:** `201 Created`

---

#### **2.2 Record Vitals**

**Endpoint:** `POST /api/appointments/{appointmentId}/vitals`

**Request Body:**
```json
{
  "weight": 70.5,
  "height": 175,
  "temperature": 101.5,
  "temperatureUnit": "F",
  "heartRate": 88,
  "respiratoryRate": 18,
  "systolicBp": 120,
  "diastolicBp": 80,
  "spo2": 96,
  "randomBloodSugar": 110,
  "painLevel": 3,
  "symptoms": ["Fever", "Cough", "Headache"]
}
```

**Response:** `201 Created`

---

### **3. Consultation & Examination APIs**

#### **3.1 Record Examination**

**Endpoint:** `POST /api/appointments/{appointmentId}/examination`

**Request Body:**
```json
{
  "generalAppearance": "Patient is alert and oriented",
  "cardiovascularSystem": "S1 S2 heard, no murmurs",
  "respiratorySystem": "Bilateral air entry equal, no added sounds",
  "examinationFindings": "Mild tachycardia, temperature elevated",
  "primaryDiagnosis": "Upper Respiratory Tract Infection",
  "primaryDiagnosisIcd10": "J06.9",
  "differentialDiagnosis": "Pneumonia, Bronchitis",
  "treatmentPlan": "Antipyretics, antibiotics, rest",
  "advice": "Drink plenty of fluids, avoid cold beverages",
  "followUpDate": "2025-11-20",
  "followUpInstructions": "Return if fever persists beyond 3 days"
}
```

**Response:** `201 Created`

---

### **4. Prescription APIs**

#### **4.1 Create Prescription**

**Endpoint:** `POST /api/appointments/{appointmentId}/prescriptions`

**Request Body:**
```json
{
  "diagnosis": "Upper Respiratory Tract Infection",
  "medicationGroup": "Antibiotics & Antipyretics",
  "instructions": "Complete the full course of antibiotics",
  "items": [
    {
      "medicineName": "Amoxicillin",
      "dosage": "500mg",
      "form": "TABLET",
      "frequency": "1-0-1",
      "duration": 5,
      "route": "ORAL",
      "instructions": "After meals",
      "quantity": 10
    },
    {
      "medicineName": "Paracetamol",
      "dosage": "650mg",
      "form": "TABLET",
      "frequency": "1-1-1",
      "duration": 3,
      "route": "ORAL",
      "instructions": "When needed for fever",
      "quantity": 9
    }
  ]
}
```

**Response:** `201 Created`
```json
{
  "id": 5001,
  "prescriptionNumber": "RX-20251115-00001",
  "appointmentId": 1001,
  "patientName": "John Doe",
  "physicianName": "Dr. Smith",
  "prescriptionDate": "2025-11-15",
  "status": "DRAFT",
  "items": [...]
}
```

---

#### **4.2 Update Prescription Status**

**Endpoint:** `PATCH /api/prescriptions/{id}/status`

**Request Body:**
```json
{
  "status": "PRESCRIBED"
}
```

---

#### **4.3 Get Prescription by Appointment**

**Endpoint:** `GET /api/appointments/{appointmentId}/prescriptions`

**Response:** `200 OK`

---

### **5. Investigation Orders APIs**

#### **5.1 Create Investigation Order**

**Endpoint:** `POST /api/appointments/{appointmentId}/investigations`

**Request Body:**
```json
{
  "orderType": "PATHOLOGY",
  "testName": "Complete Blood Count",
  "testCode": "CBC",
  "clinicalNotes": "Fever investigation",
  "urgency": "ROUTINE"
}
```

**Response:** `201 Created`

---

#### **5.2 Update Investigation Result**

**Endpoint:** `PATCH /api/investigations/{id}/result`

**Request Body:**
```json
{
  "resultValue": "WBC: 12000, Hb: 13.5",
  "resultStatus": "ABNORMAL",
  "resultNotes": "Leukocytosis noted",
  "resultReportUrl": "https://reports.healix.com/inv-123.pdf"
}
```

---

### **6. Referral APIs**

#### **6.1 Create Referral**

**Endpoint:** `POST /api/appointments/{appointmentId}/refer`

**Request Body:**
```json
{
  "referralType": "INTERNAL",
  "referredToDepartmentId": 105,
  "referredToPhysicianId": 12,
  "referralReason": "Cardiology consultation required",
  "clinicalSummary": "Patient has chest pain, needs ECG and echo"
}
```

**Response:** `201 Created` (Creates new appointment)

---

### **7. IPD Management APIs**

#### **7.1 Admit Patient (IPD)**

**Endpoint:** `POST /api/appointments/{appointmentId}/admit`

**Request Body:**
```json
{
  "bedId": 205,
  "admissionNotes": "Patient admitted for observation and IV antibiotics",
  "estimatedDischargeDate": "2025-11-20"
}
```

**Response:** `200 OK`

---

#### **7.2 Discharge Patient**

**Endpoint:** `POST /api/appointments/{appointmentId}/discharge`

**Request Body:**
```json
{
  "dischargeSummary": "Patient improved, vitals stable",
  "dischargeInstructions": "Continue oral medications for 5 days",
  "followUpDate": "2025-11-25"
}
```

**Response:** `200 OK`

---

## Business Rules

### 1. **Appointment Booking**
- No double booking: Same physician cannot have overlapping appointments
- Slot validation: Check physician availability before confirming
- Lead time: OPD appointments must be at least 15 minutes in future
- Cancellation: Appointments can be cancelled up to 2 hours before scheduled time

### 2. **Status Transitions**
- Only receptionist can mark as `WAITING`
- Only assigned nurse/doctor can update to `IN_TRIAGE`
- Only assigned physician can mark `IN_CONSULTATION`
- Prescription must be created before `TO_INVOICE`
- Payment required to move to `COMPLETED`

### 3. **Referrals**
- Inter-department referrals create new appointment with original appointment reference
- Referral notes must be visible to receiving physician
- Patient consent required for external referrals

### 4. **Prescriptions**
- Only `PRESCRIBED` prescriptions can be dispensed by pharmacy
- Controlled substances require additional validation
- Prescription validity: 30 days from date of issue

### 5. **Investigations**
- Orders auto-sent to respective departments (Pathology/Radiology)
- Critical results trigger alert to ordering physician
- Reports attached to patient medical record

### 6. **IPD Admissions**
- Bed availability must be checked before admission
- Admission requires approval from senior physician
- Daily progress notes mandatory for IPD patients
- Discharge summary mandatory before checkout

---

## Integration Points

### **Pharmacy Integration**
- Receive prescription orders
- Update dispensed status
- Track inventory
- Bill medications

### **Pathology Integration**
- Receive investigation orders
- Update sample collection status
- Enter results
- Generate reports

### **Radiology Integration**
- Receive imaging orders
- Schedule slots
- Upload images/reports
- Notify ordering physician

### **Billing Integration**
- Pull appointment services
- Generate invoice
- Process payments
- Insurance claims

---

## Future Enhancements

1. **Online Appointment Booking**: Patient portal for self-service
2. **SMS/Email Notifications**: Appointment reminders, report readiness
3. **Telemedicine**: Video consultation support
4. **AI-Assisted Diagnosis**: Symptom checker, diagnosis suggestions
5. **Mobile App**: For doctors and patients
6. **Queue Management System**: Digital displays in waiting areas
7. **Electronic Health Records (EHR)**: Complete patient history visualization
8. **Analytics Dashboard**: Appointment trends, physician performance, revenue

---

## Security & Compliance

- **HIPAA/Data Privacy**: Patient data encryption at rest and in transit
- **Role-Based Access Control (RBAC)**: Strict permissions for different user roles
- **Audit Logs**: All actions logged with user ID and timestamp
- **Data Backup**: Daily automated backups with point-in-time recovery
- **Session Management**: JWT tokens with expiry and refresh mechanism

---

**Document Version:** 1.0  
**Last Updated:** November 11, 2025  
**Maintained By:** Healix Development Team
