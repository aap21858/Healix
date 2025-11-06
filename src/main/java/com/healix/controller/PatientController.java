package com.healix.controller;

import com.healix.api.PatientManagementApi;
import com.healix.mapper.PageMapper;
import com.healix.model.*;
import com.healix.service.CsvPatientService;
import com.healix.service.PatientService;
import com.healix.util.PatientValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
// @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
public class PatientController implements PatientManagementApi {

    private final PatientService patientService;
    private final PageMapper pageMapper;
    private final CsvPatientService csvPatientService;

    @Override
    public ResponseEntity<BatchPatientResponse> registerPatient(
            List<PatientRegistrationRequest> requests) {

        log.info("Batch patient registration request received for {} patients",
                requests.size());

        // Validate insurance fields for each request
        List<String> validationErrors = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            PatientRegistrationRequest req = requests.get(i);
            if (req != null) {
                List<String> missingFields = PatientValidator.validateInsurance(req.getInsurance());
                if (!missingFields.isEmpty()) {
                    validationErrors.add("request[" + i + "] missing: " + String.join(",", missingFields));
                }
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException("Validation failed: " + String.join("; ", validationErrors));
        }

        BatchPatientResponse response = patientService.registerPatients(requests);

        // Return 207 Multi-Status if there are any failures
        // Return 201 Created if all succeeded
        if (response.getFailureCount() > 0) {
            log.warn("Batch registration completed with {} failures",
                    response.getFailureCount());
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(response);
        } else {
            log.info("All {} patients registered successfully",
                    response.getSuccessCount());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
    }

    @Override
    public ResponseEntity<PatientResponse> getPatientById(Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @Override
    public ResponseEntity<PatientPageResponse> getAllPatients(Integer page, Integer size, String sort) {
        Pageable pageable = createPageable(page, size, sort);
        Page<PatientResponse> patientPage = patientService.getAllPatients(pageable);
        PatientPageResponse response = pageMapper.toPatientPageResponse(patientPage);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<PatientResponse> updatePatient(Long id, PatientRegistrationRequest request) {
        // Validate insurance fields for update
        if (request != null) {
            List<String> missingFields = PatientValidator.validateInsurance(request.getInsurance());
            if (!missingFields.isEmpty()) {
                throw new IllegalArgumentException("Validation failed: missing " + String.join(",", missingFields));
            }
        }

        assert request != null;
        return ResponseEntity.ok(patientService.updatePatient(id, request));
    }

    @Override
    public ResponseEntity<Void> deletePatient(Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<PatientResponse> getPatientByPatientId(String patientId) {
        return ResponseEntity.ok(patientService.getPatientByPatientId(patientId));
    }

    @Override
    public ResponseEntity<PatientPageResponse> searchPatients(String query, Integer page, Integer size) {
        Pageable pageable = createPageable(page, size, null);
        Page<PatientResponse> patientPage = patientService.searchPatients(query, pageable);
        PatientPageResponse response = pageMapper.toPatientPageResponse(patientPage);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CsvUploadResponse> uploadPatientsCsv(MultipartFile file) {
        log.info("CSV upload request received: {}", file.getOriginalFilename());
        CsvUploadResponse response = csvPatientService.processCsvFile(file);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Resource> downloadCsvTemplate() {
        log.info("CSV template download requested");

        String csvContent = csvPatientService.generateCsvTemplate();
        ByteArrayResource resource = new ByteArrayResource(
                csvContent.getBytes(StandardCharsets.UTF_8));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=patient_import_template.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(resource.contentLength())
                .body(resource);
    }

    private Pageable createPageable(Integer page, Integer size, String sort) {
        // Helper method to create Pageable from query params
        return Pageable.ofSize(size != null ? size : 20)
                .withPage(page != null ? page : 0);
    }
}