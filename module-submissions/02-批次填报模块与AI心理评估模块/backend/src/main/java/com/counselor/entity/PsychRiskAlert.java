package com.counselor.entity;

import com.counselor.enums.PsychAlertStatus;
import com.counselor.enums.PsychRiskLevel;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "psych_risk_alert")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PsychRiskAlert {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "record_id", unique = true) private PsychAssessmentRecord record;
    @Enumerated(EnumType.STRING) @Column(name = "risk_level", nullable = false, length = 20) private PsychRiskLevel riskLevel;
    @Column(nullable = false, length = 500) private String reason;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) @Builder.Default private PsychAlertStatus status = PsychAlertStatus.PENDING;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assigned_admin_id") private Admin assignedAdmin;
    @Column(name = "follow_up_note", columnDefinition = "TEXT") private String followUpNote;
    @Column(name = "contacted_at") private LocalDateTime contactedAt;
    @Column(name = "completed_at") private LocalDateTime completedAt;
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @PrePersist void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate void onUpdate() { updatedAt = LocalDateTime.now(); }
}
