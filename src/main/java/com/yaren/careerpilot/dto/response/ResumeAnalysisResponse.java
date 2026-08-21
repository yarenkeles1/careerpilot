package com.yaren.careerpilot.dto.response;

import java.util.List;

public record ResumeAnalysisResponse(
        Integer overallScore,
        Integer atsScore,
        List<String> strengths,
        List<String> weaknesses,
        List<String> missingKeywords,
        List<String> recommendedRoles,
        List<String> actionableAdvice
) {}
