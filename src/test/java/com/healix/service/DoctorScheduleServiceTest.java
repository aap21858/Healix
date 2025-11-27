package com.healix.service;

import com.healix.dto.DoctorScheduleDTO;
import com.healix.entity.DoctorSchedule;
import com.healix.exception.ResourceNotFoundException;
import com.healix.mapper.DoctorScheduleMapper;
import com.healix.repository.DoctorScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorScheduleServiceTest {

    @Mock
    private DoctorScheduleRepository scheduleRepository;

    @Mock
    private DoctorScheduleMapper scheduleMapper;

    @InjectMocks
    private DoctorScheduleService scheduleService;

    private DoctorSchedule schedule;
    private DoctorScheduleDTO scheduleDTO;

    @BeforeEach
    void setUp() {
        schedule = new DoctorSchedule();
        schedule.setId(1L);
        schedule.setDoctorId(100L);
        schedule.setDayOfWeek(1); // Monday
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(17, 0));
        schedule.setSlotDurationMinutes(30);
        schedule.setBufferTimeMinutes(5);
        schedule.setIsAvailable(true);
        schedule.setEffectiveFrom(LocalDate.now());

        scheduleDTO = new DoctorScheduleDTO();
        scheduleDTO.setId(1L);
        scheduleDTO.setDoctorId(100L);
        scheduleDTO.setDayOfWeek(1);
        scheduleDTO.setStartTime(LocalTime.of(9, 0));
        scheduleDTO.setEndTime(LocalTime.of(17, 0));
        scheduleDTO.setSlotDurationMinutes(30);
        scheduleDTO.setBufferTimeMinutes(5);
        scheduleDTO.setIsAvailable(true);
        scheduleDTO.setEffectiveFrom(LocalDate.now());
    }

    @Test
    void testCreateSchedule_Success() {
        when(scheduleMapper.toEntity(any(DoctorScheduleDTO.class))).thenReturn(schedule);
        when(scheduleRepository.save(any(DoctorSchedule.class))).thenReturn(schedule);
        when(scheduleMapper.toDto(any(DoctorSchedule.class))).thenReturn(scheduleDTO);

        DoctorScheduleDTO result = scheduleService.createSchedule(scheduleDTO);

        assertNotNull(result);
        assertEquals(scheduleDTO.getDoctorId(), result.getDoctorId());
        assertEquals(scheduleDTO.getDayOfWeek(), result.getDayOfWeek());
        verify(scheduleRepository, times(1)).save(any(DoctorSchedule.class));
    }

    @Test
    void testGetScheduleById_Success() {
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(scheduleMapper.toDto(any(DoctorSchedule.class))).thenReturn(scheduleDTO);

        DoctorScheduleDTO result = scheduleService.getScheduleById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(scheduleRepository, times(1)).findById(1L);
    }

    @Test
    void testGetScheduleById_NotFound() {
        when(scheduleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> scheduleService.getScheduleById(999L));
    }

    @Test
    void testGetDoctorSchedules_Success() {
        List<DoctorSchedule> schedules = Collections.singletonList(schedule);
        when(scheduleRepository.findByDoctorIdAndIsAvailableTrue(100L)).thenReturn(schedules);
        when(scheduleMapper.toDtoList(anyList())).thenReturn(Collections.singletonList(scheduleDTO));

        List<DoctorScheduleDTO> results = scheduleService.getDoctorSchedules(100L);

        assertNotNull(results);
        assertEquals(1, results.size());
        verify(scheduleRepository, times(1)).findByDoctorIdAndIsAvailableTrue(100L);
    }

    @Test
    void testUpdateSchedule_Success() {
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(scheduleRepository.save(any(DoctorSchedule.class))).thenReturn(schedule);
        when(scheduleMapper.toDto(any(DoctorSchedule.class))).thenReturn(scheduleDTO);

        DoctorScheduleDTO result = scheduleService.updateSchedule(1L, scheduleDTO);

        assertNotNull(result);
        verify(scheduleRepository, times(1)).findById(1L);
        verify(scheduleRepository, times(1)).save(any(DoctorSchedule.class));
    }

    @Test
    void testDeleteSchedule_Success() {
        when(scheduleRepository.existsById(1L)).thenReturn(true);
        doNothing().when(scheduleRepository).deleteById(1L);

        assertDoesNotThrow(() -> scheduleService.deleteSchedule(1L));

        verify(scheduleRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteSchedule_NotFound() {
        when(scheduleRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            scheduleService.deleteSchedule(999L);
        });
    }
}

