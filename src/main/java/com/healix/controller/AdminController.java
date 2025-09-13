package com.healix.controller;

import com.healix.entity.Staff;
import com.healix.enums.STAFF_STATUS;
import com.healix.repository.StaffRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final StaffRepository staffRepository;
    @Value("${frontend.base.url}")
    private String frontEndBaseUrl;

    public AdminController(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Staff staffRequest) {
        if (staffRepository.findByEmailId(staffRequest.getEmailId()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        String token = UUID.randomUUID().toString();
        staffRequest.setEmailId(staffRequest.getEmailId());
        staffRequest.setToken(token);
        staffRequest.setStatus(String.valueOf(STAFF_STATUS.PENDING));
        staffRequest.setCreatedAt(LocalDateTime.now());
        staffRequest.setTokenCreatedAt(LocalDateTime.now());
        staffRequest.setPassword(null);

        staffRepository.save(staffRequest);

        String activationLink = frontEndBaseUrl + "/set-password?token=" + token;

//        emailService.sendEmail(staffRequest.getEmail(),
//                "Activate your account",
//                "Hi " + staffRequest.getFullName() +
//                        ",\n\nPlease click the link below to activate your account:\n" + activationLink);

        log.info("activation link : {} ", activationLink);

        return ResponseEntity.ok("Activation email sent to " + staffRequest.getEmailId());
    }
}

