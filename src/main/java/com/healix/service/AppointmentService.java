package com.healix.service;

import com.healix.entity.Appointment;
import com.healix.entity.AppointmentExamination;
import com.healix.entity.Patient;
import com.healix.entity.Vitals;
import com.healix.exception.ResourceNotFoundException;
import com.healix.mapper.AppointmentMapper;
import com.healix.model.*;
import com.healix.repository.AppointmentExaminationRepository;
import com.healix.repository.AppointmentRepository;
import com.healix.repository.PatientRepository;
import com.healix.repository.VitalsRepository;
import com.healix.util.CurrentUser;
import com.healix.validation.VitalsValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final AppointmentExaminationRepository examinationRepository;
    private final VitalsRepository vitalsRepository;
    private final AppointmentMapper appointmentMapper;
    private final CurrentUser currentUser;
    private final AppointmentAuditService appointmentAuditService;
    private final VitalsValidator vitalsValidator;
    private final com.healix.repository.StaffRepository staffRepository;

    /**
     * Generate unique appointment number
     * Format: APT-YYYYMMDD-XXXXX
     */
    private String generateAppointmentNumber(LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = appointmentRepository.countByAppointmentDate(date);
        return String.format("APT-%s-%05d", dateStr, count + 1);
    }

    /**
     * Create a new appointment
     */
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        // Validate patient exists
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with ID: " + request.getPatientId()));

        // Check if patient already has an appointment on the same date with statuses
        boolean hasExistingAppointment = appointmentRepository.existsByPatientIdAndAppointmentDateAndStatusNotIn(
                request.getPatientId(),
                request.getAppointmentDate(),
                List.of(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW, AppointmentStatus.COMPLETED, AppointmentStatus.DRAFT)
        );

        if (hasExistingAppointment) {
            log.warn("Patient {} already has a confirmed appointment on {}",
                    request.getPatientId(), request.getAppointmentDate());
            throw new IllegalStateException(
                    "Patient already has a appointment with active status on " + request.getAppointmentDate());
        }

        // Map request to entity (mapper automatically converts appointmentTime String to LocalTime)
        Appointment appointment = appointmentMapper.toEntity(request);
        appointment.setPatient(patient);
        appointment.setAppointmentNumber(generateAppointmentNumber(request.getAppointmentDate()));
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        // Populate physician name from physicianId
        if (request.getPhysicianId() != null) {
            staffRepository.findById(request.getPhysicianId()).ifPresent(staff ->
                appointment.setPhysicianName(staff.getFullName())
            );
        }

        // Set user info
        String username = currentUser.getDisplayNameOrFallback();
        appointment.setCreatedBy(username);
        appointment.setUpdatedBy(username);

        // Save appointment
        Appointment savedAppointment = appointmentRepository.save(appointment);
        log.info("Appointment created successfully: {}", savedAppointment.getAppointmentNumber());

        // Log audit
        appointmentAuditService.logAppointmentCreation(
                savedAppointment.getId(),
                savedAppointment.getStatus().name(),
                savedAppointment.getAppointmentDate(),
                savedAppointment.getAppointmentTime()
        );

        return appointmentMapper.toResponse(savedAppointment);
    }

    /**
     * Get all appointments with pagination
     */
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getAllAppointments(Pageable pageable) {
        log.info("Fetching all appointments - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Appointment> appointments = appointmentRepository.findAll(pageable);
        return appointments.map(appointmentMapper::toResponse);
    }

    /**
     * Get appointment by ID
     */
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id) {
        log.info("Fetching appointment by ID: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with ID: " + id));

        return appointmentMapper.toResponse(appointment);
    }

    /**
     * Get detailed appointment by ID (includes triage, vitals, etc.)
     */
    @Transactional(readOnly = true)
    public AppointmentDetailResponse getAppointmentDetailById(Long id) {
        log.info("Fetching detailed appointment by ID: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with ID: " + id));

        AppointmentDetailResponse response = new AppointmentDetailResponse();

        // Map basic appointment data
        AppointmentResponse basicResponse = appointmentMapper.toResponse(appointment);
        response.setId(basicResponse.getId());
        response.setAppointmentNumber(basicResponse.getAppointmentNumber());
        response.setPatientId(basicResponse.getPatientId());
        response.setPatientName(basicResponse.getPatientName());
        response.setAppointmentType(basicResponse.getAppointmentType());
        response.setAppointmentDate(basicResponse.getAppointmentDate());
        response.setAppointmentTime(basicResponse.getAppointmentTime());
        response.setDuration(basicResponse.getDuration());
        response.setPhysicianId(basicResponse.getPhysicianId());
        response.setPhysicianName(basicResponse.getPhysicianName());
        response.setDepartmentId(basicResponse.getDepartmentId());
        response.setDepartmentName(basicResponse.getDepartmentName());
        response.setConsultationRoom(basicResponse.getConsultationRoom());
        response.setUrgencyLevel(basicResponse.getUrgencyLevel());
        response.setStatus(basicResponse.getStatus());
        response.setChiefComplaint(basicResponse.getChiefComplaint());
        response.setNotes(basicResponse.getNotes());
        response.setCreatedAt(basicResponse.getCreatedAt());
        response.setUpdatedAt(basicResponse.getUpdatedAt());

        // Add patient info
        Patient patient = appointment.getPatient();
        if (patient != null) {
            com.healix.model.AppointmentDetailResponseAllOfPatient patientInfo = new com.healix.model.AppointmentDetailResponseAllOfPatient();
            patientInfo.setId(patient.getId());
            patientInfo.setName(patient.getFirstName() + " " + patient.getLastName());
            patientInfo.setAge(patient.getAge());
            patientInfo.setGender(patient.getGender().name());
            patientInfo.setContactNumber(patient.getMobileNumber());
            response.setPatient(patientInfo);
        }

        // Add examination data if exists
        examinationRepository.findByAppointmentId(id)
                .ifPresent(examination -> response.setExamination(appointmentMapper.toExaminationResponse(examination)));

        // Add vitals data
        List<Vitals> vitals = vitalsRepository.findByAppointmentIdOrderByRecordedAtDesc(id);
        response.setVitals(appointmentMapper.toVitalsResponseList(vitals));

        return response;
    }

    /**
     * Update appointment
     */
    public AppointmentResponse updateAppointment(Long id, AppointmentRequest request) {
        log.info("Updating appointment ID: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with ID: " + id));

        // Update fields
        if (request.getAppointmentDate() != null) {
            appointment.setAppointmentDate(request.getAppointmentDate());
        }
        if (request.getAppointmentTime() != null) {
            // Mapper automatically converts String to LocalTime
            appointment.setAppointmentTime(appointmentMapper.map(request.getAppointmentTime()));
        }
        if (request.getDuration() != null) {
            appointment.setDuration(request.getDuration());
        }
        if (request.getPhysicianId() != null) {
            appointment.setPhysicianId(request.getPhysicianId());
            // Update physician name when physician changes
            staffRepository.findById(request.getPhysicianId()).ifPresent(staff ->
                appointment.setPhysicianName(staff.getFullName())
            );
        }
        if (request.getDepartmentId() != null) {
            appointment.setDepartmentId(request.getDepartmentId());
        }
        if (request.getConsultationRoom() != null) {
            appointment.setConsultationRoom(request.getConsultationRoom());
        }
        if (request.getUrgencyLevel() != null) {
            appointment.setUrgencyLevel(request.getUrgencyLevel());
        }
        if (request.getChiefComplaint() != null) {
            appointment.setChiefComplaint(request.getChiefComplaint());
        }
        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }

        String username = currentUser.getDisplayNameOrFallback();
        appointment.setUpdatedBy(username);

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        log.info("Appointment updated successfully: {}", id);

        return appointmentMapper.toResponse(updatedAppointment);
    }


    /**
     * Update appointment status
     * Validate that the new status is a logical progression from the current status
     */
    public AppointmentResponse updateAppointmentStatus(Long id, AppointmentStatus newStatus) {
        log.info("Updating appointment {} status to: {}", id, newStatus);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with ID: " + id));

        AppointmentStatus currentStatus = appointment.getStatus();
        if (currentStatus == null) currentStatus = AppointmentStatus.DRAFT;

        if (newStatus == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New status must be provided");
        }

        if (!isValidStatusTransition(currentStatus, newStatus)) {
            String msg = String.format("Invalid status transition: %s -> %s", currentStatus, newStatus);
            log.warn(msg);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
        }

        appointment.setStatus(newStatus);
        String username = currentUser.getDisplayNameOrFallback();
        appointment.setUpdatedBy(username);

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        log.info("Appointment status updated successfully");

        return appointmentMapper.toResponse(updatedAppointment);
    }

    /**
     * Cancel appointment
     */
    public void cancelAppointment(Long id) {
        log.info("Cancelling appointment ID: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with ID: " + id));

        appointment.setStatus(AppointmentStatus.CANCELLED);
        String username = currentUser.getDisplayNameOrFallback();
        appointment.setUpdatedBy(username);

        appointmentRepository.save(appointment);
        log.info("Appointment cancelled successfully: {}", id);
    }

    /**
     * Search appointments by various criteria
     */
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> searchAppointments(
            Long patientId,
            Long physicianId,
            AppointmentStatus status,
            LocalDate appointmentDate,
            String contactNumber,
            String patientName,
            Pageable pageable) {

        log.info("Searching appointments with filters - patientId: {}, physicianId: {}, status: {}, date: {}",
                patientId, physicianId, status, appointmentDate);

        Page<Appointment> appointments;

        // If searching by name or contact, use keyword search
        if (patientName != null && !patientName.trim().isEmpty()) {
            appointments = appointmentRepository.searchByKeyword(patientName, pageable);
        } else if (contactNumber != null && !contactNumber.trim().isEmpty()) {
            appointments = appointmentRepository.searchByKeyword(contactNumber, pageable);
        } else {
            // Use specific criteria search
            appointments = appointmentRepository.searchAppointments(
                    patientId, physicianId, status, appointmentDate, null, pageable);
        }

        return appointments.map(appointmentMapper::toResponse);
    }

    /**
     * Reschedule an appointment
     */
    public AppointmentResponse rescheduleAppointment(Long appointmentId, LocalDate newDate, String newTime, String reason) {
        log.info("Rescheduling appointment ID: {} to {} {}", appointmentId, newDate, newTime);

        Appointment oldAppointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));

        // Store old values for audit
        LocalDate oldDate = oldAppointment.getAppointmentDate();
        LocalTime oldTimeValue = oldAppointment.getAppointmentTime();

        // Create new appointment
        Appointment newAppointment = Appointment.builder()
                .patient(oldAppointment.getPatient())
                .appointmentType(oldAppointment.getAppointmentType())
                .appointmentDate(newDate)
                .appointmentTime(appointmentMapper.map(newTime))  // Use mapper for String to LocalTime conversion
                .duration(oldAppointment.getDuration())
                .departmentId(oldAppointment.getDepartmentId())
                .departmentName(oldAppointment.getDepartmentName())
                .specialty(oldAppointment.getSpecialty())
                .physicianId(oldAppointment.getPhysicianId())
                .physicianName(oldAppointment.getPhysicianName())
                .consultationRoom(oldAppointment.getConsultationRoom())
                .urgencyLevel(oldAppointment.getUrgencyLevel())
                .status(AppointmentStatus.CONFIRMED)
                .chiefComplaint(oldAppointment.getChiefComplaint())
                .notes(oldAppointment.getNotes() + (reason != null ? "\nRescheduled: " + reason : ""))
                .build();

        newAppointment.setAppointmentNumber(generateAppointmentNumber(newDate));

        String username = currentUser.getDisplayNameOrFallback();
        newAppointment.setCreatedBy(username);
        newAppointment.setUpdatedBy(username);

        Appointment savedNewAppointment = appointmentRepository.save(newAppointment);

        // Update old appointment
        oldAppointment.setStatus(AppointmentStatus.CANCELLED);
        oldAppointment.setRescheduledToId(savedNewAppointment.getId());
        oldAppointment.setCancellationReason("Rescheduled to " + newDate + " " + newTime);
        oldAppointment.setUpdatedBy(username);
        appointmentRepository.save(oldAppointment);

        // Update new appointment with link to old
        savedNewAppointment.setRescheduledFromId(appointmentId);
        savedNewAppointment = appointmentRepository.save(savedNewAppointment);

        // Log audit
        appointmentAuditService.logRescheduling(
                appointmentId,
                oldDate,
                oldTimeValue,
                newDate,
                appointmentMapper.map(newTime),  // Use mapper for String to LocalTime conversion
                reason
        );

        appointmentAuditService.logAppointmentCreation(
                savedNewAppointment.getId(),
                savedNewAppointment.getStatus().name(),
                savedNewAppointment.getAppointmentDate(),
                savedNewAppointment.getAppointmentTime()
        );

        log.info("Appointment rescheduled successfully. Old ID: {}, New ID: {}", appointmentId, savedNewAppointment.getId());

        return appointmentMapper.toResponse(savedNewAppointment);
    }

    /**
     * Cancel appointment with reason
     */
    public void cancelAppointmentWithReason(Long id, String reason) {
        log.info("Cancelling appointment ID: {} with reason: {}", id, reason);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + id));

        String oldStatus = appointment.getStatus().name();
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(reason);
        appointment.setCancelledAt(LocalDateTime.now());

        // Get current user ID
        Long userId = null;
        try {
            if (currentUser != null && currentUser.getCurrentUser() != null) {
                userId = currentUser.getCurrentUser().getId();
            }
        } catch (Exception e) {
            log.warn("Could not get current user ID");
        }
        appointment.setCancelledBy(userId);

        String username = currentUser.getDisplayNameOrFallback();
        appointment.setUpdatedBy(username);

        appointmentRepository.save(appointment);

        // Log audit
        appointmentAuditService.logCancellation(id, reason);
        appointmentAuditService.logStatusChange(id, oldStatus, AppointmentStatus.CANCELLED.name(), reason);

        log.info("Appointment cancelled successfully: {}", id);
    }

    /**
     * Get triage data by appointment ID

    /**
     * Record vitals with comprehensive validation
     */
    public VitalsResponse recordVitals(Long appointmentId, VitalsRequest request) {
        log.info("Recording vitals for appointment: {}", appointmentId);

        // 1. Validate appointment exists
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with ID: " + appointmentId));

        // 2. Validate appointment status - vitals should only be recorded for active appointments
        if (appointment.getStatus() == AppointmentStatus.CANCELLED ||
            appointment.getStatus() == AppointmentStatus.NO_SHOW ||
            appointment.getStatus() == AppointmentStatus.COMPLETED) {
            log.warn("Cannot record vitals for appointment in {} status", appointment.getStatus());
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Cannot record vitals for appointment with status: " + appointment.getStatus()
            );
        }

        // 3. Check if vitals were recently recorded (within last 5 minutes) to prevent duplicates
        List<Vitals> recentVitals = vitalsRepository.findByAppointmentIdOrderByRecordedAtDesc(appointmentId);
        if (!recentVitals.isEmpty()) {
            Vitals lastVitals = recentVitals.getFirst();
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
            if (lastVitals.getRecordedAt().isAfter(fiveMinutesAgo)) {
                log.warn("Vitals were recorded {} seconds ago for appointment {}",
                    java.time.Duration.between(lastVitals.getRecordedAt(), LocalDateTime.now()).getSeconds(),
                    appointmentId);
                // This is a warning, not blocking - allowing multiple measurements in quick succession
            }
        }

        // 4. Validate vitals data using custom validator
        List<String> validationErrors = vitalsValidator.validate(request);
        if (!validationErrors.isEmpty()) {
            String errorMessage = "Vitals validation failed: " + String.join("; ", validationErrors);
            log.error(errorMessage);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
        }

        // 5. Generate warnings for concerning values (logged but not blocking)
        List<String> warnings = vitalsValidator.generateWarnings(request);
        if (!warnings.isEmpty()) {
            log.warn("Vitals recorded with concerning values for appointment {}: {}",
                appointmentId, String.join("; ", warnings));
        }

        // 6. Map request to entity
        Vitals vitals = appointmentMapper.toVitalsEntity(request);
        vitals.setAppointment(appointment);

        // 7. Set recorded by info
        String username = currentUser.getDisplayNameOrFallback();
        vitals.setRecordedBy(currentUser.getCurrentUser().getId());
        vitals.setRecordedByName(username);

        // 8. Save vitals (entity @PrePersist will calculate BMI automatically)
        Vitals savedVitals = vitalsRepository.save(vitals);
        log.info("Vitals recorded successfully for appointment: {} by user: {}",
            appointmentId, username);

        // 9. Log critical vital signs in audit trail
        if (isCriticalVitals(savedVitals)) {
            log.warn("CRITICAL VITALS RECORDED for appointment {}: HR={}, BP={}/{}, SpO2={}, Temp={}",
                appointmentId,
                savedVitals.getHeartRate(),
                savedVitals.getSystolicBp(),
                savedVitals.getDiastolicBp(),
                savedVitals.getSpo2(),
                savedVitals.getTemperature());
        }

        return appointmentMapper.toVitalsResponse(savedVitals);
    }

    /**
     * Get vitals by appointment ID
     */
    @Transactional(readOnly = true)
    public List<VitalsResponse> getVitalsByAppointment(Long appointmentId) {
        log.info("Fetching vitals for appointment: {}", appointmentId);

        List<Vitals> vitals = vitalsRepository.findByAppointmentIdOrderByRecordedAtDesc(appointmentId);
        return appointmentMapper.toVitalsResponseList(vitals);
    }

    /**
     * Define allowed logical status transitions. Terminal states: COMPLETED, CANCELLED, NO_SHOW
     */
    private boolean isValidStatusTransition(AppointmentStatus current, AppointmentStatus next) {
        if (current == next) return true; // no-op

        // Terminal states cannot transition to anything else
        if (current == AppointmentStatus.COMPLETED || current == AppointmentStatus.CANCELLED || current == AppointmentStatus.NO_SHOW) {
            return false;
        }

        // Allowed forward transitions map
        Map<AppointmentStatus, Set<AppointmentStatus>> allowed = new EnumMap<>(AppointmentStatus.class);

        allowed.put(AppointmentStatus.DRAFT, EnumSet.of(AppointmentStatus.CONFIRMED, AppointmentStatus.CANCELLED));
        allowed.put(AppointmentStatus.CONFIRMED, EnumSet.of(AppointmentStatus.WAITING, AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW));
        allowed.put(AppointmentStatus.WAITING, EnumSet.of(AppointmentStatus.IN_CONSULTATION, AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW));
        allowed.put(AppointmentStatus.IN_CONSULTATION, EnumSet.of(AppointmentStatus.TO_INVOICE, AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED));
        allowed.put(AppointmentStatus.TO_INVOICE, EnumSet.of(AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED));
        // For other statuses not present in map, allow only certain reasonable moves
        allowed.putIfAbsent(AppointmentStatus.COMPLETED, EnumSet.noneOf(AppointmentStatus.class));
        allowed.putIfAbsent(AppointmentStatus.CANCELLED, EnumSet.noneOf(AppointmentStatus.class));
        allowed.putIfAbsent(AppointmentStatus.NO_SHOW, EnumSet.noneOf(AppointmentStatus.class));

        Set<AppointmentStatus> allowedNext = allowed.getOrDefault(current, Collections.emptySet());
        return allowedNext.contains(next);
    }

    /**
     * Check if vitals contain any critical values that require immediate attention
     */
    private boolean isCriticalVitals(Vitals vitals) {
        // Critical heart rate (bradycardia < 40 or tachycardia > 150)
        if (vitals.getHeartRate() != null &&
            (vitals.getHeartRate() < 40 || vitals.getHeartRate() > 150)) {
            return true;
        }

        // Critical blood pressure (severe hypotension or hypertension)
        if (vitals.getSystolicBp() != null && vitals.getDiastolicBp() != null) {
            if (vitals.getSystolicBp() < 80 || vitals.getSystolicBp() > 180 ||
                vitals.getDiastolicBp() < 50 || vitals.getDiastolicBp() > 110) {
                return true;
            }
        }

        // Critical oxygen saturation (< 90%)
        if (vitals.getSpo2() != null && vitals.getSpo2() < 90) {
            return true;
        }

        // Critical temperature (severe fever or hypothermia)
        if (vitals.getTemperature() != null) {
            String unit = vitals.getTemperatureUnit();
            if ("F".equals(unit)) {
                if (vitals.getTemperature() < 95 || vitals.getTemperature() > 104) {
                    return true;
                }
            } else if ("C".equals(unit)) {
                if (vitals.getTemperature() < 35 || vitals.getTemperature() > 40) {
                    return true;
                }
            }
        }

        // Critical respiratory rate (< 8 or > 30)
        if (vitals.getRespiratoryRate() != null &&
            (vitals.getRespiratoryRate() < 8 || vitals.getRespiratoryRate() > 30)) {
            return true;
        }

        // Critical blood sugar (severe hypoglycemia or hyperglycemia)
        return vitals.getRandomBloodSugar() != null &&
                (vitals.getRandomBloodSugar() < 50 || vitals.getRandomBloodSugar() > 400);
    }

    /**
     * Record examination for an appointment
     */
    public ExaminationResponse recordExamination(Long appointmentId, ExaminationRequest request) {
        log.info("Recording examination for appointment: {}", appointmentId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with ID: " + appointmentId));

        // Check if examination already exists
        AppointmentExamination examination = examinationRepository.findByAppointmentId(appointmentId)
                .orElse(new AppointmentExamination());

        // Map request to entity
        examination.setAppointment(appointment);
        examination.setChiefComplaint(request.getChiefComplaint());
        examination.setHistoryPresentIllness(request.getHistoryPresentIllness());
        examination.setSymptoms(request.getSymptoms());
        examination.setGeneralAppearance(request.getGeneralAppearance());
        examination.setCardiovascularSystem(request.getCardiovascularSystem());
        examination.setRespiratorySystem(request.getRespiratorySystem());
        examination.setGastrointestinalSystem(request.getGastrointestinalSystem());
        examination.setCentralNervousSystem(request.getCentralNervousSystem());
        examination.setMusculoskeletalSystem(request.getMusculoskeletalSystem());
        examination.setExaminationFindings(request.getExaminationFindings());
        examination.setVitalsReviewed(request.getVitalsReviewed());
        examination.setPrimaryDiagnosis(request.getPrimaryDiagnosis());
        examination.setPrimaryDiagnosisIcd10(request.getPrimaryDiagnosisIcd10());
        examination.setDifferentialDiagnosis(request.getDifferentialDiagnosis());
        examination.setTreatmentPlan(request.getTreatmentPlan());
        examination.setAdvice(request.getAdvice());
        examination.setFollowUpDate(request.getFollowUpDate());
        examination.setFollowUpInstructions(request.getFollowUpInstructions());
        examination.setMedicalHistoryReviewed(request.getMedicalHistoryReviewed());
        examination.setMedicalHistoryUpdated(request.getMedicalHistoryUpdated());
        examination.setMedicalHistoryUpdateNotes(request.getMedicalHistoryUpdateNotes());

        // Set examined by info
        String username = getUsername();
        examination.setExaminedBy(currentUser.getCurrentUser().getId());
        examination.setExaminedByName(username);

        AppointmentExamination savedExamination = examinationRepository.save(examination);
        log.info("Examination saved successfully for appointment: {}", appointmentId);

        return appointmentMapper.toExaminationResponse(savedExamination);
    }

    /**
     * Get examination by appointment ID
     */
    @Transactional(readOnly = true)
    public ExaminationResponse getExaminationByAppointment(Long appointmentId) {
        log.info("Fetching examination for appointment: {}", appointmentId);

        AppointmentExamination examination = examinationRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Examination not found for appointment: " + appointmentId));

        return appointmentMapper.toExaminationResponse(examination);
    }

    /**
     * Helper method to get username with fallback
     */
    private String getUsername() {
        String username = "system";
        try {
            if (currentUser != null && currentUser.getCurrentUser() != null) {
                String fullName = currentUser.getCurrentUser().getFullName();
                if (fullName != null && !fullName.isEmpty()) {
                    username = fullName;
                }
            }
        } catch (Exception e) {
            // fallback to system
        }
        return username;
    }

}
