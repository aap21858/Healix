package com.healix.repository;

import com.healix.entity.InsuranceScheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InsuranceSchemeRepository extends JpaRepository<InsuranceScheme, Long> {

    Optional<InsuranceScheme> findBySchemeCode(String schemeCode);

    List<InsuranceScheme> findByIsActiveTrueOrderByDisplayOrderAsc();

    List<InsuranceScheme> findBySchemeTypeAndIsActiveTrueOrderByDisplayOrderAsc(String schemeType);
}