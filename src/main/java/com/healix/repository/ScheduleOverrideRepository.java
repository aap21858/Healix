package com.healix.repository;

import com.healix.entity.ScheduleOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleOverrideRepository extends JpaRepository<ScheduleOverride, Long> {

    Optional<ScheduleOverride> findByDoctorIdAndOverrideDate(Long doctorId, LocalDate overrideDate);

    List<ScheduleOverride> findByDoctorIdAndOverrideDateBetween(
            Long doctorId,
            LocalDate startDate,
            LocalDate endDate);

    @Query("SELECT so FROM ScheduleOverride so WHERE so.doctorId = :doctorId " +
           "AND so.overrideDate >= :fromDate " +
           "ORDER BY so.overrideDate")
    List<ScheduleOverride> findUpcomingOverrides(
            @Param("doctorId") Long doctorId,
            @Param("fromDate") LocalDate fromDate);
}

