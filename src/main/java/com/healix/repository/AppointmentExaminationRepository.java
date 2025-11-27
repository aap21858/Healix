package com.healix.repository;

import com.healix.entity.AppointmentExamination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppointmentExaminationRepository extends JpaRepository<AppointmentExamination, Long> {

    Optional<AppointmentExamination> findByAppointmentId(Long appointmentId);

    boolean existsByAppointmentId(Long appointmentId);
}

