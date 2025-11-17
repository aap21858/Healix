package com.healix.repository;

import com.healix.entity.Vitals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VitalsRepository extends JpaRepository<Vitals, Long> {

    List<Vitals> findByAppointmentIdOrderByRecordedAtDesc(Long appointmentId);

    List<Vitals> findByAppointmentId(Long appointmentId);
}

