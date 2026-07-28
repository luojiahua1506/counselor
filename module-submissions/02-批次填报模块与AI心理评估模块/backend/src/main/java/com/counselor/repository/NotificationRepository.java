package com.counselor.repository;
import com.counselor.entity.Notification;import org.springframework.data.jpa.repository.JpaRepository;import java.util.List;
public interface NotificationRepository extends JpaRepository<Notification,Long>{List<Notification> findByCounselorIdOrderByCreatedAtDesc(Long counselorId);long countByCounselorIdAndReadFalse(Long counselorId);}
