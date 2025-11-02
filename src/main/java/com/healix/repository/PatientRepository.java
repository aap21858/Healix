package com.healix.repository;

import com.healix.entity.Patient;
import com.healix.model.PatientStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByPatientId(String patientId);

    Optional<Patient> findByMobileNumber(String mobileNumber);

    Optional<Patient> findByAadharNumber(String aadharNumber);

    boolean existsByMobileNumber(String mobileNumber);

    boolean existsByAadharNumber(String aadharNumber);

    Page<Patient> findByStatus(PatientStatus status, Pageable pageable);

    @Query("SELECT p FROM Patient p WHERE " +
            "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "p.patientId LIKE CONCAT('%', :search, '%') OR " +
            "p.mobileNumber LIKE CONCAT('%', :search, '%')")
    Page<Patient> searchPatients(@Param("search") String search, Pageable pageable);

    @Query("SELECT MAX(p.patientId) FROM Patient p WHERE p.patientId LIKE :prefix")
    Optional<String> findLastPatientIdByPrefix(@Param("prefix") String prefix);
}