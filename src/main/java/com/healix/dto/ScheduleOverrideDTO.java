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
public class ScheduleOverrideDTO {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private LocalDate overrideDate;
    private String overrideType; // UNAVAILABLE, CUSTOM_HOURS, BREAK
    private LocalTime startTime;
    private LocalTime endTime;
    private String reason;
    private String notes;
}

