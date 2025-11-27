package com.healix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorScheduleDTO {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private Integer dayOfWeek; // 1=Monday, 7=Sunday
    private String dayName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer slotDurationMinutes;
    private Integer bufferTimeMinutes;
    private Integer maxAppointmentsPerSlot;
    private Boolean isAvailable;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String location;
    private String roomNumber;
    private String breakTimes; // JSON format
}

