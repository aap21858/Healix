package com.healix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentAuditDTO {
    private Long id;
    private Long appointmentId;
    private String action; // CREATED, STATUS_CHANGED, RESCHEDULED, CANCELLED, COMPLETED
    private String oldStatus;
    private String newStatus;
    private LocalDate oldDate;
    private LocalDate newDate;
    private LocalTime oldTime;
    private LocalTime newTime;
    private String reason;
    private String notes;
    private Long changedBy;
    private String changedByName;
    private LocalDateTime changedAt;
}

