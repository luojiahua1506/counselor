package com.counselor.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "psych_assessment_answer", uniqueConstraints = @UniqueConstraint(columnNames = {"record_id", "question_id"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PsychAssessmentAnswer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "record_id") @OnDelete(action = OnDeleteAction.CASCADE) private PsychAssessmentRecord record;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "question_id") private PsychScaleQuestion question;
    @Column(name = "selected_value", nullable = false) private Integer selectedValue;
    @Column(name = "calculated_score", nullable = false) private Integer calculatedScore;
}
