package com.healix.repository;

import com.healix.entity.Appointment;
import com.healix.model.AppointmentStatus;
import com.healix.model.AppointmentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByAppointmentNumber(String appointmentNumber);

    Page<Appointment> findByPatientId(Long patientId, Pageable pageable);

    Page<Appointment> findByPhysicianId(Long physicianId, Pageable pageable);

    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);

    Page<Appointment> findByAppointmentType(AppointmentType type, Pageable pageable);

    Page<Appointment> findByAppointmentDate(LocalDate date, Pageable pageable);

    Page<Appointment> findByAppointmentDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    boolean existsByPatientIdAndAppointmentDateAndStatusNotIn(Long patientId, LocalDate appointmentDate, List<AppointmentStatus> statuses);

    @Query("SELECT a FROM Appointment a WHERE " +
           "(:patientId IS NULL OR a.patient.id = :patientId) AND " +
           "(:physicianId IS NULL OR a.physicianId = :physicianId) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:appointmentDate IS NULL OR a.appointmentDate = :appointmentDate) AND " +
           "(:appointmentType IS NULL OR a.appointmentType = :appointmentType)")
    Page<Appointment> searchAppointments(
            @Param("patientId") Long patientId,
            @Param("physicianId") Long physicianId,
            @Param("status") AppointmentStatus status,
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("appointmentType") AppointmentType appointmentType,
            Pageable pageable
    );

    @Query("SELECT a FROM Appointment a WHERE " +
           "LOWER(a.patient.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.patient.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "a.appointmentNumber LIKE CONCAT('%', :search, '%') OR " +
           "a.patient.mobileNumber LIKE CONCAT('%', :search, '%')")
    Page<Appointment> searchByKeyword(@Param("search") String search, Pageable pageable);

    List<Appointment> findByPhysicianIdAndAppointmentDateAndStatus(
            Long physicianId,
            LocalDate appointmentDate,
            AppointmentStatus status
    );

    boolean existsByPatientIdAndAppointmentDateAndStatus(
            Long patientId,
            LocalDate appointmentDate,
            AppointmentStatus status
    );

    long countByAppointmentDate(LocalDate date);

    long countByPhysicianIdAndAppointmentDate(Long physicianId, LocalDate date);

    List<Appointment> findByPhysicianIdAndAppointmentDate(Long physicianId, LocalDate date);
}
