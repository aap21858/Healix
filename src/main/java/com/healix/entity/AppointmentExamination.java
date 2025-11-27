package com.healix.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointment_examination")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentExamination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    // Current Visit Assessment (formerly in "triage")
    @Column(name = "chief_complaint", columnDefinition = "TEXT")
    private String chiefComplaint;

    @Column(name = "history_present_illness", columnDefinition = "TEXT")
    private String historyPresentIllness;

    @Column(name = "symptoms", columnDefinition = "TEXT")
    private String symptoms;

    // General Examination
    @Column(name = "general_appearance", columnDefinition = "TEXT")
    private String generalAppearance;

    // Systemic Examination
    @Column(name = "cardiovascular_system", columnDefinition = "TEXT")
    private String cardiovascularSystem;

    @Column(name = "respiratory_system", columnDefinition = "TEXT")
    private String respiratorySystem;

    @Column(name = "gastrointestinal_system", columnDefinition = "TEXT")
    private String gastrointestinalSystem;

    @Column(name = "central_nervous_system", columnDefinition = "TEXT")
    private String centralNervousSystem;

    @Column(name = "musculoskeletal_system", columnDefinition = "TEXT")
    private String musculoskeletalSystem;

    // Findings
    @Column(name = "examination_findings", columnDefinition = "TEXT")
    private String examinationFindings;

    @Column(name = "vitals_reviewed")
    @Builder.Default
    private Boolean vitalsReviewed = true;

    // Diagnosis
    @Column(name = "primary_diagnosis", length = 255)
    private String primaryDiagnosis;

    @Column(name = "primary_diagnosis_icd10", length = 10)
    private String primaryDiagnosisIcd10;

    @Column(name = "differential_diagnosis", columnDefinition = "TEXT")
    private String differentialDiagnosis;

    // Treatment Plan
    @Column(name = "treatment_plan", columnDefinition = "TEXT")
    private String treatmentPlan;

    @Column(name = "advice", columnDefinition = "TEXT")
    private String advice;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    @Column(name = "follow_up_instructions", columnDefinition = "TEXT")
    private String followUpInstructions;

    // Medical History Review (audit trail)
    @Column(name = "medical_history_reviewed")
    @Builder.Default
    private Boolean medicalHistoryReviewed = true;

    @Column(name = "medical_history_updated")
    @Builder.Default
    private Boolean medicalHistoryUpdated = false;

    @Column(name = "medical_history_update_notes", columnDefinition = "TEXT")
    private String medicalHistoryUpdateNotes;

    // Doctor
    @Column(name = "examined_by", nullable = false)
    private Long examinedBy;

    @Column(name = "examined_by_name", length = 100)
    private String examinedByName;

    @Column(name = "examined_at")
    @Builder.Default
    private LocalDateTime examinedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        examinedAt = LocalDateTime.now();
        if (vitalsReviewed == null) {
            vitalsReviewed = true;
        }
        if (medicalHistoryReviewed == null) {
            medicalHistoryReviewed = true;
        }
        if (medicalHistoryUpdated == null) {
            medicalHistoryUpdated = false;
        }
    }
}

