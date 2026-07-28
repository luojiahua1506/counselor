package com.counselor.repository;

import com.counselor.entity.PsychAssessmentBatch;
import com.counselor.enums.PsychBatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PsychAssessmentBatchRepository extends JpaRepository<PsychAssessmentBatch, Long> {
    List<PsychAssessmentBatch> findByStatusOrderByCreatedAtDesc(PsychBatchStatus status);
    List<PsychAssessmentBatch> findAllByOrderByCreatedAtDesc();
}
