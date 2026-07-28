package com.counselor.entity;
import com.counselor.enums.SubmissionStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity
@Table(name="submission")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Submission {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="counselor_id",nullable=false) private Counselor counselor;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="batch_id",nullable=false) private Batch batch;
    @Column(nullable=false,length=50) private String name;
    @Column(length=10) private String gender;
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="college_id") private College college;
    @Column(name="political_status",length=50) private String politicalStatus;
    @Column(name="highest_education",length=50) private String highestEducation;
    @Column(name="office_address",length=200) private String officeAddress;
    @Column(length=20) private String phone;
    @Column(length=100) private String email;
    @Column(name="photo_url",length=500) private String photoUrl;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) @Builder.Default private SubmissionStatus status=SubmissionStatus.DRAFT;
    @Column(name="review_comment",length=500) private String reviewComment;
    @Column(name="submitted_at") private LocalDateTime submittedAt;
    @Column(name="reviewed_at") private LocalDateTime reviewedAt;
    @Column(name="created_at",updatable=false) private LocalDateTime createdAt;
    @Column(name="updated_at") private LocalDateTime updatedAt;
    @PrePersist void onCreate(){ createdAt=updatedAt=LocalDateTime.now(); }
    @PreUpdate void onUpdate(){ updatedAt=LocalDateTime.now(); }
}
