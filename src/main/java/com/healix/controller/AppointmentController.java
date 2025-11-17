package com.healix.controller;

import com.healix.api.AppointmentManagementApi;
import com.healix.mapper.PageMapper;
import com.healix.model.*;
import com.healix.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AppointmentController implements AppointmentManagementApi {

    private final AppointmentService appointmentService;
    private final PageMapper pageMapper;

    @Override
    public ResponseEntity<AppointmentResponse> createAppointment(AppointmentRequest appointmentRequest) {
        log.info("Creating new appointment for patient: {}", appointmentRequest.getPatientId());

        AppointmentResponse response = appointmentService.createAppointment(appointmentRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<AppointmentPageResponse> getAllAppointments(
            Integer page, Integer size, String sort) {

        log.info("Fetching all appointments - page: {}, size: {}", page, size);

        Pageable pageable = createPageable(page, size, sort);
        Page<AppointmentResponse> appointments = appointmentService.getAllAppointments(pageable);

        AppointmentPageResponse response = new AppointmentPageResponse();
        response.setContent(appointments.getContent());
        response.setTotalElements(appointments.getTotalElements());
        response.setTotalPages(appointments.getTotalPages());
        response.setCurrentPage(appointments.getNumber());
        response.setSize(appointments.getSize());

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<AppointmentDetailResponse> getAppointmentById(Long id) {
        log.info("Fetching appointment by ID: {}", id);

        AppointmentDetailResponse response = appointmentService.getAppointmentDetailById(id);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<AppointmentResponse> updateAppointment(
            Long id, AppointmentRequest appointmentRequest) {

        log.info("Updating appointment: {}", id);

        AppointmentResponse response = appointmentService.updateAppointment(id, appointmentRequest);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> cancelAppointment(Long id) {
        log.info("Cancelling appointment: {}", id);

        appointmentService.cancelAppointment(id);

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<AppointmentPageResponse> searchAppointments(
            String patientName,
            Long patientId,
            String contactNumber,
            LocalDate appointmentDate,
            Long physicianId,
            AppointmentStatus status,
            Integer page,
            Integer size) {

        log.info("Searching appointments with filters");

        Pageable pageable = createPageable(page, size, "createdAt,desc");
        Page<AppointmentResponse> appointments = appointmentService.searchAppointments(
                patientId, physicianId, status, appointmentDate, contactNumber, patientName, pageable);

        AppointmentPageResponse response = new AppointmentPageResponse();
        response.setContent(appointments.getContent());
        response.setTotalElements(appointments.getTotalElements());
        response.setTotalPages(appointments.getTotalPages());
        response.setCurrentPage(appointments.getNumber());
        response.setSize(appointments.getSize());

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<AppointmentResponse> updateAppointmentStatus(
            Long id, UpdateAppointmentStatusRequest updateAppointmentStatusRequest) {

        log.info("Updating appointment {} status to: {}", id, updateAppointmentStatusRequest.getStatus());

        AppointmentResponse response = appointmentService.updateAppointmentStatus(
                id, updateAppointmentStatusRequest.getStatus());

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<TriageResponse> createOrUpdateTriage(
            Long appointmentId, TriageRequest triageRequest) {

        log.info("Creating/updating triage for appointment: {}", appointmentId);

        TriageResponse response = appointmentService.createOrUpdateTriage(appointmentId, triageRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<TriageResponse> getTriageByAppointment(Long appointmentId) {
        log.info("Fetching triage for appointment: {}", appointmentId);

        TriageResponse response = appointmentService.getTriageByAppointment(appointmentId);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<VitalsResponse> recordVitals(
            Long appointmentId, VitalsRequest vitalsRequest) {

        log.info("Recording vitals for appointment: {}", appointmentId);

        VitalsResponse response = appointmentService.recordVitals(appointmentId, vitalsRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<List<VitalsResponse>> getVitalsByAppointment(Long appointmentId) {
        log.info("Fetching vitals for appointment: {}", appointmentId);

        List<VitalsResponse> response = appointmentService.getVitalsByAppointment(appointmentId);

        return ResponseEntity.ok(response);
    }

    // Note: Examination, Prescription, Investigation, Referral, Admit, and Discharge methods
    // are placeholders and should be implemented similarly to triage and vitals

    @Override
    public ResponseEntity<ExaminationResponse> recordExamination(
            Long appointmentId, ExaminationRequest examinationRequest) {
        // TODO: Implement examination recording
        log.warn("Examination recording not yet implemented");
        throw new UnsupportedOperationException("Examination recording not yet implemented");
    }

    @Override
    public ResponseEntity<ExaminationResponse> getExaminationByAppointment(Long appointmentId) {
        // TODO: Implement examination retrieval
        log.warn("Examination retrieval not yet implemented");
        throw new UnsupportedOperationException("Examination retrieval not yet implemented");
    }

    @Override
    public ResponseEntity<PrescriptionResponse> createPrescription(
            Long appointmentId, PrescriptionRequest prescriptionRequest) {
        // TODO: Implement prescription creation
        log.warn("Prescription creation not yet implemented");
        throw new UnsupportedOperationException("Prescription creation not yet implemented");
    }

    @Override
    public ResponseEntity<List<PrescriptionResponse>> getPrescriptionsByAppointment(Long appointmentId) {
        // TODO: Implement prescription retrieval
        log.warn("Prescription retrieval not yet implemented");
        throw new UnsupportedOperationException("Prescription retrieval not yet implemented");
    }

    @Override
    public ResponseEntity<PrescriptionResponse> getPrescriptionById(Long id) {
        // TODO: Implement prescription retrieval by ID
        log.warn("Prescription retrieval by ID not yet implemented");
        throw new UnsupportedOperationException("Prescription retrieval by ID not yet implemented");
    }

    @Override
    public ResponseEntity<PrescriptionResponse> updatePrescriptionStatus(
            Long id, UpdatePrescriptionStatusRequest updatePrescriptionStatusRequest) {
        // TODO: Implement prescription status update
        log.warn("Prescription status update not yet implemented");
        throw new UnsupportedOperationException("Prescription status update not yet implemented");
    }

    @Override
    public ResponseEntity<InvestigationOrderResponse> createInvestigationOrder(
            Long appointmentId, InvestigationOrderRequest investigationOrderRequest) {
        // TODO: Implement investigation order creation
        log.warn("Investigation order creation not yet implemented");
        throw new UnsupportedOperationException("Investigation order creation not yet implemented");
    }

    @Override
    public ResponseEntity<List<InvestigationOrderResponse>> getInvestigationsByAppointment(Long appointmentId) {
        // TODO: Implement investigation retrieval
        log.warn("Investigation retrieval not yet implemented");
        throw new UnsupportedOperationException("Investigation retrieval not yet implemented");
    }

    @Override
    public ResponseEntity<InvestigationOrderResponse> updateInvestigationResult(
            Long id, InvestigationResultRequest investigationResultRequest) {
        // TODO: Implement investigation result update
        log.warn("Investigation result update not yet implemented");
        throw new UnsupportedOperationException("Investigation result update not yet implemented");
    }

    @Override
    public ResponseEntity<AppointmentResponse> createReferral(
            Long appointmentId, ReferralRequest referralRequest) {
        // TODO: Implement referral creation
        log.warn("Referral creation not yet implemented");
        throw new UnsupportedOperationException("Referral creation not yet implemented");
    }

    @Override
    public ResponseEntity<AppointmentResponse> admitPatient(
            Long appointmentId, AdmitRequest admitRequest) {
        // TODO: Implement patient admission
        log.warn("Patient admission not yet implemented");
        throw new UnsupportedOperationException("Patient admission not yet implemented");
    }

    @Override
    public ResponseEntity<AppointmentResponse> dischargePatient(
            Long appointmentId, DischargeRequest dischargeRequest) {
        // TODO: Implement patient discharge
        log.warn("Patient discharge not yet implemented");
        throw new UnsupportedOperationException("Patient discharge not yet implemented");
    }

    /**
     * Helper method to create Pageable from request parameters
     */
    private Pageable createPageable(Integer page, Integer size, String sort) {
        int pageNumber = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size > 0) ? Math.min(size, 100) : 20;

        if (sort != null && !sort.trim().isEmpty()) {
            String[] sortParams = sort.split(",");
            String property = sortParams[0];
            Sort.Direction direction = (sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]))
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            return PageRequest.of(pageNumber, pageSize, Sort.by(direction, property));
        }

        return PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}

