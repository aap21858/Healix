package com.healix.mapper;

import com.healix.dto.DoctorScheduleDTO;
import com.healix.entity.DoctorSchedule;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DoctorScheduleMapper {

    @Mapping(target = "doctorName", ignore = true)
    @Mapping(target = "dayName", ignore = true)
    DoctorScheduleDTO toDto(DoctorSchedule entity);

    List<DoctorScheduleDTO> toDtoList(List<DoctorSchedule> entities);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    DoctorSchedule toEntity(DoctorScheduleDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(DoctorScheduleDTO dto, @MappingTarget DoctorSchedule entity);
}

