package com.healix.controller;

import com.healix.api.DropdownLookupApi;
import com.healix.model.DropdownLookupRequest;
import com.healix.model.DropdownLookupResponse;
import com.healix.service.DropdownLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DropdownLookupController implements DropdownLookupApi {

    private final DropdownLookupService service;

    @Override
    public ResponseEntity<List<DropdownLookupResponse>> createDropdowns(List<DropdownLookupRequest> dropdownLookupRequest) {
        List<DropdownLookupResponse> created = service.createBulk(dropdownLookupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    public ResponseEntity<Void> deleteDropdownById(Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<DropdownLookupResponse>> getAllDropdowns() {
        List<DropdownLookupResponse> list = service.getAll();
        return ResponseEntity.ok(list);
    }

    @Override
    public ResponseEntity<DropdownLookupResponse> getDropdownById(Long id) {
        DropdownLookupResponse resp = service.getById(id);
        return ResponseEntity.ok(resp);
    }

    @Override
    public ResponseEntity<List<DropdownLookupResponse>> getDropdownsByType(String type, Boolean active) {
        List<DropdownLookupResponse> list = service.getByType(type, active);
        return ResponseEntity.ok(list);
    }

    @Override
    public ResponseEntity<DropdownLookupResponse> patchDropdown(Long id, Map<String, Object> requestBody) {
        DropdownLookupResponse resp = service.patch(id, requestBody);
        return ResponseEntity.ok(resp);
    }

    @Override
    public ResponseEntity<DropdownLookupResponse> updateDropdown(Long id, DropdownLookupRequest dropdownLookupRequest) {
        DropdownLookupResponse resp = service.update(id, dropdownLookupRequest);
        return ResponseEntity.ok(resp);
    }
}
