package com.healix.mapper;

import com.healix.dto.ScheduleOverrideDTO;
import com.healix.entity.ScheduleOverride;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ScheduleOverrideMapper {

    @Mapping(target = "doctorName", ignore = true)
    ScheduleOverrideDTO toDto(ScheduleOverride entity);

    List<ScheduleOverrideDTO> toDtoList(List<ScheduleOverride> entities);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    ScheduleOverride toEntity(ScheduleOverrideDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntityFromDto(ScheduleOverrideDTO dto, @MappingTarget ScheduleOverride entity);
}

