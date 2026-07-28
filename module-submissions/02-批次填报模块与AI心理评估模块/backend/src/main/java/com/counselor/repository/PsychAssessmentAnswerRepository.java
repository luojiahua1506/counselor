package com.counselor.repository;

import com.counselor.entity.PsychAssessmentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PsychAssessmentAnswerRepository extends JpaRepository<PsychAssessmentAnswer, Long> {
    List<PsychAssessmentAnswer> findByRecordId(Long recordId);
    void deleteByRecordId(Long recordId);
}
