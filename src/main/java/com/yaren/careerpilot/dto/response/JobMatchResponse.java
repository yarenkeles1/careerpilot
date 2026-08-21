package com.yaren.careerpilot.dto.response;

import java.util.List;

public record JobMatchResponse(
        int matchScore,
        List<String> matchedSkills,
        List<String> missingSkills,
        List<String> strongAreas,
        List<String> gapAreas,
        List<String> recommendations
) {}
