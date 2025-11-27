package com.healix.service;

import com.healix.dto.AppointmentAuditDTO;
import com.healix.entity.AppointmentAudit;
import com.healix.mapper.AppointmentAuditMapper;
import com.healix.repository.AppointmentAuditRepository;
import com.healix.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentAuditService {

    private final AppointmentAuditRepository auditRepository;
    private final AppointmentAuditMapper auditMapper;
    private final CurrentUser currentUserProvider;

    @Transactional
    public void logAppointmentCreation(Long appointmentId, String status, LocalDate date, LocalTime time) {
        AppointmentAudit audit = new AppointmentAudit();
        audit.setAppointmentId(appointmentId);
        audit.setAction("CREATED");
        audit.setNewStatus(status);
        audit.setNewDate(date);
        audit.setNewTime(time);
        audit.setChangedBy(getCurrentUserId());

        auditRepository.save(audit);
        log.info("Logged appointment creation for appointment ID: {}", appointmentId);
    }

    @Transactional
    public void logStatusChange(Long appointmentId, String oldStatus, String newStatus, String reason) {
        AppointmentAudit audit = new AppointmentAudit();
        audit.setAppointmentId(appointmentId);
        audit.setAction("STATUS_CHANGED");
        audit.setOldStatus(oldStatus);
        audit.setNewStatus(newStatus);
        audit.setReason(reason);
        audit.setChangedBy(getCurrentUserId());

        auditRepository.save(audit);
        log.info("Logged status change for appointment ID: {} from {} to {}",
                appointmentId, oldStatus, newStatus);
    }

    @Transactional
    public void logRescheduling(Long appointmentId, LocalDate oldDate, LocalTime oldTime,
                                LocalDate newDate, LocalTime newTime, String reason) {
        AppointmentAudit audit = new AppointmentAudit();
        audit.setAppointmentId(appointmentId);
        audit.setAction("RESCHEDULED");
        audit.setOldDate(oldDate);
        audit.setOldTime(oldTime);
        audit.setNewDate(newDate);
        audit.setNewTime(newTime);
        audit.setReason(reason);
        audit.setChangedBy(getCurrentUserId());

        auditRepository.save(audit);
        log.info("Logged rescheduling for appointment ID: {} from {}/{} to {}/{}",
                appointmentId, oldDate, oldTime, newDate, newTime);
    }

    @Transactional
    public void logCancellation(Long appointmentId, String reason) {
        AppointmentAudit audit = new AppointmentAudit();
        audit.setAppointmentId(appointmentId);
        audit.setAction("CANCELLED");
        audit.setReason(reason);
        audit.setChangedBy(getCurrentUserId());

        auditRepository.save(audit);
        log.info("Logged cancellation for appointment ID: {}", appointmentId);
    }

    @Transactional
    public void logCompletion(Long appointmentId, String notes) {
        AppointmentAudit audit = new AppointmentAudit();
        audit.setAppointmentId(appointmentId);
        audit.setAction("COMPLETED");
        audit.setNotes(notes);
        audit.setChangedBy(getCurrentUserId());

        auditRepository.save(audit);
        log.info("Logged completion for appointment ID: {}", appointmentId);
    }

    public List<AppointmentAuditDTO> getAppointmentAuditHistory(Long appointmentId) {
        List<AppointmentAudit> audits = auditRepository.findByAppointmentIdOrderByChangedAtDesc(appointmentId);
        return auditMapper.toDtoList(audits);
    }

    public List<AppointmentAuditDTO> getUserAuditHistory(Long userId) {
        List<AppointmentAudit> audits = auditRepository.findByChangedByOrderByChangedAtDesc(userId);
        return auditMapper.toDtoList(audits);
    }

    private Long getCurrentUserId() {
        try {
            if (currentUserProvider != null && currentUserProvider.getCurrentUser() != null) {
                return currentUserProvider.getCurrentUser().getId();
            }
        } catch (Exception e) {
            log.warn("Could not get current user, using default");
        }
        return 1L; // Default to system user
    }
}

