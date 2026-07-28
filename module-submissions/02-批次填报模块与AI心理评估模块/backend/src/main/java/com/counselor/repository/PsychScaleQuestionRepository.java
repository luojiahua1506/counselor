package com.counselor.repository;

import com.counselor.entity.PsychScaleQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PsychScaleQuestionRepository extends JpaRepository<PsychScaleQuestion, Long> {
    List<PsychScaleQuestion> findAllByOrderBySortOrderAsc();
    boolean existsByCode(String code);
}
