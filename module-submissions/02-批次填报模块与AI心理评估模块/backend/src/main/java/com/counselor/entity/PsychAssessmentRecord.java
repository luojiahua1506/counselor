package com.counselor.entity;

import com.counselor.enums.PsychRecordStatus;
import com.counselor.enums.PsychRiskLevel;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "psych_assessment_record", uniqueConstraints = @UniqueConstraint(columnNames = {"counselor_id", "batch_id"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PsychAssessmentRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "counselor_id") private Counselor counselor;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "batch_id") private PsychAssessmentBatch batch;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) @Builder.Default private PsychRecordStatus status = PsychRecordStatus.DRAFT;
    @Column(name = "pss_score") private Integer pssScore;
    @Column(name = "gad_score") private Integer gadScore;
    @Column(name = "phq_score") private Integer phqScore;
    @Enumerated(EnumType.STRING) @Column(name = "risk_level", length = 20) private PsychRiskLevel riskLevel;
    @Column(name = "self_harm_flag", nullable = false) @Builder.Default private Boolean selfHarmFlag = false;
    @Column(name = "ai_status", length = 20) private String aiStatus;
    @Column(name = "consent_version", nullable = false, length = 30) private String consentVersion;
    @Column(name = "consent_time", nullable = false) private LocalDateTime consentTime;
    @Column(name = "submitted_at") private LocalDateTime submittedAt;
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @PrePersist void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate void onUpdate() { updatedAt = LocalDateTime.now(); }
}
