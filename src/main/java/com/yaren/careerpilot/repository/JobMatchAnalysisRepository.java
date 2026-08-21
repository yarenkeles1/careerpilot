package com.yaren.careerpilot.repository;

import com.yaren.careerpilot.entity.JobMatchAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobMatchAnalysisRepository extends JpaRepository<JobMatchAnalysis, Long> {
    Optional<JobMatchAnalysis> findByResumeIdAndJobDescription(Long resumeId, String jobDescription);
}
