package com.counselor.repository;

import com.counselor.entity.PsychAssessmentRecord;
import com.counselor.enums.PsychRecordStatus;
import com.counselor.enums.PsychRiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PsychAssessmentRecordRepository extends JpaRepository<PsychAssessmentRecord, Long> {
    Optional<PsychAssessmentRecord> findByCounselorIdAndBatchId(Long counselorId, Long batchId);
    List<PsychAssessmentRecord> findByCounselorIdOrderByCreatedAtDesc(Long counselorId);
    long countByStatus(PsychRecordStatus status);
    long countByRiskLevel(PsychRiskLevel level);
    List<PsychAssessmentRecord> findBySubmittedAtBefore(LocalDateTime cutoff);
    List<PsychAssessmentRecord> findTop20ByAiStatusAndStatusOrderByUpdatedAtAsc(String aiStatus, PsychRecordStatus status);
}
