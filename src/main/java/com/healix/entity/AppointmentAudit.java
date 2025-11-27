package com.healix.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "appointment_audit")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId;

    @Column(name = "action", nullable = false)
    private String action; // CREATED, STATUS_CHANGED, RESCHEDULED, CANCELLED, COMPLETED

    @Column(name = "old_status")
    private String oldStatus;

    @Column(name = "new_status")
    private String newStatus;

    @Column(name = "old_date")
    private LocalDate oldDate;

    @Column(name = "new_date")
    private LocalDate newDate;

    @Column(name = "old_time")
    private LocalTime oldTime;

    @Column(name = "new_time")
    private LocalTime newTime;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "changed_by")
    private Long changedBy;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    @PrePersist
    protected void onCreate() {
        changedAt = LocalDateTime.now();
    }
}

