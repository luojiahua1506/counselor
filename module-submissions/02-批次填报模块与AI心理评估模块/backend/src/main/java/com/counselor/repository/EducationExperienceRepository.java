package com.counselor.repository; import com.counselor.entity.EducationExperience; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface EducationExperienceRepository extends JpaRepository<EducationExperience,Long> { List<EducationExperience> findBySubmissionIdOrderBySortOrder(Long submissionId); void deleteBySubmissionId(Long submissionId); }
