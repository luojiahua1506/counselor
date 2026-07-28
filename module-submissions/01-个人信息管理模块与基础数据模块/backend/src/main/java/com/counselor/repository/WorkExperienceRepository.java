package com.counselor.repository; import com.counselor.entity.WorkExperience; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface WorkExperienceRepository extends JpaRepository<WorkExperience,Long> { List<WorkExperience> findBySubmissionIdOrderBySortOrder(Long submissionId); void deleteBySubmissionId(Long submissionId); }
