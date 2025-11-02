package com.healix.controller;

import com.healix.entity.Staff;
import com.healix.enums.STAFF_STATUS;
import com.healix.repository.StaffRepository;
import com.healix.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    private final AuthService authService;
    @Value("${frontend.base.url}")
    private String frontEndBaseUrl;

    public AdminController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody Staff staffRequest) {
        String token = UUID.randomUUID().toString();
        authService.registerNewStaff(token, staffRequest);

        String activationLink = frontEndBaseUrl + "/set-password?token=" + token;
        log.info("activation link : {} ", activationLink);

        return ResponseEntity.ok("Activation email sent to " + staffRequest.getEmailId());
    }
}

