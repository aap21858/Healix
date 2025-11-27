package com.healix.controller;

import com.healix.dto.DoctorScheduleDTO;
import com.healix.service.DoctorScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for managing doctor schedules
 */
@RestController
@RequestMapping("/api/doctor-schedules")
@RequiredArgsConstructor
@Slf4j
public class DoctorScheduleController {

    private final DoctorScheduleService scheduleService;

    /**
     * Create a new doctor schedule
     * POST /api/doctor-schedules
     */
    @PostMapping
    public ResponseEntity<DoctorScheduleDTO> createSchedule(@Valid @RequestBody DoctorScheduleDTO scheduleDTO) {
        log.info("Creating doctor schedule for doctor ID: {}", scheduleDTO.getDoctorId());
        DoctorScheduleDTO created = scheduleService.createSchedule(scheduleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Get schedule by ID
     * GET /api/doctor-schedules/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<DoctorScheduleDTO> getScheduleById(@PathVariable Long id) {
        log.info("Fetching schedule by ID: {}", id);
        DoctorScheduleDTO schedule = scheduleService.getScheduleById(id);
        return ResponseEntity.ok(schedule);
    }

    /**
     * Get all schedules for a doctor
     * GET /api/doctor-schedules?doctorId={doctorId}
     */
    @GetMapping
    public ResponseEntity<List<DoctorScheduleDTO>> getDoctorSchedules(
            @RequestParam(required = false) Long doctorId) {

        if (doctorId != null) {
            log.info("Fetching schedules for doctor ID: {}", doctorId);
            List<DoctorScheduleDTO> schedules = scheduleService.getDoctorSchedules(doctorId);
            return ResponseEntity.ok(schedules);
        }

        return ResponseEntity.badRequest().build();
    }

    /**
     * Get active schedules for a doctor on a specific date
     * GET /api/doctor-schedules/active?doctorId={doctorId}&date={date}
     */
    @GetMapping("/active")
    public ResponseEntity<List<DoctorScheduleDTO>> getActiveDoctorSchedules(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("Fetching active schedules for doctor ID: {} on date: {}", doctorId, date);
        List<DoctorScheduleDTO> schedules = scheduleService.getActiveDoctorSchedulesForDate(doctorId, date);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Update a doctor schedule
     * PUT /api/doctor-schedules/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<DoctorScheduleDTO> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody DoctorScheduleDTO scheduleDTO) {

        log.info("Updating schedule ID: {}", id);
        DoctorScheduleDTO updated = scheduleService.updateSchedule(id, scheduleDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a doctor schedule
     * DELETE /api/doctor-schedules/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        log.info("Deleting schedule ID: {}", id);
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }
}

