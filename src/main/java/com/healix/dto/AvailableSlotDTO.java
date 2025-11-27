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
public class AvailableSlotDTO {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer availableSlots;
    private Boolean isAvailable;
    private String reason; // If not available
}

