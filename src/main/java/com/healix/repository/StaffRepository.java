package com.healix.repository;

import com.healix.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByEmailId(String email);
    Optional<Staff> findByToken(String token);
    Optional<Staff> findByContactNumber(String contactNumber);

    // For validation: check if another staff (different id) already uses email/contact
    boolean existsByEmailIdAndIdNot(String email, Long id);
    boolean existsByContactNumberAndIdNot(String contactNumber, Long id);
}
