package com.healix.service;

import com.healix.entity.DropdownLookup;
import com.healix.exception.DuplicateResourceException;
import com.healix.exception.ResourceNotFoundException;
import com.healix.model.DropdownLookupRequest;
import com.healix.model.DropdownLookupResponse;
import com.healix.repository.DropdownLookupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DropdownLookupService {

    private final DropdownLookupRepository repository;

    public List<DropdownLookupResponse> createBulk(List<DropdownLookupRequest> requests) {
        // Validate duplicates inside incoming list (type+code)
        Set<String> keys = requests.stream()
                .map(r -> r.getType() + "::" + r.getCode())
                .collect(Collectors.toSet());
        if (keys.size() < requests.size()) {
            throw new DuplicateResourceException("Duplicate entries found in request payload (same type+code)");
        }

        // Check DB for existing entries with same type+code
        for (DropdownLookupRequest req : requests) {
            repository.findByTypeAndCode(req.getType(), req.getCode()).ifPresent(existing -> {
                throw new DuplicateResourceException("DropdownLookup", "type+code", req.getType() + ":" + req.getCode());
            });
        }

        List<DropdownLookup> entities = requests.stream().map(this::toEntity).collect(Collectors.toList());
        List<DropdownLookup> saved = repository.saveAll(entities);
        return saved.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<DropdownLookupResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public DropdownLookupResponse getById(Long id) {
        DropdownLookup entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DropdownLookup", "id", id));
        return toResponse(entity);
    }

    public List<DropdownLookupResponse> getByType(String type, Boolean active) {
        List<DropdownLookup> list;
        if (active != null) {
            list = repository.findAll().stream()
                    .filter(d -> type.equals(d.getType()) && active.equals(d.getActive()))
                    .sorted(Comparator.comparingInt(DropdownLookup::getDisplayOrder))
                    .collect(Collectors.toList());
        } else {
            list = repository.findAll().stream()
                    .filter(d -> type.equals(d.getType()))
                    .sorted(Comparator.comparingInt(DropdownLookup::getDisplayOrder))
                    .collect(Collectors.toList());
        }
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public void deleteById(Long id) {
        DropdownLookup entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DropdownLookup", "id", id));
        repository.delete(entity);
    }

    public DropdownLookupResponse update(Long id, DropdownLookupRequest request) {
        DropdownLookup entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DropdownLookup", "id", id));

        // If type/code changed, ensure new type+code is not used by another record
        if (!entity.getType().equals(request.getType()) || !entity.getCode().equals(request.getCode())) {
            repository.findByTypeAndCode(request.getType(), request.getCode()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new DuplicateResourceException("DropdownLookup", "type+code", request.getType() + ":" + request.getCode());
                }
            });
        }

        entity.setType(request.getType());
        entity.setCode(request.getCode());
        entity.setDescription(request.getDescription());
        entity.setActive(request.getActive());
        entity.setDisplayOrder(request.getDisplayOrder());
        // update timestamp will be handled by entity @PreUpdate
        DropdownLookup updated = repository.save(entity);
        return toResponse(updated);
    }

    public DropdownLookupResponse patch(Long id, Map<String, Object> patch) {
        DropdownLookup entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DropdownLookup", "id", id));

        String newType;
        String newCode = entity.getCode();
        if (patch.containsKey("type")) newType = (String) patch.get("type");
        else {
            newType = entity.getType();
        }
        if (patch.containsKey("code")) newCode = (String) patch.get("code");

        // If type/code would change, ensure uniqueness
        if (!entity.getType().equals(newType) || !entity.getCode().equals(newCode)) {
            String finalNewCode = newCode;
            repository.findByTypeAndCode(newType, newCode).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new DuplicateResourceException("DropdownLookup", "type+code", newType + ":" + finalNewCode);
                }
            });
        }

        if (patch.containsKey("type")) entity.setType((String) patch.get("type"));
        if (patch.containsKey("code")) entity.setCode((String) patch.get("code"));
        if (patch.containsKey("description")) entity.setDescription((String) patch.get("description"));
        if (patch.containsKey("active")) entity.setActive((Boolean) patch.get("active"));
        if (patch.containsKey("displayOrder")) entity.setDisplayOrder((Integer) patch.get("displayOrder"));

        DropdownLookup updated = repository.save(entity);
        return toResponse(updated);
    }

    private DropdownLookup toEntity(DropdownLookupRequest req) {
        return DropdownLookup.builder()
                .type(req.getType())
                .code(req.getCode())
                .description(req.getDescription())
                .active(req.getActive() == null || req.getActive())
                .displayOrder(req.getDisplayOrder() == null ? 0 : req.getDisplayOrder())
                .build();
    }

    private DropdownLookupResponse toResponse(DropdownLookup d) {
        DropdownLookupResponse r = new DropdownLookupResponse();
        r.setId(d.getId());
        r.setType(d.getType());
        r.setCode(d.getCode());
        r.setDescription(d.getDescription());
        r.setActive(d.getActive());
        r.setDisplayOrder(d.getDisplayOrder());
        if (d.getCreatedAt() != null) r.setCreatedAt(d.getCreatedAt().atOffset(ZoneOffset.UTC));
        if (d.getUpdatedAt() != null) r.setUpdatedAt(d.getUpdatedAt().atOffset(ZoneOffset.UTC));
        return r;
    }
}
