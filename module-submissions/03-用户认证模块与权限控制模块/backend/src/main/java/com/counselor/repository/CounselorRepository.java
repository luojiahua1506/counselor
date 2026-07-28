package com.counselor.repository; import com.counselor.entity.Counselor; import com.counselor.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.data.jpa.repository.JpaSpecificationExecutor; import java.util.Optional;
public interface CounselorRepository extends JpaRepository<Counselor,Long>, JpaSpecificationExecutor<Counselor> { Optional<Counselor> findByUsername(String username); boolean existsByUsername(String username); long countByAccountStatus(AccountStatus status); }
