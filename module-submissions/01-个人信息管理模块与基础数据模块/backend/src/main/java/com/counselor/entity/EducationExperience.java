package com.counselor.entity;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDate;
@Entity @Table(name="education_experience") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EducationExperience {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="submission_id",nullable=false) private Submission submission;
    @Column(nullable=false,length=200) private String school;
    @Column(nullable=false,length=100) private String major; @Column(length=50) private String degree;
    @Column(name="start_date") private LocalDate startDate; @Column(name="end_date") private LocalDate endDate;
    @Column(name="sort_order") @Builder.Default private Integer sortOrder=0;
}
