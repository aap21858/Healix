package com.healix.entity;

import com.healix.model.AppointmentStatus;
import com.healix.model.AppointmentType;
import com.healix.model.UrgencyLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String appointmentNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentType appointmentType;

    @Column(nullable = false)
    private LocalDate appointmentDate;

    @Column(nullable = false)
    private LocalTime appointmentTime;

    @Column(nullable = false)
    private Integer duration; // Duration in minutes

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "specialty", length = 100)
    private String specialty;

    @Column(name = "physician_id", nullable = false)
    private Long physicianId;

    @Column(name = "physician_name", length = 255)
    private String physicianName;

    @Column(name = "department_name", length = 255)
    private String departmentName;

    @Column(name = "consultation_room", length = 50)
    private String consultationRoom;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UrgencyLevel urgencyLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AppointmentStatus status;

    @Column(columnDefinition = "TEXT")
    private String chiefComplaint;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Medical History Review (for check-in)
    @Column(name = "medical_history_review_required")
    private Boolean medicalHistoryReviewRequired = false;

    @Column(name = "medical_history_review_notes", columnDefinition = "TEXT")
    private String medicalHistoryReviewNotes;

    // Referral fields
    @Column(name = "referred_from_appointment_id")
    private Long referredFromAppointmentId;

    @Column(name = "referred_to_department_id")
    private Long referredToDepartmentId;

    @Column(name = "referred_to_physician_id")
    private Long referredToPhysicianId;

    // IPD specific fields
    @Column(name = "bed_id")
    private Long bedId;

    @Column(name = "admission_date")
    private LocalDateTime admissionDate;

    @Column(name = "discharge_date")
    private LocalDateTime dischargeDate;

    // Completion Details
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "actual_duration_minutes")
    private Integer actualDurationMinutes;

    // Cancellation/Rescheduling
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_by")
    private Long cancelledBy;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "rescheduled_from_id")
    private Long rescheduledFromId;

    @Column(name = "rescheduled_to_id")
    private Long rescheduledToId;

    // Metadata
    @Column(name = "room_number", length = 20)
    private String roomNumber;

    @Column(name = "equipment_required", columnDefinition = "TEXT")
    private String equipmentRequired;

    @Column(name = "requires_assistance")
    private Boolean requiresAssistance;

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = AppointmentStatus.DRAFT;
        }
        if (urgencyLevel == null) {
            urgencyLevel = UrgencyLevel.NORMAL;
        }
        if (duration == null) {
            duration = 30;
        }
        if (requiresAssistance == null) {
            requiresAssistance = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
