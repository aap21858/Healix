package com.healix.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "triage")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Triage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Column(columnDefinition = "TEXT")
    private String chiefComplaints;

    @Column(columnDefinition = "TEXT")
    private String historyPresentIllness;

    @Column(columnDefinition = "TEXT")
    private String pastMedicalHistory;

    @Column(columnDefinition = "TEXT")
    private String familyHistory;

    @Column(columnDefinition = "TEXT")
    private String allergies;

    @Column(columnDefinition = "TEXT")
    private String currentMedications;

    @Column(columnDefinition = "TEXT")
    private String socialHistory;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "recorded_by")
    private Long recordedBy;

    @Column(name = "recorded_by_name", length = 255)
    private String recordedByName;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() {
        recordedAt = LocalDateTime.now();
    }
}

