package com.counselor.repository; import com.counselor.entity.ProfileEditRequest; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface ProfileEditRequestRepository extends JpaRepository<ProfileEditRequest,Long> { List<ProfileEditRequest> findByCounselorIdOrderByCreatedAtDesc(Long counselorId); }
