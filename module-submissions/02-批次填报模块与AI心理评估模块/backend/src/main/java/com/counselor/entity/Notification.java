package com.counselor.entity;
import jakarta.persistence.*;import lombok.*;import java.time.LocalDateTime;
@Entity @Table(name="notification") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="counselor_id") private Counselor counselor;
    @Column(nullable=false,length=100) private String title;
    @Column(nullable=false,length=1000) private String content;
    @Column(nullable=false,length=30) private String type;
    @Column(name="business_type",length=30) private String businessType;
    @Column(name="business_id") private Long businessId;
    @Column(name="target_path",length=300) private String targetPath;
    @Column(name="is_read",nullable=false) @Builder.Default private Boolean read=false;
    @Column(name="created_at",updatable=false) private LocalDateTime createdAt;
    @Column(name="read_at") private LocalDateTime readAt;
    @PrePersist void create(){createdAt=LocalDateTime.now();}
}
