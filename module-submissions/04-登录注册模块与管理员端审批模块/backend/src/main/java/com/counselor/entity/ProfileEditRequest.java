package com.counselor.entity;
import com.counselor.enums.EditRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "profile_edit_request")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ProfileEditRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "counselor_id", nullable = false) private Counselor counselor;
    @Column(name = "changes_json", columnDefinition = "TEXT") private String changesJson;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) @Builder.Default private EditRequestStatus status = EditRequestStatus.PENDING;
    @Column(name = "admin_comment", length = 500) private String adminComment;
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate void onUpdate() { updatedAt = LocalDateTime.now(); }
}
