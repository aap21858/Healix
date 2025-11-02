package com.healix.entity;

import com.healix.enums.ROLE;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "Staff")
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email_id")
    @NotBlank(message = "Email ID is required")
    @Email(message = "Invalid email format")
    @Size(max = 140, message = "Email must be at most 140 characters")
    private String emailId;

    @Column(name = "password")
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "StaffRoles", joinColumns = @JoinColumn(name = "staffId"))
    @Column(name = "role")
    @NotEmpty(message = "Role is required")
    private Set<String> roles = new HashSet<>();

    @Column(name = "full_name")
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 120, message = "Full name must be between 2 and 120 characters")
    @Pattern(
            regexp = "^[A-Za-z .'-]+$",
            message = "Full name may only contain letters, spaces, dot, apostrophe, and hyphen"
    )
    private String fullName;

    @Column(name = "contact_number")
    @NotBlank(message = "Contact number is required")
    @Length(min = 8, max = 12 , message = "Contact number must be a valid Indian mobile number")
    @Pattern(
            regexp = "^[0-9]*$",
            message = "Contact number must be in digits"
    )
    private String contactNumber;

    @Column(name = "status")
    private String status;

    @Column(name = "token")
    private String token;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "token_created_at")
    private LocalDateTime tokenCreatedAt;

}
