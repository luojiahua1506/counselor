package com.counselor.entity;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDate;
@Entity @Table(name="work_experience") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkExperience {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="submission_id",nullable=false) private Submission submission;
    @Column(nullable=false,length=200) private String organization;
    @Column(nullable=false,length=100) private String position;
    @Column(name="start_date") private LocalDate startDate; @Column(name="end_date") private LocalDate endDate;
    @Column(columnDefinition="TEXT") private String description;
    @Column(name="sort_order") @Builder.Default private Integer sortOrder=0;
}
