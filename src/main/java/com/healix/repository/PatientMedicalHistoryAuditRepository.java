package com.healix.repository;

import com.healix.entity.PatientMedicalHistoryAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientMedicalHistoryAuditRepository extends JpaRepository<PatientMedicalHistoryAudit, Long> {

    List<PatientMedicalHistoryAudit> findByPatientIdOrderByChangedAtDesc(Long patientId);

    List<PatientMedicalHistoryAudit> findByAppointmentIdOrderByChangedAtDesc(Long appointmentId);
}

