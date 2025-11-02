package com.healix.service;

import com.healix.exception.CsvProcessingException;
import com.healix.model.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvPatientService {

    private final PatientService patientService;
    private final Validator validator;

    // CSV column names (case-insensitive)
    private static final String COL_FIRST_NAME = "firstName";
    private static final String COL_LAST_NAME = "lastName";
    private static final String COL_DATE_OF_BIRTH = "dateOfBirth";
    private static final String COL_GENDER = "gender";
    private static final String COL_BLOOD_GROUP = "bloodGroup";
    private static final String COL_MOBILE_NUMBER = "mobileNumber";
    private static final String COL_EMAIL_ID = "emailId";
    private static final String COL_AADHAR_NUMBER = "aadharNumber";
    private static final String COL_ADDRESS_LINE1 = "addressLine1";
    private static final String COL_CITY = "city";
    private static final String COL_DISTRICT = "district";
    private static final String COL_STATE = "state";
    private static final String COL_PIN_CODE = "pinCode";

    private static final Set<String> REQUIRED_COLUMNS = Set.of(
            COL_FIRST_NAME, COL_LAST_NAME, COL_DATE_OF_BIRTH,
            COL_GENDER, COL_MOBILE_NUMBER, COL_CITY, COL_PIN_CODE
    );

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Process uploaded CSV file and register patients
     */
    public CsvUploadResponse processCsvFile(MultipartFile file) {
        log.info("Processing CSV file: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());

        // Validate file
        validateFile(file);

        List<PatientResponse> successfulRegistrations = new ArrayList<>();
        List<CsvValidationError> validationErrors = new ArrayList<>();
        int rowNumber = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            // Parse CSV with case-insensitive headers
            CSVParser csvParser = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .build()
                    .parse(reader);

            // Validate headers
            validateHeaders(csvParser.getHeaderMap());

            // Process each row
            for (CSVRecord record : csvParser) {
                rowNumber++;

                try {
                    // Parse CSV record to PatientRegistrationRequest
                    PatientRegistrationRequest request = parseRecord(record);

                    // Validate request using Bean Validation
                    List<String> errors = validateRequest(request);

                    if (!errors.isEmpty()) {
                        // Add validation errors
                        CsvValidationError error = CsvValidationError.builder()
                                .rowNumber(rowNumber)
                                .rowData(recordToMap(record))
                                .errors(errors)
                                .errorType(CsvValidationError.ErrorTypeEnum.VALIDATION_ERROR)
                                .build();
                        validationErrors.add(error);
                        log.warn("Validation failed for row {}: {}", rowNumber, errors);
                    } else {
                        // Try to register patient
                        try {
                            PatientResponse response = patientService.registerSinglePatient(request);
                            successfulRegistrations.add(response);
                            log.debug("Successfully registered patient from row {}: {}",
                                    rowNumber, response.getPatientId());
                        } catch (Exception e) {
                            // Handle duplicate or other registration errors
                            CsvValidationError error = CsvValidationError.builder()
                                    .rowNumber(rowNumber)
                                    .rowData(recordToMap(record))
                                    .errors(List.of(e.getMessage()))
                                    .errorType(determineCsvErrorType(e))
                                    .build();
                            validationErrors.add(error);
                            log.warn("Failed to register patient from row {}: {}",
                                    rowNumber, e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    // Handle parsing errors
                    CsvValidationError error = CsvValidationError.builder()
                            .rowNumber(rowNumber)
                            .rowData(recordToMap(record))
                            .errors(List.of("Parsing error: " + e.getMessage()))
                            .errorType(CsvValidationError.ErrorTypeEnum.PARSING_ERROR)
                            .build();
                    validationErrors.add(error);
                    log.error("Error parsing row {}: {}", rowNumber, e.getMessage(), e);
                }
            }

        } catch (IOException e) {
            log.error("Error reading CSV file: {}", e.getMessage(), e);
            throw new CsvProcessingException("Failed to read CSV file: " + e.getMessage());
        }

        CsvUploadResponse response = CsvUploadResponse.builder()
                .fileName(file.getOriginalFilename())
                .totalRows(rowNumber)
                .successCount(successfulRegistrations.size())
                .failureCount(validationErrors.size())
                .successfulRegistrations(successfulRegistrations)
                .validationErrors(validationErrors)
                .build();

        log.info("CSV processing completed. Total: {}, Success: {}, Failed: {}",
                rowNumber, successfulRegistrations.size(), validationErrors.size());

        return response;
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CsvProcessingException("Uploaded file is empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new CsvProcessingException("Invalid file type. Only CSV files are allowed");
        }

        // Check file size (e.g., max 10MB)
        long maxSizeBytes = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSizeBytes) {
            throw new CsvProcessingException("File size exceeds maximum limit of 10MB");
        }
    }

    /**
     * Validate CSV headers
     */
    private void validateHeaders(Map<String, Integer> headers) {
        // Get all header names in lowercase
        Set<String> headerNamesLowerCase = headers.keySet().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        // Find missing required columns
        Set<String> missingColumns = REQUIRED_COLUMNS.stream()
                .filter(col -> !headerNamesLowerCase.contains(col.toLowerCase()))
                .collect(Collectors.toSet());

        if (!missingColumns.isEmpty()) {
            throw new CsvProcessingException(
                    "Missing required columns: " + String.join(", ", missingColumns));
        }
    }

    /**
     * Parse CSV record to PatientRegistrationRequest
     */
    private PatientRegistrationRequest parseRecord(CSVRecord record) {
        PatientRegistrationRequest request = new PatientRegistrationRequest();

        // Required fields
        request.setFirstName(getStringValue(record, COL_FIRST_NAME));
        request.setLastName(getStringValue(record, COL_LAST_NAME));
        request.setDateOfBirth(parseDate(record, COL_DATE_OF_BIRTH));
        request.setGender(parseGender(record, COL_GENDER));
        request.setMobileNumber(getStringValue(record, COL_MOBILE_NUMBER));
        request.setCity(getStringValue(record, COL_CITY));
        request.setPinCode(getStringValue(record, COL_PIN_CODE));

        // Optional fields
        request.setBloodGroup(parseBloodGroup(record, COL_BLOOD_GROUP));
        request.setAadharNumber(getStringValue(record, COL_AADHAR_NUMBER));
        request.setEmailId(getStringValue(record, COL_EMAIL_ID));
        request.setAddressLine1(getStringValue(record, COL_ADDRESS_LINE1));
        request.setDistrict(getStringValue(record, COL_DISTRICT));
        request.setState(getStringValue(record, COL_STATE, "Maharashtra"));

        return request;
    }

    /**
     * Validate request using Bean Validation
     */
    private List<String> validateRequest(PatientRegistrationRequest request) {
        List<String> errors = new ArrayList<>();

        Set<ConstraintViolation<PatientRegistrationRequest>> violations =
                validator.validate(request);

        for (ConstraintViolation<PatientRegistrationRequest> violation : violations) {
            errors.add(violation.getPropertyPath() + ": " + violation.getMessage());
        }

        return errors;
    }

    /**
     * Determine CSV error type from exception
     */
    private CsvValidationError.ErrorTypeEnum determineCsvErrorType(Exception e) {
        if (e instanceof com.healix.exception.DuplicateResourceException) {
            return CsvValidationError.ErrorTypeEnum.DUPLICATE_ERROR;
        }
        return CsvValidationError.ErrorTypeEnum.VALIDATION_ERROR;
    }

    /**
     * Get string value from CSV record
     */
    private String getStringValue(CSVRecord record, String column) {
        return getStringValue(record, column, null);
    }

    private String getStringValue(CSVRecord record, String column, String defaultValue) {
        try {
            String value = record.get(column);
            return (value == null || value.trim().isEmpty()) ? defaultValue : value.trim();
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    /**
     * Parse date from CSV
     */
    private LocalDate parseDate(CSVRecord record, String column) {
        String value = getStringValue(record, column);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(column + " is required");
        }

        try {
            return LocalDate.parse(value, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    column + " must be in format yyyy-MM-dd (e.g., 1985-03-15)");
        }
    }

    /**
     * Parse gender enum
     */
    private Gender parseGender(CSVRecord record, String column) {
        String value = getStringValue(record, column);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(column + " is required");
        }

        try {
            return Gender.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    column + " must be one of: MALE, FEMALE, OTHER");
        }
    }

    /**
     * Parse blood group enum
     */
    private BloodGroup parseBloodGroup(CSVRecord record, String column) {
        String value = getStringValue(record, column);
        if (value == null || value.isEmpty()) {
            return null;
        }

        try {
            return BloodGroup.fromValue(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    column + " must be one of: A+, A-, B+, B-, O+, O-, AB+, AB-");
        }
    }

    /**
     * Convert CSV record to Map for error reporting
     */
    private Map<String, String> recordToMap(CSVRecord record) {
        Map<String, String> map = new HashMap<>();
        record.toMap().forEach((key, value) -> map.put(key, value));
        return map;
    }

    /**
     * Generate CSV template
     */
    public String generateCsvTemplate() {
        return String.join(",", List.of(
                COL_FIRST_NAME, COL_LAST_NAME, COL_DATE_OF_BIRTH, COL_GENDER,
                COL_BLOOD_GROUP, COL_MOBILE_NUMBER, COL_EMAIL_ID, COL_AADHAR_NUMBER,
                COL_ADDRESS_LINE1, COL_CITY, COL_DISTRICT, COL_STATE, COL_PIN_CODE
        )) + "\n" +
                "Rahul,Patil,'1985-03-15,MALE,O+,9876543210,rahul@gmail.com,123456789012," +
                "\"123 Main Street, Vishrambag\",Sangli,Sangli,Maharashtra,416416\n";
    }
}