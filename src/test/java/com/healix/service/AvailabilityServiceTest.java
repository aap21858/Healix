package com.healix.service;

import com.healix.dto.AvailableSlotDTO;
import com.healix.dto.DoctorScheduleDTO;
import com.healix.dto.ScheduleOverrideDTO;
import com.healix.entity.Appointment;
import com.healix.model.AppointmentStatus;
import com.healix.repository.AppointmentRepository;
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
class AvailabilityServiceTest {

    @Mock
    private DoctorScheduleService scheduleService;

    @Mock
    private ScheduleOverrideService overrideService;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

    private DoctorScheduleDTO schedule;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.now().plusDays(1);

        schedule = new DoctorScheduleDTO();
        schedule.setDoctorId(100L);
        schedule.setDayOfWeek(testDate.getDayOfWeek().getValue());
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(12, 0));
        schedule.setSlotDurationMinutes(30);
        schedule.setBufferTimeMinutes(5);
        schedule.setIsAvailable(true);
    }

    @Test
    void testGetAvailableSlots_NoOverride_NoBookings() {
        when(overrideService.getOverrideByDoctorAndDate(100L, testDate)).thenReturn(Optional.empty());
        when(scheduleService.getActiveDoctorSchedulesForDate(100L, testDate))
                .thenReturn(Collections.singletonList(schedule));
        when(appointmentRepository.findByPhysicianIdAndAppointmentDate(100L, testDate))
                .thenReturn(Collections.emptyList());

        List<AvailableSlotDTO> slots = availabilityService.getAvailableSlots(100L, testDate);

        assertNotNull(slots);
        assertFalse(slots.isEmpty());
        assertTrue(slots.stream().allMatch(AvailableSlotDTO::getIsAvailable));
    }

    @Test
    void testGetAvailableSlots_WithBookings() {
        Appointment bookedAppointment = new Appointment();
        bookedAppointment.setId(1L);
        bookedAppointment.setPhysicianId(100L);
        bookedAppointment.setAppointmentDate(testDate);
        bookedAppointment.setAppointmentTime(LocalTime.of(9, 0));
        bookedAppointment.setDuration(30);
        bookedAppointment.setStatus(AppointmentStatus.CONFIRMED);

        when(overrideService.getOverrideByDoctorAndDate(100L, testDate)).thenReturn(Optional.empty());
        when(scheduleService.getActiveDoctorSchedulesForDate(100L, testDate))
                .thenReturn(Collections.singletonList(schedule));
        when(appointmentRepository.findByPhysicianIdAndAppointmentDate(100L, testDate))
                .thenReturn(List.of(bookedAppointment));

        List<AvailableSlotDTO> slots = availabilityService.getAvailableSlots(100L, testDate);

        assertNotNull(slots);
        // At least one slot should be unavailable (the booked one)
        assertTrue(slots.stream().anyMatch(slot -> !slot.getIsAvailable()));
    }

    @Test
    void testGetAvailableSlots_DoctorUnavailable() {
        ScheduleOverrideDTO override = new ScheduleOverrideDTO();
        override.setDoctorId(100L);
        override.setOverrideDate(testDate);
        override.setOverrideType("UNAVAILABLE");
        override.setReason("On leave");

        when(overrideService.getOverrideByDoctorAndDate(100L, testDate))
                .thenReturn(Optional.of(override));

        List<AvailableSlotDTO> slots = availabilityService.getAvailableSlots(100L, testDate);

        assertNotNull(slots);
        assertEquals(0, slots.size());
    }

    @Test
    void testGetAvailableSlots_CustomHours() {
        ScheduleOverrideDTO override = new ScheduleOverrideDTO();
        override.setDoctorId(100L);
        override.setOverrideDate(testDate);
        override.setOverrideType("CUSTOM_HOURS");
        override.setStartTime(LocalTime.of(10, 0));
        override.setEndTime(LocalTime.of(12, 0));

        when(overrideService.getOverrideByDoctorAndDate(100L, testDate))
                .thenReturn(Optional.of(override));
        when(appointmentRepository.findByPhysicianIdAndAppointmentDate(100L, testDate))
                .thenReturn(Collections.emptyList());

        List<AvailableSlotDTO> slots = availabilityService.getAvailableSlots(100L, testDate);

        assertNotNull(slots);
        assertFalse(slots.isEmpty());
        // All slots should be between 10:00 and 12:00
        assertTrue(slots.stream().allMatch(slot ->
                !slot.getStartTime().isBefore(LocalTime.of(10, 0)) &&
                !slot.getEndTime().isAfter(LocalTime.of(12, 0))));
    }

    @Test
    void testGetAvailableSlots_NoSchedule() {
        when(overrideService.getOverrideByDoctorAndDate(100L, testDate)).thenReturn(Optional.empty());
        when(scheduleService.getActiveDoctorSchedulesForDate(100L, testDate))
                .thenReturn(Collections.emptyList());

        List<AvailableSlotDTO> slots = availabilityService.getAvailableSlots(100L, testDate);

        assertNotNull(slots);
        assertEquals(0, slots.size());
    }

    @Test
    void testIsSlotAvailable_Available() {
        when(overrideService.getOverrideByDoctorAndDate(100L, testDate)).thenReturn(Optional.empty());
        when(scheduleService.getActiveDoctorSchedulesForDate(100L, testDate))
                .thenReturn(Collections.singletonList(schedule));
        when(appointmentRepository.findByPhysicianIdAndAppointmentDate(100L, testDate))
                .thenReturn(Collections.emptyList());

        boolean available = availabilityService.isSlotAvailable(100L, testDate, LocalTime.of(9, 0), 30);

        assertTrue(available);
    }

    @Test
    void testGetAvailableSlotsForDateRange() {
        LocalDate startDate = testDate;
        LocalDate endDate = testDate.plusDays(2);

        when(overrideService.getOverrideByDoctorAndDate(eq(100L), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(scheduleService.getActiveDoctorSchedulesForDate(eq(100L), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(schedule));
        when(appointmentRepository.findByPhysicianIdAndAppointmentDate(eq(100L), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        List<AvailableSlotDTO> slots = availabilityService.getAvailableSlotsForDateRange(100L, startDate, endDate);

        assertNotNull(slots);
        assertFalse(slots.isEmpty());
        // Should have slots for multiple days
        assertTrue(slots.stream().map(AvailableSlotDTO::getDate).distinct().count() > 1);
    }

    @Test
    void testGetNextAvailableSlot() {
        when(overrideService.getOverrideByDoctorAndDate(eq(100L), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(scheduleService.getActiveDoctorSchedulesForDate(eq(100L), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(schedule));
        when(appointmentRepository.findByPhysicianIdAndAppointmentDate(eq(100L), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        Optional<AvailableSlotDTO> nextSlot = availabilityService.getNextAvailableSlot(100L, testDate);

        assertTrue(nextSlot.isPresent());
        assertTrue(nextSlot.get().getIsAvailable());
    }
}

