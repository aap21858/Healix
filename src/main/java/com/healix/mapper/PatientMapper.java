package com.healix.mapper;

import com.healix.entity.Patient;
import com.healix.entity.PatientEmergencyContact;
import com.healix.model.EmergencyContactRequest;
import com.healix.model.PatientRegistrationRequest;
import com.healix.model.PatientResponse;
import org.mapstruct.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PatientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "emergencyContacts", ignore = true)
    Patient toEntity(PatientRegistrationRequest request);

    @Mapping(target = "age", expression = "java(patient.getAge())")
    PatientResponse toResponse(Patient patient);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PatientEmergencyContact toEmergencyContactEntity(EmergencyContactRequest request);

    @AfterMapping
    default void mapEmergencyContacts(PatientRegistrationRequest request, @MappingTarget Patient patient) {
        if (request.getEmergencyContacts() != null) {
            Set<PatientEmergencyContact> contacts = request.getEmergencyContacts().stream()
                .map(contactRequest -> {
                    PatientEmergencyContact contact = toEmergencyContactEntity(contactRequest);
                    contact.setPatient(patient);
                    contact.setIsPrimary(contactRequest.getIsPrimary() != null ? contactRequest.getIsPrimary() : true);
                    return contact;
                })
                .collect(Collectors.toSet());

            // Clear and add new contacts using the helper method
            if (patient.getEmergencyContacts() == null) {
                patient.setEmergencyContacts(new HashSet<>());
            }
            patient.getEmergencyContacts().clear();
            contacts.forEach(patient::addEmergencyContact);
        } else {
            // If request does not provide emergency contacts, clear only if patient already has contacts
            if (patient.getEmergencyContacts() != null) {
                patient.getEmergencyContacts().clear();
            }
        }
    }

    @AfterMapping
    default void linkEmergencyContacts(@MappingTarget Patient patient) {
        if (patient.getEmergencyContacts() != null) {
            patient.getEmergencyContacts().forEach(contact -> contact.setPatient(patient));
        }
    }

    default void updatePatientFromRequest(PatientRegistrationRequest request, @MappingTarget Patient patient) {
        if (request == null) {
            return;
        }

        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setMobileNumber(request.getMobileNumber());
        patient.setEmailId(request.getEmailId());
        patient.setCity(request.getCity());
        patient.setPinCode(request.getPinCode());
        patient.setPreferredContactMethod(request.getPreferredContactMethod());
        patient.setAddressLine1(request.getAddressLine1());
        patient.setAadharNumber(request.getAadharNumber());

        // Handle emergency contacts using the same logic as in mapEmergencyContacts
        if (request.getEmergencyContacts() != null) {
            Set<PatientEmergencyContact> contacts = request.getEmergencyContacts().stream()
                .map(contactRequest -> {
                    PatientEmergencyContact contact = toEmergencyContactEntity(contactRequest);
                    contact.setPatient(patient);
                    contact.setIsPrimary(contactRequest.getIsPrimary() != null ? contactRequest.getIsPrimary() : true);
                    return contact;
                })
                .collect(Collectors.toSet());

            // Clear and add new contacts using the helper method
            if (patient.getEmergencyContacts() == null) {
                patient.setEmergencyContacts(new HashSet<>());
            }
            patient.getEmergencyContacts().clear();
            contacts.forEach(patient::addEmergencyContact);
        } else {
            if (patient.getEmergencyContacts() != null) {
                patient.getEmergencyContacts().clear();
            }
        }
    }
}
