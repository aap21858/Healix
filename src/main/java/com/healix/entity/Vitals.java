package com.healix.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "vitals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vitals {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Column
    private Double weight; // in kg

    @Column
    private Double height; // in cm

    @Column(name = "head_circumference")
    private Double headCircumference; // in cm

    @Column
    private Double temperature;

    @Column(name = "temperature_unit", length = 1)
    private String temperatureUnit; // F or C

    @Column(name = "heart_rate")
    private Integer heartRate; // bpm

    @Column(name = "respiratory_rate")
    private Integer respiratoryRate; // breaths/min

    @Column(name = "systolic_bp")
    private Integer systolicBp; // mmHg

    @Column(name = "diastolic_bp")
    private Integer diastolicBp; // mmHg

    @Column
    private Double spo2; // oxygen saturation %

    @Column(name = "random_blood_sugar")
    private Double randomBloodSugar; // mg/dL

    @Column
    private Double bmi;

    @Column(name = "bmi_status", length = 20)
    private String bmiStatus; // Underweight, Normal, Overweight, Obese

    @Column(name = "pain_level")
    private Integer painLevel; // 0-10 scale

    @ElementCollection
    @CollectionTable(name = "vital_symptoms", joinColumns = @JoinColumn(name = "vital_id"))
    @Column(name = "symptom")
    private List<String> symptoms;

    @Column(name = "recorded_by")
    private Long recordedBy;

    @Column(name = "recorded_by_name", length = 255)
    private String recordedByName;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() {
        recordedAt = LocalDateTime.now();
        if (temperatureUnit == null) {
            temperatureUnit = "F";
        }
        // Calculate BMI if weight and height are provided
        if (weight != null && height != null && height > 0) {
            double heightInMeters = height / 100.0;
            bmi = weight / (heightInMeters * heightInMeters);
            bmiStatus = calculateBmiStatus(bmi);
        }
    }

    private String calculateBmiStatus(Double bmi) {
        if (bmi < 18.5) return "Underweight";
        else if (bmi < 25) return "Normal";
        else if (bmi < 30) return "Overweight";
        else return "Obese";
    }
}

