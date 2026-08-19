package com.yaren.careerpilot.repository;

import com.yaren.careerpilot.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByFileNameContainingIgnoreCase(String keyword);

    List<Resume> findByUserIdAndFileNameContainingIgnoreCase(Long userId, String keyword);

    List<Resume> findByUserId(Long userId);
}
