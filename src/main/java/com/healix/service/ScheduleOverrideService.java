package com.healix.service;

import com.healix.dto.ScheduleOverrideDTO;
import com.healix.entity.ScheduleOverride;
import com.healix.exception.ResourceNotFoundException;
import com.healix.mapper.ScheduleOverrideMapper;
import com.healix.repository.ScheduleOverrideRepository;
import com.healix.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleOverrideService {

    private final ScheduleOverrideRepository overrideRepository;
    private final ScheduleOverrideMapper overrideMapper;
    private final CurrentUser currentUserProvider;

    @Transactional
    public ScheduleOverrideDTO createOverride(ScheduleOverrideDTO dto) {
        log.info("Creating schedule override for doctor ID: {} on date: {}",
                dto.getDoctorId(), dto.getOverrideDate());

        // Check if override already exists for this doctor and date
        Optional<ScheduleOverride> existing = overrideRepository.findByDoctorIdAndOverrideDate(
                dto.getDoctorId(), dto.getOverrideDate());

        if (existing.isPresent()) {
            throw new IllegalStateException("Schedule override already exists for this date");
        }

        ScheduleOverride override = overrideMapper.toEntity(dto);

        // Set created by
        Long currentUserId = getCurrentUserId();
        override.setCreatedBy(currentUserId);

        ScheduleOverride saved = overrideRepository.save(override);
        return overrideMapper.toDto(saved);
    }

    @Transactional
    public ScheduleOverrideDTO updateOverride(Long id, ScheduleOverrideDTO dto) {
        log.info("Updating schedule override ID: {}", id);

        ScheduleOverride existing = overrideRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule override not found with id: " + id));

        overrideMapper.updateEntityFromDto(dto, existing);
        ScheduleOverride updated = overrideRepository.save(existing);

        return overrideMapper.toDto(updated);
    }

    public ScheduleOverrideDTO getOverrideById(Long id) {
        ScheduleOverride override = overrideRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule override not found with id: " + id));

        return overrideMapper.toDto(override);
    }

    public Optional<ScheduleOverrideDTO> getOverrideByDoctorAndDate(Long doctorId, LocalDate date) {
        return overrideRepository.findByDoctorIdAndOverrideDate(doctorId, date)
                .map(overrideMapper::toDto);
    }

    public List<ScheduleOverrideDTO> getOverridesByDateRange(Long doctorId, LocalDate startDate, LocalDate endDate) {
        List<ScheduleOverride> overrides = overrideRepository.findByDoctorIdAndOverrideDateBetween(
                doctorId, startDate, endDate);
        return overrideMapper.toDtoList(overrides);
    }

    public List<ScheduleOverrideDTO> getUpcomingOverrides(Long doctorId) {
        LocalDate today = LocalDate.now();
        List<ScheduleOverride> overrides = overrideRepository.findUpcomingOverrides(doctorId, today);
        return overrideMapper.toDtoList(overrides);
    }

    @Transactional
    public void deleteOverride(Long id) {
        log.info("Deleting schedule override ID: {}", id);

        if (!overrideRepository.existsById(id)) {
            throw new ResourceNotFoundException("Schedule override not found with id: " + id);
        }

        overrideRepository.deleteById(id);
    }

    private Long getCurrentUserId() {
        try {
            if (currentUserProvider != null && currentUserProvider.getCurrentUser() != null) {
                return currentUserProvider.getCurrentUser().getId();
            }
        } catch (Exception e) {
            log.warn("Could not get current user, using default");
        }
        return 1L; // Default to system user
    }
}

