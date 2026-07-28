package com.counselor.entity;
import com.counselor.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
@Entity
@Table(name="counselor")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Counselor {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,unique=true,length=50) private String username;
    @Column(nullable=false,length=200) private String password;
    @Column(nullable=false,length=50) private String name;
    @Column(length=10) private String gender;
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="college_id") private College college;
    @Column(name="political_status",length=50) private String politicalStatus;
    @Column(name="highest_education",length=50) private String highestEducation;
    @Column(name="office_address",length=200) private String officeAddress;
    @Column(length=20) private String phone;
    @Column(length=100) private String email;
    @Column(name="photo_url",length=500) private String photoUrl;
    @Column(name="registration_review_comment",length=500) private String registrationReviewComment;
    @Column(name="must_change_password",nullable=false) @Builder.Default private Boolean mustChangePassword=false;
    @Enumerated(EnumType.STRING) @JdbcTypeCode(SqlTypes.VARCHAR) @Column(name="account_status",nullable=false,length=20) @Builder.Default private AccountStatus accountStatus=AccountStatus.PENDING_REVIEW;
    @Column(name="created_at",updatable=false) private LocalDateTime createdAt;
    @Column(name="updated_at") private LocalDateTime updatedAt;
    @PrePersist void onCreate(){ createdAt=updatedAt=LocalDateTime.now(); }
    @PreUpdate void onUpdate(){ updatedAt=LocalDateTime.now(); }
}
