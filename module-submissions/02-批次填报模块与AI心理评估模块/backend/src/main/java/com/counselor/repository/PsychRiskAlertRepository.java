package com.counselor.repository;

import com.counselor.entity.PsychRiskAlert;
import com.counselor.enums.PsychAlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PsychRiskAlertRepository extends JpaRepository<PsychRiskAlert, Long> {
    Optional<PsychRiskAlert> findByRecordId(Long recordId);
    List<PsychRiskAlert> findAllByOrderByCreatedAtDesc();
    long countByStatus(PsychAlertStatus status);
}
