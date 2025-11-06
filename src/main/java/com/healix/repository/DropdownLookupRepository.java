package com.healix.repository;

import com.healix.entity.DropdownLookup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DropdownLookupRepository extends JpaRepository<DropdownLookup, Long> {
    List<DropdownLookup> findByTypeAndActiveTrueOrderByDisplayOrder(String type);
    Optional<DropdownLookup> findByTypeAndCode(String type, String code);
}

