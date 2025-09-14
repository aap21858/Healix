package com.healix.service;

import com.healix.entity.Staff;
import com.healix.enums.STAFF_STATUS;
import com.healix.repository.StaffRepository;
import com.healix.util.CurrentUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StaffService {

    private final StaffRepository staffRepository;
    private final CurrentUser currentUser;

    public StaffService(StaffRepository staffRepository, CurrentUser currentUser) {
        this.staffRepository = staffRepository;
        this.currentUser = currentUser;
    }

    public List<Staff> getAllStaffDetails() {
        return staffRepository.findAll()
                .stream().filter((staff) -> staff.getStatus().equals(STAFF_STATUS.ACTIVE.name()))
                .toList();
    }

    public void updateStaff(Long id, Staff updatedStaff) {
        staffRepository.findById(id).map(staff -> {

            // ✅ Check email uniqueness (exclude current staff id)
            if (staffRepository.existsByEmailIdAndIdNot(updatedStaff.getEmailId(), id)) {
                throw new RuntimeException("Email " + updatedStaff.getEmailId() + " is already used by another staff.");
            }

            // ✅ Check contact number uniqueness (exclude current staff id)
            if (staffRepository.existsByContactNumberAndIdNot(updatedStaff.getContactNumber(), id)) {
                throw new RuntimeException("Contact number " + updatedStaff.getContactNumber() + " is already used by another staff.");
            }

            // ✅ Perform update
            staff.setFullName(updatedStaff.getFullName());
            staff.setEmailId(updatedStaff.getEmailId());
            staff.setRoles(updatedStaff.getRoles());
            staff.setContactNumber(updatedStaff.getContactNumber());
            staff.setUpdatedBy(currentUser.getCurrentUser().getId());
            staff.setUpdatedAt(LocalDateTime.now());

            return staffRepository.save(staff);

        }).orElseThrow(() -> new RuntimeException("Staff not found with id " + updatedStaff.getId()));
    }

    public void deleteStaff(Long id) {
        if (!staffRepository.existsById(id)) {
            throw new RuntimeException("Staff not found with id " + id);
        }
        staffRepository.deleteById(id);
    }

    public Optional<Staff> getStaffById(Long id) {
        return staffRepository.findById(id);
    }
}
