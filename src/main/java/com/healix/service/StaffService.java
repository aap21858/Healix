package com.healix.service;

import com.healix.entity.Staff;
import com.healix.enums.STAFF_STATUS;
import com.healix.repository.StaffRepository;
import com.healix.util.CurrentUser;
import org.apache.coyote.BadRequestException;
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
        return staffRepository.findAll();
    }

    public Staff getStaffByEmailId(String emailId) throws BadRequestException {
        Optional<Staff> staff =  staffRepository.findByEmailId(emailId);
        if(staff.isPresent() && staff.get().getStatus().equals(STAFF_STATUS.ACTIVE.toString())) return staff.get();
        throw new BadRequestException("No User/email-id found");
    }

    public Staff saveStaff(Staff staff) {
        return staffRepository.save(staff);
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

            return saveStaff(staff);

        }).orElseThrow(() -> new RuntimeException("Staff not found with id " + updatedStaff.getId()));
    }

    public void deleteStaff(Long id) {
        Optional<Staff> optionalStaff = staffRepository.findById(id);
        if (optionalStaff.isEmpty()) {
            throw new RuntimeException("Staff not found with id " + id);
        }
        Staff staff = optionalStaff.get();
        staff.setStatus(STAFF_STATUS.INACTIVE.toString());
        staff.setUpdatedBy(currentUser.getCurrentUser().getUpdatedBy());
        staff.setUpdatedAt(LocalDateTime.now());
        staffRepository.save(staff);
    }

    public Optional<Staff> getStaffById(Long id) {
        return staffRepository.findById(id);
    }
}
