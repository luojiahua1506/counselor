package com.counselor.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "psych_ai_report")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PsychAiReport {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "record_id", unique = true) private PsychAssessmentRecord record;
    @Column(name = "status_summary", columnDefinition = "TEXT") private String statusSummary;
    @Column(name = "stress_analysis", columnDefinition = "TEXT") private String stressAnalysis;
    @Column(name = "emotion_analysis", columnDefinition = "TEXT") private String emotionAnalysis;
    @Column(columnDefinition = "TEXT") private String suggestions;
    @Column(name = "model_name", length = 100) private String modelName;
    @Column(name = "failure_reason", columnDefinition = "TEXT") private String failureReason;
    @Column(name = "generated_at") private LocalDateTime generatedAt;
}
