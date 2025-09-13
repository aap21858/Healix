package com.healix.controller;

import com.healix.entity.Staff;
import com.healix.service.StaffService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping("/")
    public ResponseEntity<?> getAllStaffDetails() {
        try {
            return ResponseEntity.ok(staffService.getAllStaffDetails());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Something went wrong while fetching staff details");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStaffDetail(@PathVariable String id, @RequestBody Staff staff) {
        try{
            staffService.updateStaff(Long.valueOf(id), staff);
            return ResponseEntity.ok("Record Updated for " + staff.getFullName() + "successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStaff(@PathVariable Long id) {
        staffService.deleteStaff(id);
        return ResponseEntity.ok("Record Deleted Successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Staff> getStaff(@PathVariable Long id) {
        return staffService.getStaffById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
