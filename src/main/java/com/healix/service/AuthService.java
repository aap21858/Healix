package com.healix.service;

import com.healix.dto.LoginRequest;
import com.healix.dto.LoginResponse;
import com.healix.entity.Staff;
import com.healix.enums.STAFF_STATUS;
import com.healix.repository.StaffRepository;
import com.healix.util.CurrentUser;
import lombok.extern.slf4j.Slf4j;
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
    @Value("${frontend.base.url}")
    private String frontEndBaseUrl;

    public AuthService(StaffRepository userRepo, JwtService jwtService, PasswordEncoder passwordEncoder, StaffRepository staffRepository, CurrentUser currentUser) {
        this.userRepo = userRepo;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.staffRepository = staffRepository;
        this.currentUser = currentUser;
    }

    public LoginResponse login(LoginRequest request) {
        Staff staff = getStaffByEmailId(request.emailId());
        if (!passwordEncoder.matches(request.password(), staff.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(staff);
        return new LoginResponse(token, staff.getRoles());
    }

    public Staff getStaffByEmailId(String emailId) {
        Optional<Staff> staff =  userRepo.findByEmailId(emailId);
        if(staff.isPresent() && staff.get().getStatus().equals(STAFF_STATUS.ACTIVE.toString())) return staff.get();
        throw new RuntimeException("No User/email-id found");
    }

    public void activateUser(String token) {
        staffRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid activation token"));

    }

    public boolean validate(String token) {
        String emailId = jwtService.extractEmailId(token);
        Optional<Staff> staff = staffRepository.findByEmailId(emailId);

        if (staff.isPresent()) {
            return jwtService.validateToken(token, emailId);
        }

        return false;
    }

    public Optional<Staff> getStaffDetailsByActivationToken(String activationToken) {
        return staffRepository.findByToken(activationToken);
    }

    public void setPasswordForNewStaff(String password, Staff staff) {
        staff.setPassword(passwordEncoder.encode(password));
        staff.setStatus(String.valueOf(STAFF_STATUS.ACTIVE));
        staff.setCreatedBy(currentUser.getCurrentUser().getId());
        staff.setCreatedAt(LocalDateTime.now());
        staff.setToken(null);
        staff.setTokenCreatedAt(null);

        staffRepository.save(staff);
    }

    public void resetPasswordLinkIfPresent( String emailId) {
        Staff staff  = getStaffByEmailId(emailId);
        String token = UUID.randomUUID().toString();
        staff.setToken(token);
        staff.setTokenCreatedAt(LocalDateTime.now());
        staffRepository.save(staff);
        String updatePasswordLink = frontEndBaseUrl + "/set-password?token=" + token;

//        emailService.sendEmail(staffRequest.getEmail(),
//                "Activate your account",
//                "Hi " + staffRequest.getFullName() +
//                        ",\n\nPlease click the link below to activate your account:\n" + updatePasswordLink);

        log.info("activation link : {} ", updatePasswordLink);
    }
}
