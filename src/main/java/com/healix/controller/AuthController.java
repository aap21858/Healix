package com.healix.controller;

import com.healix.dto.LoginRequest;
import com.healix.dto.LoginResponse;
import com.healix.dto.SetPasswordRequest;
import com.healix.entity.Staff;
import com.healix.enums.STAFF_STATUS;
import com.healix.service.AuthService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/activate")
    public ResponseEntity<?> activateUser(@RequestParam String token) {
        authService.activateUser(token);

        return ResponseEntity.ok("Valid token"); // frontend will show reset password form
    }


    @PostMapping("/set-password")
    public ResponseEntity<?> setPassword(@RequestBody SetPasswordRequest request) {
        String token = request.token();
        String password = request.password();

        Optional<Staff> staff = authService.getStaffDetailsByActivationToken(token);
        if (staff.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Activation Token");
        if (staff.get().getTokenCreatedAt().plusMinutes(2).isBefore(LocalDateTime.now()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Expired Activation token");

        authService.setPasswordForNewStaff(password, staff.get());
        return ResponseEntity.ok("Password set successfully");
    }

    @GetMapping("/forgot-password")
    public ResponseEntity<?> sendResetPasswordLinkIfPresent(@RequestParam String emailId) {
        try {
            authService.resetPasswordLinkIfPresent(emailId);

            return ResponseEntity.ok("Activation email sent to " + emailId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String tokenHeader) {
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
        }

        String token = tokenHeader.substring(7);
        try {

            if (authService.validate(token)) {
                return ResponseEntity.ok().body("Valid token");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token expired");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
    }
}