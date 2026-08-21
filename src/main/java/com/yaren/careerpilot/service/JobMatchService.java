package com.yaren.careerpilot.service;

import com.yaren.careerpilot.dto.request.JobMatchRequest;
import com.yaren.careerpilot.dto.response.JobMatchResponse;

public interface JobMatchService {
    JobMatchResponse matchJob(Long resumeId, JobMatchRequest request);
}
