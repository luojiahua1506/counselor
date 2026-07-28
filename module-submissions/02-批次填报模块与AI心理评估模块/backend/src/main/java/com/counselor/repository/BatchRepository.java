package com.counselor.repository; import com.counselor.entity.Batch; import com.counselor.enums.BatchStatus;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface BatchRepository extends JpaRepository<Batch,Long> { List<Batch> findByStatusOrderByCreatedAtDesc(BatchStatus status); }
