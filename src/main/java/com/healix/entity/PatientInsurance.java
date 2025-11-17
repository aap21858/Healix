package com.healix.entity;

import com.healix.model.InsuranceType;
import com.healix.model.Relationship;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_insurance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientInsurance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "has_insurance", nullable = false)
    @Builder.Default
    private Boolean hasInsurance = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "insurance_type", length = 20)
    private InsuranceType insuranceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheme_id")
    private DropdownLookup scheme;

    @Column(name = "policy_card_number", length = 50)
    private String policyCardNumber;

    @Column(name = "insurance_provider", length = 100)
    private String insuranceProvider;

    @Column(name = "policy_holder_name", length = 100)
    private String policyHolderName;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_to_holder", length = 20)
    private Relationship relationshipToHolder;

    @Column(name = "policy_expiry_date")
    private LocalDate policyExpiryDate;

    @Column(name = "insurance_card_front_url", length = 500)
    private String insuranceCardFrontUrl;

    @Column(name = "insurance_card_back_url", length = 500)
    private String insuranceCardBackUrl;

    @Column(name = "pmjay_card_url", length = 500)
    private String pmjayCardUrl;

    @Column(name = "claim_amount_limit")
    private BigDecimal claimAmountLimit;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}