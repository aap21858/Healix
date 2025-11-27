package com.healix.service;

import com.healix.dto.DoctorScheduleDTO;
import com.healix.entity.DoctorSchedule;
import com.healix.exception.ResourceNotFoundException;
import com.healix.mapper.DoctorScheduleMapper;
import com.healix.repository.DoctorScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorScheduleService {

    private final DoctorScheduleRepository scheduleRepository;
    private final DoctorScheduleMapper scheduleMapper;

    @Transactional
    public DoctorScheduleDTO createSchedule(DoctorScheduleDTO dto) {
        log.info("Creating schedule for doctor ID: {}", dto.getDoctorId());

        DoctorSchedule schedule = scheduleMapper.toEntity(dto);
        DoctorSchedule saved = scheduleRepository.save(schedule);

        return scheduleMapper.toDto(saved);
    }

    @Transactional
    public DoctorScheduleDTO updateSchedule(Long id, DoctorScheduleDTO dto) {
        log.info("Updating schedule ID: {}", id);

        DoctorSchedule existing = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));

        scheduleMapper.updateEntityFromDto(dto, existing);
        DoctorSchedule updated = scheduleRepository.save(existing);

        return scheduleMapper.toDto(updated);
    }

    public DoctorScheduleDTO getScheduleById(Long id) {
        DoctorSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));

        DoctorScheduleDTO dto = scheduleMapper.toDto(schedule);
        dto.setDayName(getDayName(schedule.getDayOfWeek()));
        return dto;
    }

    public List<DoctorScheduleDTO> getDoctorSchedules(Long doctorId) {
        List<DoctorSchedule> schedules = scheduleRepository.findByDoctorIdAndIsAvailableTrue(doctorId);
        List<DoctorScheduleDTO> dtos = scheduleMapper.toDtoList(schedules);

        // Set day names
        dtos.forEach(dto -> dto.setDayName(getDayName(dto.getDayOfWeek())));

        return dtos;
    }

    public List<DoctorScheduleDTO> getActiveDoctorSchedulesForDate(Long doctorId, LocalDate date) {
        int dayOfWeek = date.getDayOfWeek().getValue();
        List<DoctorSchedule> schedules = scheduleRepository.findActiveDoctorScheduleByDayOfWeek(
                doctorId, dayOfWeek, date);

        List<DoctorScheduleDTO> dtos = scheduleMapper.toDtoList(schedules);
        dtos.forEach(dto -> dto.setDayName(getDayName(dto.getDayOfWeek())));

        return dtos;
    }

    @Transactional
    public void deleteSchedule(Long id) {
        log.info("Deleting schedule ID: {}", id);

        if (!scheduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Schedule not found with id: " + id);
        }

        scheduleRepository.deleteById(id);
    }

    private String getDayName(Integer dayOfWeek) {
        if (dayOfWeek == null) return null;
        return DayOfWeek.of(dayOfWeek).name();
    }
}

