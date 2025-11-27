package com.healix.controller;

import com.healix.dto.ScheduleOverrideDTO;
import com.healix.service.ScheduleOverrideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing schedule overrides
 */
@RestController
@RequestMapping("/api/schedule-overrides")
@RequiredArgsConstructor
@Slf4j
public class ScheduleOverrideController {

    private final ScheduleOverrideService overrideService;

    /**
     * Create a new schedule override
     * POST /api/schedule-overrides
     */
    @PostMapping
    public ResponseEntity<ScheduleOverrideDTO> createOverride(@Valid @RequestBody ScheduleOverrideDTO overrideDTO) {
        log.info("Creating schedule override for doctor ID: {} on date: {}",
                overrideDTO.getDoctorId(), overrideDTO.getOverrideDate());
        ScheduleOverrideDTO created = overrideService.createOverride(overrideDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Get override by ID
     * GET /api/schedule-overrides/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ScheduleOverrideDTO> getOverrideById(@PathVariable Long id) {
        log.info("Fetching override by ID: {}", id);
        ScheduleOverrideDTO override = overrideService.getOverrideById(id);
        return ResponseEntity.ok(override);
    }

    /**
     * Get override by doctor and date
     * GET /api/schedule-overrides/by-date?doctorId={doctorId}&date={date}
     */
    @GetMapping("/by-date")
    public ResponseEntity<ScheduleOverrideDTO> getOverrideByDoctorAndDate(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("Fetching override for doctor ID: {} on date: {}", doctorId, date);
        Optional<ScheduleOverrideDTO> override = overrideService.getOverrideByDoctorAndDate(doctorId, date);
        return override.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get overrides by date range
     * GET /api/schedule-overrides/range?doctorId={doctorId}&startDate={startDate}&endDate={endDate}
     */
    @GetMapping("/range")
    public ResponseEntity<List<ScheduleOverrideDTO>> getOverridesByDateRange(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Fetching overrides for doctor ID: {} from {} to {}", doctorId, startDate, endDate);
        List<ScheduleOverrideDTO> overrides = overrideService.getOverridesByDateRange(doctorId, startDate, endDate);
        return ResponseEntity.ok(overrides);
    }

    /**
     * Get upcoming overrides for a doctor
     * GET /api/schedule-overrides/upcoming?doctorId={doctorId}
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<ScheduleOverrideDTO>> getUpcomingOverrides(@RequestParam Long doctorId) {
        log.info("Fetching upcoming overrides for doctor ID: {}", doctorId);
        List<ScheduleOverrideDTO> overrides = overrideService.getUpcomingOverrides(doctorId);
        return ResponseEntity.ok(overrides);
    }

    /**
     * Update a schedule override
     * PUT /api/schedule-overrides/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ScheduleOverrideDTO> updateOverride(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleOverrideDTO overrideDTO) {

        log.info("Updating override ID: {}", id);
        ScheduleOverrideDTO updated = overrideService.updateOverride(id, overrideDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a schedule override
     * DELETE /api/schedule-overrides/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOverride(@PathVariable Long id) {
        log.info("Deleting override ID: {}", id);
        overrideService.deleteOverride(id);
        return ResponseEntity.noContent().build();
    }
}

