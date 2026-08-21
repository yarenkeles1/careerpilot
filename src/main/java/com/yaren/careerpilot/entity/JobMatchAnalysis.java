package com.yaren.careerpilot.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "job_match_analyses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobMatchAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "job_description", columnDefinition = "TEXT", nullable = false)
    private String jobDescription;

    @Column(name = "match_score")
    private int matchScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "matched_skills")
    private List<String> matchedSkills;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "missing_skills")
    private List<String> missingSkills;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "strong_areas")
    private List<String> strongAreas;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "gap_areas")
    private List<String> gapAreas;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "recommendations")
    private List<String> recommendations;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
