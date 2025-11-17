# Appointment Scheduling Implementation Summary

## Overview
Complete implementation of patient appointment scheduling functionality including entities, repositories, services, mappers, and controllers.

## ✅ Completed Components

### 1. Entity Classes (3 files)

#### **Appointment.java**
- Core appointment entity with all required fields
- Supports both OPD and IPD appointment types
- Tracks appointment lifecycle with status enum
- Includes patient relationship, physician info, and scheduling details
- Auto-generates appointment number (format: `APT-YYYYMMDD-XXXXX`)
- Audit fields: createdAt, updatedAt, createdBy, updatedBy

**Key Fields:**
- `appointmentNumber` - Unique identifier
- `patient` - ManyToOne relationship with Patient
- `appointmentType` - OPD/IPD enum
- `appointmentDate` and `appointmentTime` - Scheduling
- `status` - Appointment status (DRAFT, CONFIRMED, WAITING, etc.)
- `urgencyLevel` - NORMAL, URGENT, EMERGENCY
- `physicianId`, `departmentId` - Staff assignment
- `chiefComplaint`, `notes` - Clinical information

#### **Triage.java**
- Pre-consultation assessment entity
- OneToOne relationship with Appointment
- Captures comprehensive patient history
- Records nurse/junior doctor observations

**Key Fields:**
- `chiefComplaints` - Main symptoms
- `historyPresentIllness` - Current illness details
- `pastMedicalHistory` - Previous conditions
- `familyHistory`, `allergies`, `currentMedications`
- `recordedBy`, `recordedByName` - Staff info

#### **Vitals.java**
- Patient vital signs recording
- ManyToOne relationship with Appointment (multiple vitals per appointment)
- Auto-calculates BMI and BMI status
- Supports multiple readings over time

**Key Fields:**
- Physical measurements: `weight`, `height`, `headCircumference`
- Vital signs: `temperature`, `heartRate`, `respiratoryRate`, `systolicBp`, `diastolicBp`
- Clinical indicators: `spo2`, `randomBloodSugar`, `bmi`, `painLevel`
- `symptoms` - Collection of symptoms
- `recordedBy`, `recordedByName` - Staff info

### 2. Repository Interfaces (3 files)

#### **AppointmentRepository.java**
Comprehensive query methods for appointment management:

**Basic Queries:**
- `findByAppointmentNumber(String)` - Find by unique number
- `findByPatientId(Long, Pageable)` - Patient's appointments
- `findByPhysicianId(Long, Pageable)` - Physician's schedule
- `findByStatus(AppointmentStatus, Pageable)` - Filter by status
- `findByAppointmentDate(LocalDate, Pageable)` - Daily schedule

**Advanced Queries:**
- `searchAppointments(...)` - Multi-criteria search
- `searchByKeyword(String, Pageable)` - Full-text search by patient name/phone/appointment number
- `findByAppointmentDateBetween(...)` - Date range queries

**Business Logic Support:**
- `existsByPatientIdAndAppointmentDateAndStatus(...)` - Prevent double booking
- `countByAppointmentDate(LocalDate)` - Daily appointment count
- `countByPhysicianIdAndAppointmentDate(...)` - Physician's daily load

#### **TriageRepository.java**
- `findByAppointmentId(Long)` - Get triage by appointment
- `existsByAppointmentId(Long)` - Check if triage exists

#### **VitalsRepository.java**
- `findByAppointmentIdOrderByRecordedAtDesc(Long)` - Get all vitals (latest first)
- `findByAppointmentId(Long)` - All vitals for appointment

### 3. Mapper Interface (1 file)

#### **AppointmentMapper.java**
MapStruct-based mapper for entity-model conversions:

**Appointment Mapping:**
- `toEntity(AppointmentRequest)` - Request to entity
- `toResponse(Appointment)` - Entity to response
- Auto-generates patient full name

**Triage Mapping:**
- `toTriageEntity(TriageRequest)` - Request to entity
- `toTriageResponse(Triage)` - Entity to response

**Vitals Mapping:**
- `toVitalsEntity(VitalsRequest)` - Request to entity
- `toVitalsResponse(Vitals)` - Entity to response
- `toVitalsResponseList(List<Vitals>)` - List conversion

### 4. Service Layer (1 file)

#### **AppointmentService.java**
Complete business logic implementation:

**Appointment Management:**
- `createAppointment(AppointmentRequest)` - Create new appointment with validation
  - Validates patient exists
  - Prevents double booking (same patient, same date, confirmed status)
  - Auto-generates appointment number
  - Sets default status to CONFIRMED
  
- `getAllAppointments(Pageable)` - Paginated list of all appointments

- `getAppointmentById(Long)` - Get basic appointment details

- `getAppointmentDetailById(Long)` - Get comprehensive details including:
  - Basic appointment info
  - Patient info (name, age, gender, contact)
  - Triage data
  - All vitals records
  - (Future: examination, prescriptions, investigations)

- `updateAppointment(Long, AppointmentRequest)` - Update appointment details

- `cancelAppointment(Long)` - Cancel appointment (sets status to CANCELLED)

- `updateAppointmentStatus(Long, AppointmentStatus)` - Change appointment status

**Search & Filter:**
- `searchAppointments(...)` - Multi-criteria search supporting:
  - Patient ID, name, or contact number
  - Physician ID
  - Appointment status
  - Appointment date
  - Appointment type

**Triage Management:**
- `createOrUpdateTriage(Long, TriageRequest)` - Create or update triage data
- `getTriageByAppointment(Long)` - Retrieve triage data

**Vitals Management:**
- `recordVitals(Long, VitalsRequest)` - Record new vital signs
- `getVitalsByAppointment(Long)` - Get all vitals for appointment

**Helper Methods:**
- `generateAppointmentNumber(LocalDate)` - Format: `APT-YYYYMMDD-XXXXX`

### 5. Controller Layer (1 file)

#### **AppointmentController.java**
Implements `AppointmentManagementApi` interface generated from OpenAPI spec:

**Fully Implemented Endpoints:**

1. **POST /api/appointments** - Create appointment
2. **GET /api/appointments** - Get all appointments (paginated)
3. **GET /api/appointments/{id}** - Get appointment details
4. **PUT /api/appointments/{id}** - Update appointment
5. **DELETE /api/appointments/{id}** - Cancel appointment
6. **GET /api/appointments/search** - Search appointments
7. **PATCH /api/appointments/{id}/status** - Update status
8. **POST /api/appointments/{appointmentId}/triage** - Create/update triage
9. **GET /api/appointments/{appointmentId}/triage** - Get triage
10. **POST /api/appointments/{appointmentId}/vitals** - Record vitals
11. **GET /api/appointments/{appointmentId}/vitals** - Get vitals

**Placeholder Endpoints (To be implemented):**
- Examination recording and retrieval
- Prescription management
- Investigation orders and results
- Referral creation
- Patient admission (OPD to IPD)
- Patient discharge

### 6. OpenAPI Specification Updates (2 files)

#### **appointment.yml**
Added new request models:
- `UpdateAppointmentStatusRequest` - For status updates
- `UpdatePrescriptionStatusRequest` - For prescription status updates

#### **main.yml**
Updated to include the new models in components.schemas section

## 🗄️ Database Schema

### Tables Created:

**appointments**
```sql
- id (PK, BIGINT, AUTO_INCREMENT)
- appointment_number (VARCHAR(50), UNIQUE)
- patient_id (FK → patients.id)
- appointment_type (VARCHAR(20)) -- OPD/IPD
- appointment_date (DATE)
- appointment_time (TIME)
- duration (INT) -- minutes
- department_id (BIGINT)
- physician_id (BIGINT)
- physician_name (VARCHAR(255))
- department_name (VARCHAR(255))
- consultation_room (VARCHAR(50))
- urgency_level (VARCHAR(20))
- status (VARCHAR(30))
- chief_complaint (TEXT)
- notes (TEXT)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
- created_by (VARCHAR(100))
- updated_by (VARCHAR(100))
```

**triage**
```sql
- id (PK, BIGINT, AUTO_INCREMENT)
- appointment_id (FK → appointments.id, UNIQUE)
- chief_complaints (TEXT)
- history_present_illness (TEXT)
- past_medical_history (TEXT)
- family_history (TEXT)
- allergies (TEXT)
- current_medications (TEXT)
- social_history (TEXT)
- notes (TEXT)
- recorded_by (BIGINT)
- recorded_by_name (VARCHAR(255))
- recorded_at (TIMESTAMP)
```

**vitals**
```sql
- id (PK, BIGINT, AUTO_INCREMENT)
- appointment_id (FK → appointments.id)
- weight (DECIMAL(5,2)) -- kg
- height (DECIMAL(5,2)) -- cm
- head_circumference (DECIMAL(5,2)) -- cm
- temperature (DECIMAL(5,2))
- temperature_unit (VARCHAR(1)) -- F/C
- heart_rate (INT) -- bpm
- respiratory_rate (INT) -- breaths/min
- systolic_bp (INT) -- mmHg
- diastolic_bp (INT) -- mmHg
- spo2 (DECIMAL(5,2)) -- %
- random_blood_sugar (DECIMAL(6,2)) -- mg/dL
- bmi (DECIMAL(5,2))
- bmi_status (VARCHAR(20))
- pain_level (INT) -- 0-10
- recorded_by (BIGINT)
- recorded_by_name (VARCHAR(255))
- recorded_at (TIMESTAMP)
```

**vital_symptoms** (ElementCollection)
```sql
- vital_id (FK → vitals.id)
- symptom (VARCHAR(255))
```

## 📋 API Endpoints Summary

### Appointment Management
| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| POST | `/api/appointments` | Create new appointment | ✅ Implemented |
| GET | `/api/appointments` | Get all appointments (paginated) | ✅ Implemented |
| GET | `/api/appointments/{id}` | Get appointment details | ✅ Implemented |
| PUT | `/api/appointments/{id}` | Update appointment | ✅ Implemented |
| DELETE | `/api/appointments/{id}` | Cancel appointment | ✅ Implemented |
| GET | `/api/appointments/search` | Search appointments | ✅ Implemented |
| PATCH | `/api/appointments/{id}/status` | Update status | ✅ Implemented |

### Triage Management
| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| POST | `/api/appointments/{appointmentId}/triage` | Create/update triage | ✅ Implemented |
| GET | `/api/appointments/{appointmentId}/triage` | Get triage data | ✅ Implemented |

### Vitals Management
| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| POST | `/api/appointments/{appointmentId}/vitals` | Record vitals | ✅ Implemented |
| GET | `/api/appointments/{appointmentId}/vitals` | Get all vitals | ✅ Implemented |

### To Be Implemented
- Examination (2 endpoints)
- Prescriptions (4 endpoints)
- Investigations (3 endpoints)
- Referrals (1 endpoint)
- IPD Admission (1 endpoint)
- IPD Discharge (1 endpoint)

## 🔄 Appointment Workflow

### 1. Create Appointment
```
POST /api/appointments
{
  "patientId": 1,
  "appointmentDate": "2025-11-15",
  "appointmentTime": "10:30:00",
  "appointmentType": "OPD",
  "physicianId": 5,
  "urgencyLevel": "NORMAL",
  "chiefComplaint": "Fever and cough"
}
```

### 2. Triage (Nurse Assessment)
```
POST /api/appointments/1/triage
{
  "chiefComplaints": "High fever and dry cough",
  "historyPresentIllness": "Started 3 days ago",
  "allergies": "Penicillin"
}
```

### 3. Record Vitals
```
POST /api/appointments/1/vitals
{
  "temperature": 101.5,
  "heartRate": 88,
  "bloodPressure": "120/80",
  "spo2": 96
}
```

### 4. Update Status
```
PATCH /api/appointments/1/status
{
  "status": "IN_CONSULTATION"
}
```

## 🎯 Key Features

### Validation & Business Rules
1. **Prevent Double Booking**: Same patient cannot have multiple CONFIRMED appointments on same date
2. **Auto-Generate Appointment Number**: Format `APT-YYYYMMDD-XXXXX`
3. **Auto-Calculate BMI**: When height and weight are recorded
4. **BMI Status Classification**: Underweight, Normal, Overweight, Obese
5. **Default Values**: 
   - Status: DRAFT (then CONFIRMED on save)
   - Duration: 30 minutes
   - UrgencyLevel: NORMAL
   - AppointmentType: OPD

### Search & Filter Capabilities
- Search by patient name (partial match)
- Search by patient contact number
- Filter by patient ID
- Filter by physician ID
- Filter by appointment status
- Filter by appointment date
- Filter by appointment type
- Pagination and sorting support

### Audit Trail
- All entities track creation and update timestamps
- All entities track created_by and updated_by users
- Vitals and Triage track who recorded the data

## 📦 Files Created

### Java Files (10 files)
1. `entity/Appointment.java` - Core appointment entity
2. `entity/Triage.java` - Triage assessment entity
3. `entity/Vitals.java` - Vital signs entity
4. `repository/AppointmentRepository.java` - Appointment data access
5. `repository/TriageRepository.java` - Triage data access
6. `repository/VitalsRepository.java` - Vitals data access
7. `mapper/AppointmentMapper.java` - Entity-Model mapping
8. `service/AppointmentService.java` - Business logic
9. `controller/AppointmentController.java` - REST API endpoints
10. `APPOINTMENT_IMPLEMENTATION_SUMMARY.md` - This file

### OpenAPI Specification Updates (2 files)
1. `swagger/schemas/appointment.yml` - Added 2 new models
2. `swagger/paths/appointments.yml` - Updated to use new models

## 🚀 Next Steps

### Phase 2 - Examination Module
1. Create `Examination` entity
2. Create `ExaminationRepository`
3. Implement examination methods in `AppointmentService`
4. Update `AppointmentController` with examination endpoints

### Phase 3 - Prescription Module
1. Create `Prescription` and `PrescriptionItem` entities
2. Create repositories
3. Implement prescription CRUD in service
4. Update controller

### Phase 4 - Investigation Module
1. Create `InvestigationOrder` entity
2. Implement investigation order and result tracking
3. Support for Pathology and Radiology

### Phase 5 - Advanced Features
1. Referral management
2. IPD admission workflow
3. IPD discharge summary
4. Billing integration
5. Notification system

## 🧪 Testing

### Manual Testing Steps

1. **Test Appointment Creation**
```bash
curl -X POST http://localhost:8081/api/appointments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "patientId": 1,
    "appointmentDate": "2025-11-15",
    "appointmentTime": "10:30:00",
    "physicianId": 5
  }'
```

2. **Test Search**
```bash
curl -X GET "http://localhost:8081/api/appointments/search?patientName=John&page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

3. **Test Triage**
```bash
curl -X POST http://localhost:8081/api/appointments/1/triage \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "chiefComplaints": "Fever and cough",
    "allergies": "Penicillin"
  }'
```

## 📝 Notes

- All endpoints require authentication (Bearer token)
- Pagination defaults: page=0, size=20
- Sorting defaults: createdAt,desc
- Patient must exist before creating appointment
- Current implementation assumes H2 in-memory database
- TODO: Integration with Staff/Department entities for physician/department names
- TODO: Integration with User authentication for recordedBy/createdBy user IDs

## ✅ Implementation Status

**Core Appointment Features: 90% Complete**
- ✅ Appointment CRUD
- ✅ Triage recording
- ✅ Vitals recording
- ✅ Search and filtering
- ✅ Status management
- ⏳ Examination (pending)
- ⏳ Prescriptions (pending)
- ⏳ Investigations (pending)
- ⏳ Referrals (pending)
- ⏳ IPD workflow (pending)

---

**Last Updated**: January 2025  
**Version**: 1.0  
**Status**: Core implementation complete, ready for testing

