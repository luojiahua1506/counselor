package com.counselor.entity;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="college") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class College {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,unique=true,length=100) private String name;
    @Column(name="created_at",updatable=false) private LocalDateTime createdAt;
    @PrePersist void onCreate(){ createdAt=LocalDateTime.now(); }
}
