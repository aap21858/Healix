package com.healix.mapper;

import com.healix.dto.AppointmentAuditDTO;
import com.healix.entity.AppointmentAudit;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AppointmentAuditMapper {

    @Mapping(target = "changedByName", ignore = true)
    AppointmentAuditDTO toDto(AppointmentAudit entity);

    List<AppointmentAuditDTO> toDtoList(List<AppointmentAudit> entities);

    @Mapping(target = "changedAt", ignore = true)
    AppointmentAudit toEntity(AppointmentAuditDTO dto);
}

