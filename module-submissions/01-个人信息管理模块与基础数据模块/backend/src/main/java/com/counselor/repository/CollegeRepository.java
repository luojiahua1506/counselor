package com.counselor.repository; import com.counselor.entity.College; import org.springframework.data.jpa.repository.JpaRepository;
public interface CollegeRepository extends JpaRepository<College,Long> { boolean existsByName(String name); }
