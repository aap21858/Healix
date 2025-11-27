package com.healix.service;

import com.healix.dto.AvailableSlotDTO;
import com.healix.dto.DoctorScheduleDTO;
import com.healix.dto.ScheduleOverrideDTO;
import com.healix.entity.Appointment;
import com.healix.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for calculating available appointment slots
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AvailabilityService {

    private final DoctorScheduleService scheduleService;
    private final ScheduleOverrideService overrideService;
    private final AppointmentRepository appointmentRepository;

    /**
     * Get available appointment slots for a doctor on a specific date
     */
    @Transactional(readOnly = true)
    public List<AvailableSlotDTO> getAvailableSlots(Long doctorId, LocalDate date) {
        log.info("Calculating available slots for doctor ID: {} on date: {}", doctorId, date);

        List<AvailableSlotDTO> availableSlots = new ArrayList<>();

        // Check if there's an override for this date
        Optional<ScheduleOverrideDTO> override = overrideService.getOverrideByDoctorAndDate(doctorId, date);

        if (override.isPresent()) {
            ScheduleOverrideDTO overrideDTO = override.get();

            // If doctor is unavailable, return empty list
            if ("UNAVAILABLE".equals(overrideDTO.getOverrideType())) {
                log.info("Doctor is unavailable on this date due to: {}", overrideDTO.getReason());
                return availableSlots;
            }

            // If custom hours, use override times
            if ("CUSTOM_HOURS".equals(overrideDTO.getOverrideType()) &&
                overrideDTO.getStartTime() != null && overrideDTO.getEndTime() != null) {
                return calculateSlotsForTimeRange(
                        doctorId,
                        date,
                        overrideDTO.getStartTime(),
                        overrideDTO.getEndTime(),
                        30, // Default slot duration
                        5    // Default buffer time
                );
            }
        }

        // Get regular schedule for this day of week
        List<DoctorScheduleDTO> schedules = scheduleService.getActiveDoctorSchedulesForDate(doctorId, date);

        if (schedules.isEmpty()) {
            log.info("No schedule found for doctor on this date");
            return availableSlots;
        }

        // Calculate slots for each schedule period
        for (DoctorScheduleDTO schedule : schedules) {
            List<AvailableSlotDTO> slotsForPeriod = calculateSlotsForTimeRange(
                    doctorId,
                    date,
                    schedule.getStartTime(),
                    schedule.getEndTime(),
                    schedule.getSlotDurationMinutes() != null ? schedule.getSlotDurationMinutes() : 30,
                    schedule.getBufferTimeMinutes() != null ? schedule.getBufferTimeMinutes() : 5
            );
            availableSlots.addAll(slotsForPeriod);
        }

        return availableSlots;
    }

    /**
     * Calculate available slots for a specific time range
     */
    private List<AvailableSlotDTO> calculateSlotsForTimeRange(
            Long doctorId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            int slotDuration,
            int bufferTime) {

        List<AvailableSlotDTO> slots = new ArrayList<>();

        // Get existing appointments for this doctor on this date
        List<Appointment> existingAppointments = appointmentRepository.findByPhysicianIdAndAppointmentDate(
                doctorId, date);

        // Generate all possible slots
        LocalTime currentTime = startTime;
        while (currentTime.plusMinutes(slotDuration).isBefore(endTime) ||
               currentTime.plusMinutes(slotDuration).equals(endTime)) {

            LocalTime slotStartTime = currentTime;
            LocalTime slotEndTime = currentTime.plusMinutes(slotDuration);

            // Check if this slot is already booked
            boolean isBooked = existingAppointments.stream()
                    .anyMatch(apt -> {
                        LocalTime aptStart = apt.getAppointmentTime();
                        LocalTime aptEnd = aptStart.plusMinutes(apt.getDuration() != null ? apt.getDuration() : 30);

                        // Check for overlap
                        return !(slotEndTime.isBefore(aptStart) || slotEndTime.equals(aptStart) ||
                                slotStartTime.isAfter(aptEnd) || slotStartTime.equals(aptEnd));
                    });

            // Create slot DTO
            AvailableSlotDTO slot = AvailableSlotDTO.builder()
                    .date(date)
                    .startTime(slotStartTime)
                    .endTime(slotEndTime)
                    .availableSlots(isBooked ? 0 : 1)
                    .isAvailable(!isBooked)
                    .reason(isBooked ? "Already booked" : null)
                    .build();

            slots.add(slot);

            // Move to next slot (slot duration + buffer time)
            currentTime = currentTime.plusMinutes(slotDuration + bufferTime);
        }

        log.info("Generated {} slots, {} available", slots.size(),
                slots.stream().filter(AvailableSlotDTO::getIsAvailable).count());

        return slots;
    }

    /**
     * Check if a specific time slot is available
     */
    public boolean isSlotAvailable(Long doctorId, LocalDate date, LocalTime time, int duration) {
        log.info("Checking if slot is available: doctor={}, date={}, time={}, duration={}",
                doctorId, date, time, duration);

        List<AvailableSlotDTO> slots = getAvailableSlots(doctorId, date);

        return slots.stream()
                .anyMatch(slot -> slot.getStartTime().equals(time) &&
                         slot.getIsAvailable());
    }

    /**
     * Get available slots for multiple days
     */
    public List<AvailableSlotDTO> getAvailableSlotsForDateRange(
            Long doctorId,
            LocalDate startDate,
            LocalDate endDate) {

        log.info("Fetching available slots for doctor ID: {} from {} to {}",
                doctorId, startDate, endDate);

        List<AvailableSlotDTO> allSlots = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            List<AvailableSlotDTO> dailySlots = getAvailableSlots(doctorId, currentDate);
            allSlots.addAll(dailySlots);
            currentDate = currentDate.plusDays(1);
        }

        return allSlots;
    }

    /**
     * Get next available slot for a doctor
     */
    public Optional<AvailableSlotDTO> getNextAvailableSlot(Long doctorId, LocalDate fromDate) {
        log.info("Finding next available slot for doctor ID: {} from date: {}", doctorId, fromDate);

        // Search for next 30 days
        LocalDate endDate = fromDate.plusDays(30);
        List<AvailableSlotDTO> slots = getAvailableSlotsForDateRange(doctorId, fromDate, endDate);

        return slots.stream()
                .filter(AvailableSlotDTO::getIsAvailable)
                .findFirst();
    }
}

