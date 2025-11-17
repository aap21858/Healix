package com.healix.service;

import com.healix.entity.Appointment;
import com.healix.entity.Patient;
import com.healix.entity.Triage;
import com.healix.entity.Vitals;
import com.healix.exception.ResourceNotFoundException;
import com.healix.mapper.AppointmentMapper;
import com.healix.model.*;
import com.healix.repository.AppointmentRepository;
import com.healix.repository.PatientRepository;
import com.healix.repository.TriageRepository;
import com.healix.repository.VitalsRepository;
import com.healix.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
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
    private final TriageRepository triageRepository;
    private final VitalsRepository vitalsRepository;
    private final AppointmentMapper appointmentMapper;
    private final CurrentUser currentUser;

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

        // Check if patient already has an appointment on the same date with CONFIRMED status
        boolean hasExistingAppointment = appointmentRepository.existsByPatientIdAndAppointmentDateAndStatus(
                request.getPatientId(),
                request.getAppointmentDate(),
                AppointmentStatus.CONFIRMED
        );

        if (hasExistingAppointment) {
            log.warn("Patient {} already has a confirmed appointment on {}",
                    request.getPatientId(), request.getAppointmentDate());
            throw new IllegalStateException(
                    "Patient already has a confirmed appointment on " + request.getAppointmentDate());
        }

        // Map request to entity
        Appointment appointment = appointmentMapper.toEntity(request);
        appointment.setPatient(patient);
        appointment.setAppointmentNumber(generateAppointmentNumber(request.getAppointmentDate()));
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        // Convert appointmentTime from String to LocalTime if needed
        if (request.getAppointmentTime() != null) {
            appointment.setAppointmentTime(LocalTime.parse(request.getAppointmentTime()));
        }

        // Set user info
        String username = currentUser.getDisplayNameOrFallback();
        appointment.setCreatedBy(username);
        appointment.setUpdatedBy(username);

        // Save appointment
        Appointment savedAppointment = appointmentRepository.save(appointment);
        log.info("Appointment created successfully: {}", savedAppointment.getAppointmentNumber());

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

        // Add triage data
        triageRepository.findByAppointmentId(id)
                .ifPresent(triage -> response.setTriage(appointmentMapper.toTriageResponse(triage)));

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
            appointment.setAppointmentTime(LocalTime.parse(request.getAppointmentTime()));
        }
        if (request.getDuration() != null) {
            appointment.setDuration(request.getDuration());
        }
        if (request.getPhysicianId() != null) {
            appointment.setPhysicianId(request.getPhysicianId());
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
     * Create or update triage data
     */
    public TriageResponse createOrUpdateTriage(Long appointmentId, TriageRequest request) {
        log.info("Creating/updating triage for appointment: {}", appointmentId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with ID: " + appointmentId));

        Triage triage = triageRepository.findByAppointmentId(appointmentId)
                .orElse(new Triage());

        // Map request to entity
        triage.setAppointment(appointment);
        triage.setChiefComplaints(request.getChiefComplaints());
        triage.setHistoryPresentIllness(request.getHistoryPresentIllness());
        triage.setPastMedicalHistory(request.getPastMedicalHistory());
        triage.setFamilyHistory(request.getFamilyHistory());
        triage.setAllergies(request.getAllergies());
        triage.setCurrentMedications(request.getCurrentMedications());
        triage.setSocialHistory(request.getSocialHistory());
        triage.setNotes(request.getNotes());

        // Set recorded by info
        String username = currentUser.getDisplayNameOrFallback();
        triage.setRecordedBy(currentUser.getCurrentUser().getId());
        triage.setRecordedByName(username);

        Triage savedTriage = triageRepository.save(triage);
        log.info("Triage data saved successfully for appointment: {}", appointmentId);

        return appointmentMapper.toTriageResponse(savedTriage);
    }

    /**
     * Get triage data by appointment ID
     */
    @Transactional(readOnly = true)
    public TriageResponse getTriageByAppointment(Long appointmentId) {
        log.info("Fetching triage for appointment: {}", appointmentId);

        Triage triage = triageRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Triage data not found for appointment: " + appointmentId));

        return appointmentMapper.toTriageResponse(triage);
    }

    /**
     * Record vitals
     */
    public VitalsResponse recordVitals(Long appointmentId, VitalsRequest request) {
        log.info("Recording vitals for appointment: {}", appointmentId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with ID: " + appointmentId));

        Vitals vitals = appointmentMapper.toVitalsEntity(request);
        vitals.setAppointment(appointment);

        // Set recorded by info
        String username = currentUser.getDisplayNameOrFallback();
        vitals.setRecordedBy(1L); // TODO: Get actual user ID
        vitals.setRecordedByName(username);

        Vitals savedVitals = vitalsRepository.save(vitals);
        log.info("Vitals recorded successfully for appointment: {}", appointmentId);

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
        allowed.put(AppointmentStatus.CONFIRMED, EnumSet.of(AppointmentStatus.WAITING, AppointmentStatus.IN_TRIAGE, AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW));
        allowed.put(AppointmentStatus.WAITING, EnumSet.of(AppointmentStatus.IN_TRIAGE, AppointmentStatus.IN_CONSULTATION, AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW));
        allowed.put(AppointmentStatus.IN_TRIAGE, EnumSet.of(AppointmentStatus.IN_CONSULTATION, AppointmentStatus.CANCELLED));
        allowed.put(AppointmentStatus.IN_CONSULTATION, EnumSet.of(AppointmentStatus.TO_INVOICE, AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED));
        allowed.put(AppointmentStatus.TO_INVOICE, EnumSet.of(AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED));
        // For other statuses not present in map, allow only certain reasonable moves
        allowed.putIfAbsent(AppointmentStatus.COMPLETED, EnumSet.noneOf(AppointmentStatus.class));
        allowed.putIfAbsent(AppointmentStatus.CANCELLED, EnumSet.noneOf(AppointmentStatus.class));
        allowed.putIfAbsent(AppointmentStatus.NO_SHOW, EnumSet.noneOf(AppointmentStatus.class));

        Set<AppointmentStatus> allowedNext = allowed.getOrDefault(current, Collections.emptySet());
        return allowedNext.contains(next);
    }

}
