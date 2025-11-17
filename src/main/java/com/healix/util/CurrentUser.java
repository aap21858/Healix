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
        if (authentication == null) {
            throw new IllegalStateException("No authentication available");
        }
        String email = authentication.getName();
        return staffRepository.findByEmailId(email)
                .orElseThrow(() -> new IllegalStateException("Staff not found for the given email: " + email));
    }

    /**
     * Returns a display name for the currently authenticated user.
     * Falls back to "system" if no user is authenticated or any error occurs.
     */
    public String getDisplayNameOrFallback() {
        String username = "system";
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                // try to obtain staff and use its full name if present
                Staff staff = null;
                try {
                    staff = getCurrentUser();
                } catch (Exception ignored) {
                    // ignore and fallback
                }
                if (staff != null) {
                    String fullName = staff.getFullName();
                    if (fullName != null && !fullName.isEmpty()) {
                        username = fullName;
                    }
                }
            }
        } catch (Exception e) {
            // fallback to system
        }
        return username;
    }
}
