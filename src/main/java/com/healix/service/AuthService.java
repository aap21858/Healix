package com.healix.service;

import com.healix.dto.LoginRequest;
import com.healix.dto.LoginResponse;
import com.healix.entity.Staff;
import com.healix.enums.STAFF_STATUS;
import com.healix.repository.StaffRepository;
import com.healix.util.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@Service
@Slf4j
public class AuthService {
    private final StaffRepository userRepo;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final StaffRepository staffRepository;
    private final CurrentUser currentUser;
    private final StaffService staffService;
    @Value("${frontend.base.url}")
    private String frontEndBaseUrl;

    public AuthService(StaffRepository userRepo, JwtService jwtService, PasswordEncoder passwordEncoder, StaffRepository staffRepository, CurrentUser currentUser, StaffService staffService) {
        this.userRepo = userRepo;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.staffRepository = staffRepository;
        this.currentUser = currentUser;
        this.staffService = staffService;
    }

    public LoginResponse login(LoginRequest request) throws BadRequestException {
        Staff staff = staffService.getStaffByEmailId(request.emailId());
        if (!passwordEncoder.matches(request.password(), staff.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(staff);
        return new LoginResponse(token, staff.getRoles());
    }

    public void activateUser(String token) {
        staffRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid activation token"));

    }

    public boolean validate(String token) throws BadRequestException {
        String emailId = jwtService.extractEmailId(token);
        Optional<Staff> staff = Optional.ofNullable(staffService.getStaffByEmailId(emailId));

        if (staff.isPresent()) {
            return jwtService.validateToken(token, emailId);
        }

        return false;
    }

    public Optional<Staff> getStaffDetailsByActivationToken(String activationToken) {
        return staffRepository.findByToken(activationToken);
    }

    public void registerNewStaff(String token, Staff staffRequest) {
        if (staffRepository.findByEmailId(staffRequest.getEmailId()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        staffRequest.setEmailId(staffRequest.getEmailId());
        staffRequest.setToken(token);
        staffRequest.setStatus(String.valueOf(STAFF_STATUS.PENDING));
        staffRequest.setCreatedBy(currentUser.getCurrentUser().getId());
        staffRequest.setCreatedAt(LocalDateTime.now());
        staffRequest.setTokenCreatedAt(LocalDateTime.now());
        staffRequest.setPassword(null);

        staffService.saveStaff(staffRequest);
    }

    public void setPasswordForNewStaff(String password, Staff staff) {
        staff.setPassword(passwordEncoder.encode(password));
        staff.setStatus(String.valueOf(STAFF_STATUS.ACTIVE));
        staff.setUpdatedBy(staff.getId());
        staff.setUpdatedAt(LocalDateTime.now());
        staff.setToken(null);
        staff.setTokenCreatedAt(null);

        staffService.saveStaff(staff);
    }

    public void resetPasswordLinkIfPresent( String emailId) throws BadRequestException {
        Staff staff  = staffService.getStaffByEmailId(emailId);
        String token = UUID.randomUUID().toString();
        staff.setToken(token);
        staff.setTokenCreatedAt(LocalDateTime.now());
        staffService.saveStaff(staff);
        String updatePasswordLink = frontEndBaseUrl + "/set-password?token=" + token;

//        emailService.sendEmail(staffRequest.getEmail(),
//                "Activate your account",
//                "Hi " + staffRequest.getFullName() +
//                        ",\n\nPlease click the link below to activate your account:\n" + updatePasswordLink);

        log.info("activation link : {} ", updatePasswordLink);
    }
}
