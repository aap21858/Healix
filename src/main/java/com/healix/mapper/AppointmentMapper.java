package com.healix.mapper;

import com.healix.entity.Appointment;
import com.healix.entity.Triage;
import com.healix.entity.Vitals;
import com.healix.model.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AppointmentMapper {

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
    Appointment toEntity(AppointmentRequest request);

    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "patientName", expression = "java(getPatientFullName(appointment))")
    AppointmentResponse toResponse(Appointment appointment);

    default String getPatientFullName(Appointment appointment) {
        if (appointment.getPatient() == null) return null;
        return appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appointment", ignore = true)
    @Mapping(target = "recordedAt", ignore = true)
    @Mapping(target = "recordedBy", ignore = true)
    @Mapping(target = "recordedByName", ignore = true)
    Triage toTriageEntity(TriageRequest request);

    @Mapping(target = "appointmentId", source = "appointment.id")
    TriageResponse toTriageResponse(Triage triage);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appointment", ignore = true)
    @Mapping(target = "recordedAt", ignore = true)
    @Mapping(target = "recordedBy", ignore = true)
    @Mapping(target = "recordedByName", ignore = true)
    @Mapping(target = "bmi", ignore = true)
    @Mapping(target = "bmiStatus", ignore = true)
    Vitals toVitalsEntity(VitalsRequest request);

    @Mapping(target = "appointmentId", source = "appointment.id")
    VitalsResponse toVitalsResponse(Vitals vitals);

    List<VitalsResponse> toVitalsResponseList(List<Vitals> vitals);

    default java.time.OffsetDateTime map(java.time.LocalDateTime value) {
        return value == null ? null : value.atOffset(java.time.ZoneOffset.UTC);
    }
}
