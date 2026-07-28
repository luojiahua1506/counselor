package com.counselor.entity;

import com.counselor.enums.PsychBatchStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "psych_assessment_batch")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PsychAssessmentBatch {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 200) private String title;
    @Column(columnDefinition = "TEXT") private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) @Builder.Default
    private PsychBatchStatus status = PsychBatchStatus.DRAFT;
    @Column(name = "start_time") private LocalDateTime startTime;
    @Column(name = "end_time") private LocalDateTime endTime;
    @Column(name = "anonymous_statistics", nullable = false) @Builder.Default
    private Boolean anonymousStatistics = true;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by") private Admin createdBy;
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @PrePersist void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate void onUpdate() { updatedAt = LocalDateTime.now(); }
}
