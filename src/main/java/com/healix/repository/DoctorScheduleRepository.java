package com.healix.repository;

import com.healix.entity.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {

    List<DoctorSchedule> findByDoctorIdAndIsAvailableTrue(Long doctorId);

    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.doctorId = :doctorId " +
           "AND ds.dayOfWeek = :dayOfWeek " +
           "AND ds.isAvailable = true " +
           "AND ds.effectiveFrom <= :date " +
           "AND (ds.effectiveTo IS NULL OR ds.effectiveTo >= :date)")
    List<DoctorSchedule> findActiveDoctorScheduleByDayOfWeek(
            @Param("doctorId") Long doctorId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("date") LocalDate date);

    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.doctorId = :doctorId " +
           "AND ds.effectiveFrom <= :date " +
           "AND (ds.effectiveTo IS NULL OR ds.effectiveTo >= :date)")
    List<DoctorSchedule> findActiveDoctorSchedules(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date);
}

