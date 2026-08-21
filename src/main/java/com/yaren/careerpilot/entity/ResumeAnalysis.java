package com.yaren.careerpilot.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "resume_analyses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "resume_id", nullable = false, unique = true)
    private Resume resume;

    private Integer overallScore;

    private Integer atsScore;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> strengths;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> weaknesses;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> missingKeywords;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> recommendedRoles;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> actionableAdvice;
}
