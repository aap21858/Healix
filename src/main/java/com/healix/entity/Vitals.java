package com.healix.entity;

import com.healix.model.VitalsRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
    @NotNull(message = "Appointment is required")
    private Appointment appointment;

    @Column(columnDefinition = "DECIMAL(5,2)")
    @DecimalMin(value = "0.1", message = "Weight must be greater than 0 kg")
    @DecimalMax(value = "500.0", message = "Weight cannot exceed 500 kg")
    private Double weight; // in kg

    @Column(columnDefinition = "DECIMAL(5,2)")
    @DecimalMin(value = "10.0", message = "Height must be at least 10 cm")
    @DecimalMax(value = "300.0", message = "Height cannot exceed 300 cm")
    private Double height; // in cm

    @Column(name = "head_circumference", columnDefinition = "DECIMAL(5,2)")
    @DecimalMin(value = "10.0", message = "Head circumference must be at least 10 cm")
    @DecimalMax(value = "100.0", message = "Head circumference cannot exceed 100 cm")
    private Double headCircumference; // in cm

    @Column(columnDefinition = "DECIMAL(5,2)")
    @DecimalMin(value = "25.0", message = "Temperature must be at least 25°")
    @DecimalMax(value = "115.0", message = "Temperature cannot exceed 115°")
    private Double temperature;

    @Column(name = "temperature_unit", length = 1)
    @Pattern(regexp = "^[FC]$", message = "Temperature unit must be either 'F' or 'C'")
    private String temperatureUnit; // F or C

    @Column(name = "heart_rate")
    @Min(value = 20, message = "Heart rate must be at least 20 bpm")
    @Max(value = 300, message = "Heart rate cannot exceed 300 bpm")
    private Integer heartRate; // bpm

    @Column(name = "respiratory_rate", columnDefinition = "DECIMAL(5,2)")
    @DecimalMin(value = "1.0", message = "Respiratory rate must be at least 1 breath/min")
    @DecimalMax(value = "100.0", message = "Respiratory rate cannot exceed 100 breaths/min")
    private Double respiratoryRate; // breaths/min

    @Column(name = "systolic_bp")
    @Min(value = 40, message = "Systolic BP must be at least 40 mmHg")
    @Max(value = 300, message = "Systolic BP cannot exceed 300 mmHg")
    private Integer systolicBp; // mmHg

    @Column(name = "diastolic_bp")
    @Min(value = 20, message = "Diastolic BP must be at least 20 mmHg")
    @Max(value = 200, message = "Diastolic BP cannot exceed 200 mmHg")
    private Integer diastolicBp; // mmHg

    @Column(columnDefinition = "DECIMAL(5,2)")
    @DecimalMin(value = "0.0", message = "SpO2 must be at least 0%")
    @DecimalMax(value = "100.0", message = "SpO2 cannot exceed 100%")
    private Double spo2; // oxygen saturation %

    @Column(name = "random_blood_sugar", columnDefinition = "DECIMAL(6,2)")
    @DecimalMin(value = "0.0", message = "Blood sugar must be non-negative")
    @DecimalMax(value = "1000.0", message = "Blood sugar value seems unrealistic (max 1000 mg/dL)")
    private Double randomBloodSugar; // mg/dL

    @Column(columnDefinition = "DECIMAL(5,2)")
    @PositiveOrZero(message = "BMI must be zero or positive")
    private Double bmi;

    @Column(name = "bmi_status", length = 20)
    @Size(max = 20, message = "BMI status cannot exceed 20 characters")
    private String bmiStatus; // Underweight, Normal, Overweight, Obese

    @Column(name = "pain_level")
    @Min(value = 0, message = "Pain level must be between 0 and 10")
    @Max(value = 10, message = "Pain level must be between 0 and 10")
    private Integer painLevel; // 0-10 scale

    @ElementCollection
    @CollectionTable(name = "vital_symptoms", joinColumns = @JoinColumn(name = "vital_id"))
    @Column(name = "symptom")
    @Size(max = 50, message = "Cannot have more than 50 symptoms")
    private List<@NotBlank(message = "Symptom cannot be blank")
                  @Size(max = 255, message = "Symptom description cannot exceed 255 characters") String> symptoms;

    @Column(name = "recorded_by")
    @NotNull(message = "Recorded by is required")
    @Positive(message = "Recorded by ID must be positive")
    private Long recordedBy;

    @Column(name = "recorded_by_name", length = 255)
    @NotBlank(message = "Recorded by name is required")
    @Size(min = 2, max = 255, message = "Recorded by name must be between 2 and 255 characters")
    private String recordedByName;

    @Column(name = "recorded_at", nullable = false)
    @NotNull(message = "Recorded at timestamp is required")
    @PastOrPresent(message = "Recorded at timestamp cannot be in the future")
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
        if (bmi < 18.5) return VitalsRequest.BmiStatusEnum.UNDERWEIGHT.getValue();
        else if (bmi < 25) return VitalsRequest.BmiStatusEnum.NORMAL.getValue();
        else if (bmi < 30) return VitalsRequest.BmiStatusEnum.OVERWEIGHT.getValue();
        else return VitalsRequest.BmiStatusEnum.OBESE.getValue();
    }
}

