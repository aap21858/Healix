package com.healix.entity;

import com.healix.model.BloodGroup;
import com.healix.model.Gender;
import com.healix.model.PatientStatus;
import com.healix.model.PreferredContactMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "patients", indexes = {
        @Index(name = "idx_patient_mobile", columnList = "mobile_number"),
        @Index(name = "idx_patient_aadhar", columnList = "aadhar_number"),
        @Index(name = "idx_patient_id", columnList = "patient_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", unique = true, nullable = false, length = 20)
    private String patientId;

    // Personal Information
    @NotBlank(message = "First name is required")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", length = 15)
    private BloodGroup bloodGroup;

    @Pattern(regexp = "^$|^[0-9]{12}$", message = "Aadhar number must be 12 digits or empty")
    @Column(name = "aadhar_number", unique = true, length = 12)
    private String aadharNumber;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    // Contact Information
    @NotBlank(message = "Mobile number is required")
    @Length(min = 8, max = 12 , message = "Mobile number must be a valid Indian mobile number")
    @Pattern(
            regexp = "^[0-9]*$",
            message = "Contact number must be in digits"
    )
    private String mobileNumber;

    @Email(message = "Invalid email format")
    @Column(name = "email_id", length = 100)
    private String emailId;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_contact_method", length = 20)
    private PreferredContactMethod preferredContactMethod;

    // Address
    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @NotBlank(message = "City is required")
    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "state", length = 50)
    @Builder.Default
    private String state = "Maharashtra";

    @NotBlank(message = "Pin code is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Pin code must be 6 digits")
    @Column(name = "pin_code", nullable = false, length = 6)
    private String pinCode;

    // Relationships
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<PatientEmergencyContact> emergencyContacts = new HashSet<>();

    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PatientInsurance insurance;

    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PatientMedicalHistory medicalHistory;

    // Audit fields
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 15)
    @Builder.Default
    private PatientStatus status = PatientStatus.ACTIVE;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // Calculated field
    @Transient
    public int getAge() {
        if (dateOfBirth == null) {
            return 0;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public void setEmergencyContacts(Set<PatientEmergencyContact> contacts) {
        // Clear existing contacts
        if (this.emergencyContacts != null) {
            this.emergencyContacts.clear();
            if (contacts != null) {
                contacts.forEach(this::addEmergencyContact);
            }
        }
    }

    public void addEmergencyContact(PatientEmergencyContact contact) {
        if (emergencyContacts == null) {
            emergencyContacts = new HashSet<>();
        }
        emergencyContacts.add(contact);
        contact.setPatient(this);
    }

    public void removeEmergencyContact(PatientEmergencyContact contact) {
        if (emergencyContacts != null) {
            emergencyContacts.remove(contact);
            contact.setPatient(null);
        }
    }

    public void setInsurance(PatientInsurance insurance) {
        this.insurance = insurance;
        if (insurance != null) {
            insurance.setPatient(this);
        }
    }

    public void setMedicalHistory(PatientMedicalHistory history) {
        this.medicalHistory = history;
        if (history != null) {
            history.setPatient(this);
        }
    }
}