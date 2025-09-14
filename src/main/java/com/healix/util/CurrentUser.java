package com.healix.util;

import com.healix.entity.Staff;
import com.healix.repository.StaffRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    private final StaffRepository staffRepository;

    public CurrentUser(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    public Staff getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("authentication.getName(): " + authentication.getName());
        String email = authentication.getName();
        return staffRepository.findByEmailId(email)
                .orElseThrow(() -> new IllegalStateException("Staff not found for the given email: " + email));
    }
}
