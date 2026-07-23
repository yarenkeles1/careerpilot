package com.yaren.careerpilot.service;

import com.yaren.careerpilot.dto.request.ResumeUploadRequest;
import com.yaren.careerpilot.dto.response.ResumeUploadResponse;

public interface ResumeService {

    ResumeUploadResponse uploadResume(ResumeUploadRequest request);

}
