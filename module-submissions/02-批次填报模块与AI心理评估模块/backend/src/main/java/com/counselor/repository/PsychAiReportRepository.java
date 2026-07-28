package com.counselor.repository;

import com.counselor.entity.PsychAiReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PsychAiReportRepository extends JpaRepository<PsychAiReport, Long> {
    Optional<PsychAiReport> findByRecordId(Long recordId);
    void deleteByRecordId(Long recordId);
}
