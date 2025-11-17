package com.healix.service;

import com.healix.entity.Patient;
import com.healix.exception.ResourceNotFoundException;
import com.healix.exception.DuplicateResourceException;
import com.healix.model.*;
import com.healix.repository.PatientRepository;
import com.healix.util.CurrentUser;
import com.healix.mapper.PatientMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;
    private final CurrentUser currentUser;
    private final PatientMapper patientMapper;

    private String clinicCode() {
        return "SNG";
    }

    private String generatePatientId(Long dbId){
        String year = String.valueOf(LocalDate.now().getYear());
        return "%s%s%03d".formatted(clinicCode(), year, dbId);
    }

    /**
     * Register multiple patients in batch
     * @param requests List of patient registration requests
     * @return BatchPatientResponse with success and failure details
     */
    public BatchPatientResponse registerPatients(List<PatientRegistrationRequest> requests) {
        log.info("Starting batch patient registration for {} patients", requests.size());

        List<PatientResponse> successfulRegistrations = new ArrayList<>();
        List<FailedRegistration> failedRegistrations = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            PatientRegistrationRequest request = requests.get(i);

            try {
                log.debug("Processing patient {}/{}: {} {}",
                        i + 1, requests.size(), request.getFirstName(), request.getLastName());

                // Validate and register patient
                PatientResponse response = registerSinglePatient(request);
                successfulRegistrations.add(response);

                log.info("Successfully registered patient {}: {}",
                        i + 1, response.getPatientId());

            } catch (DuplicateResourceException e) {
                log.warn("Failed to register patient {} due to duplicate: {}",
                        i + 1, e.getMessage());

                FailedRegistration failure = FailedRegistration.builder()
                        .requestIndex(i)
                        .patientData(request)
                        .errorMessage(e.getMessage())
                        .errorCode(determineErrorCode(e))
                        .build();

                failedRegistrations.add(failure);

            } catch (IllegalArgumentException e) {
                log.warn("Failed to register patient {} due to validation error: {}",
                        i + 1, e.getMessage());

                FailedRegistration failure = FailedRegistration.builder()
                        .requestIndex(i)
                        .patientData(request)
                        .errorMessage(e.getMessage())
                        .errorCode(FailedRegistration.ErrorCodeEnum.VALIDATION_ERROR)
                        .build();

                failedRegistrations.add(failure);

            } catch (Exception e) {
                log.error("Failed to register patient {} due to unexpected error: {}",
                        i + 1, e.getMessage(), e);

                FailedRegistration failure = FailedRegistration.builder()
                        .requestIndex(i)
                        .patientData(request)
                        .errorMessage("Internal error: " + e.getMessage())
                        .errorCode(FailedRegistration.ErrorCodeEnum.INTERNAL_ERROR)
                        .build();

                failedRegistrations.add(failure);
            }
        }

        BatchPatientResponse response = BatchPatientResponse.builder()
                .totalRequested(requests.size())
                .successCount(successfulRegistrations.size())
                .failureCount(failedRegistrations.size())
                .successfulRegistrations(successfulRegistrations)
                .failedRegistrations(failedRegistrations)
                .build();

        log.info("Batch registration completed: {} succeeded, {} failed",
                response.getSuccessCount(), response.getFailureCount());

        return response;
    }

    /**
     * Register a single patient (used by both direct registration and CSV import)
     * @param request Patient registration request
     * @return PatientResponse
     * @throws DuplicateResourceException if duplicate found
     * @throws IllegalArgumentException if validation fails
     */
    public PatientResponse registerSinglePatient(PatientRegistrationRequest request) {
        // Check for duplicate mobile number
        if (patientRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new DuplicateResourceException("Patient", "mobileNumber", request.getMobileNumber());
        }

        // Check for duplicate Aadhar number (if provided)
        if (request.getAadharNumber() != null && !request.getAadharNumber().isEmpty() &&
                patientRepository.existsByAadharNumber(request.getAadharNumber())) {
            throw new DuplicateResourceException("Patient", "aadharNumber", request.getAadharNumber());
        }

        // Map request -> entity
        Patient patient = mapToEntity(request);

        // Detach child relations temporarily so they are not inserted before parent has a DB id
        var insurance = patient.getInsurance();
        var medicalHistory = patient.getMedicalHistory();
        patient.setInsurance(null);
        patient.setMedicalHistory(null);

        // Persist parent and flush to obtain generated DB id
        Patient saved = patientRepository.saveAndFlush(patient);

        // Generate and set application patientId using the DB id
        saved.setPatientId(generatePatientId(saved.getId()));

        // Re-attach children and set their parent references so foreign key is populated
        if (insurance != null) {
            insurance.setPatient(saved);
            saved.setInsurance(insurance);
        }
        if (medicalHistory != null) {
            medicalHistory.setPatient(saved);
            saved.setMedicalHistory(medicalHistory);
        }

        // Save again to persist patientId and child rows with non-null patient_id
        Patient finalSaved = patientRepository.save(saved);

        return mapToResponse(finalSaved);
    }

    /**
     * Determine error code from exception
     */
    private FailedRegistration.ErrorCodeEnum determineErrorCode(DuplicateResourceException e) {
        String message = e.getMessage().toLowerCase();
        if (message.contains("mobilenumber")) {
            return FailedRegistration.ErrorCodeEnum.DUPLICATE_MOBILE;
        } else if (message.contains("aadharnumber")) {
            return FailedRegistration.ErrorCodeEnum.DUPLICATE_AADHAR;
        }
        return FailedRegistration.ErrorCodeEnum.INTERNAL_ERROR;
    }

    /**
     * Get patient by ID
     */
    public PatientResponse getPatientById(Long id) {
        log.debug("Fetching patient with id: {}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));

        return mapToResponse(patient);
    }

    /**
     * Get patient by Patient ID
     */
    public PatientResponse getPatientByPatientId(String patientId) {
        log.debug("Fetching patient with patientId: {}", patientId);

        Patient patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "patientId", patientId));

        return mapToResponse(patient);
    }

    /**
     * Update patient
     */
    public PatientResponse updatePatient(Long id, PatientRegistrationRequest request) {
        log.debug("Updating patient with id: {}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));

        // Check for duplicate mobile (excluding current patient)
        if (!patient.getMobileNumber().equals(request.getMobileNumber()) &&
                patientRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new DuplicateResourceException("Patient", "mobileNumber", request.getMobileNumber());
        }

        // Check for duplicate Aadhar (excluding current patient)
        if (request.getAadharNumber() != null &&
                !request.getAadharNumber().equals(patient.getAadharNumber()) &&
                patientRepository.existsByAadharNumber(request.getAadharNumber())) {
            throw new DuplicateResourceException("Patient", "aadharNumber", request.getAadharNumber());
        }

        updateEntityFromRequest(patient, request);
        Patient updatedPatient = patientRepository.save(patient);

        log.info("Patient updated successfully: {}", updatedPatient.getPatientId());
        return mapToResponse(updatedPatient);
    }

    /**
     * Delete (soft delete) patient
     */
    public void deletePatient(Long id) {
        log.debug("Deleting patient with id: {}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));

        patient.setStatus(PatientStatus.INACTIVE);
        patientRepository.save(patient);

        log.info("Patient deactivated successfully: {}", patient.getPatientId());
    }

    /**
     * Activate patient (set status to ACTIVE)
     */
    public PatientResponse activatePatient(Long id) {
        log.debug("Activating patient with id: {}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));

        patient.setStatus(PatientStatus.ACTIVE);
        Patient saved = patientRepository.save(patient);

        log.info("Patient activated successfully: {}", saved.getPatientId());
        return mapToResponse(saved);
    }

    /**
     * Get all patients (paginated)
     */
    public Page<PatientResponse> getAllPatients(Pageable pageable) {
        log.debug("Fetching all patients - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        return patientRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Search patients
     */
    public Page<PatientResponse> searchPatients(String query, Pageable pageable) {
        log.debug("Searching patients with query: {}", query);

        return patientRepository.searchPatients(query, pageable)
                .map(this::mapToResponse);
    }

    private PatientResponse mapToResponse(Patient patient) {
        return patientMapper.toResponse(patient);
    }

    private Patient mapToEntity(PatientRegistrationRequest request) {
        Patient patient = patientMapper.toEntity(request);
        patient.setCreatedBy(currentUser.getCurrentUser().getId());
        // If the request explicitly indicates hasInsurance == false, remove any mapped insurance entity so JPA will delete it
        clearInsuranceWhenNotPresent(request, patient);
        return patient;
    }

    private void updateEntityFromRequest(Patient patient, PatientRegistrationRequest request) {
        patientMapper.updatePatientFromRequest(request, patient);
        // If the request explicitly indicates hasInsurance == false, remove any existing insurance entity
        clearInsuranceWhenNotPresent(request, patient);
    }

    private void clearInsuranceWhenNotPresent(PatientRegistrationRequest request, Patient patient) {
        if (request == null) return;
        if (request.getInsurance() == null) return;

        Boolean hasInsurance = request.getInsurance().getHasInsurance();
        if (hasInsurance != null && !hasInsurance) {
            // Remove the associated PatientInsurance object entirely.
            // Patient entity defines the relationship with `cascade = ALL` and `orphanRemoval = true`,
            // so clearing the reference will cause JPA to delete the child row when the patient is saved.
            if (patient.getInsurance() != null) {
                patient.setInsurance(null);
            }
        }
    }
}
