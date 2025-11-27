package com.healix.mapper;

import com.healix.entity.Appointment;
import com.healix.entity.AppointmentExamination;
import com.healix.entity.Staff;
import com.healix.entity.Vitals;
import com.healix.model.*;
import com.healix.repository.StaffRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class AppointmentMapper {

    @Autowired
    protected StaffRepository staffRepository;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appointmentNumber", ignore = true)
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "physicianName", ignore = true)
    @Mapping(target = "departmentName", ignore = true)
    public abstract Appointment toEntity(AppointmentRequest request);

    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "patientName", expression = "java(getPatientFullName(appointment))")
    @Mapping(target = "physicianName", expression = "java(getPhysicianName(appointment))")
    public abstract AppointmentResponse toResponse(Appointment appointment);

    protected String getPatientFullName(Appointment appointment) {
        if (appointment.getPatient() == null) return null;
        return appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName();
    }

    protected String getPhysicianName(Appointment appointment) {
        if (appointment.getPhysicianId() == null) return null;

        // First check if physicianName is already set in the appointment entity
        if (appointment.getPhysicianName() != null && !appointment.getPhysicianName().isEmpty()) {
            return appointment.getPhysicianName();
        }

        // Otherwise, fetch from staff repository
        return staffRepository.findById(appointment.getPhysicianId())
                .map(Staff::getFullName)
                .orElse(null);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appointment", ignore = true)
    @Mapping(target = "examinedAt", ignore = true)
    @Mapping(target = "examinedBy", ignore = true)
    @Mapping(target = "examinedByName", ignore = true)
    public abstract AppointmentExamination toExaminationEntity(ExaminationRequest request);

    @Mapping(target = "appointmentId", source = "appointment.id")
    public abstract ExaminationResponse toExaminationResponse(AppointmentExamination examination);

    // Vitals Mapping
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appointment", ignore = true)
    @Mapping(target = "recordedAt", ignore = true)
    @Mapping(target = "recordedBy", ignore = true)
    @Mapping(target = "recordedByName", ignore = true)
    @Mapping(target = "bmi", ignore = true)
    @Mapping(target = "bmiStatus", source = "bmiStatus")
    public abstract Vitals toVitalsEntity(VitalsRequest request);

    @Mapping(target = "appointmentId", source = "appointment.id")
    @Mapping(target = "bmiStatus", expression = "java(mapBmiStatus(vitals.getBmiStatus()))")
    public abstract VitalsResponse toVitalsResponse(Vitals vitals);

    public abstract List<VitalsResponse> toVitalsResponseList(List<Vitals> vitals);

    // Type conversion helpers for MapStruct

    protected String map(VitalsRequest.BmiStatusEnum statusEnum) {
        return statusEnum == null ? null : statusEnum.getValue();
    }

    protected VitalsResponse.BmiStatusEnum mapBmiStatus(String value) {
        if (value == null) {
            return null;
        }
        return switch (value) {
            case "Underweight" -> VitalsResponse.BmiStatusEnum.UNDERWEIGHT;
            case "Normal" -> VitalsResponse.BmiStatusEnum.NORMAL;
            case "Overweight" -> VitalsResponse.BmiStatusEnum.OVERWEIGHT;
            case "Obese" -> VitalsResponse.BmiStatusEnum.OBESE;
            default -> null;
        };
    }

    // LocalDateTime -> OffsetDateTime (for entity to DTO)
    protected java.time.OffsetDateTime map(java.time.LocalDateTime value) {
        return value == null ? null : value.atOffset(java.time.ZoneOffset.UTC);
    }

    // OffsetDateTime -> LocalDateTime (for DTO to entity)
    protected java.time.LocalDateTime map(java.time.OffsetDateTime value) {
        return value == null ? null : value.toLocalDateTime();
    }

    // String -> LocalTime (for DTO to entity, e.g., appointmentTime)
    public java.time.LocalTime map(String value) {
        return (value == null || value.isEmpty()) ? null : java.time.LocalTime.parse(value);
    }

    // LocalTime -> String (for entity to DTO, e.g., appointmentTime)
    protected String map(java.time.LocalTime value) {
        return value == null ? null : value.toString();
    }
}
