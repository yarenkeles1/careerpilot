package com.yaren.careerpilot.service;

import com.yaren.careerpilot.dto.request.ResumeUploadRequest;
import com.yaren.careerpilot.dto.response.ResumeResponse;
import com.yaren.careerpilot.dto.response.ResumeUploadResponse;

import java.util.List;

public interface ResumeService {

    ResumeUploadResponse uploadResume(ResumeUploadRequest request);

    List<ResumeResponse> getAllResumes();

    ResumeResponse getResumeById(Long id);

    ResumeResponse updateResume(Long id, ResumeUploadRequest request);

    void deleteResume(Long id);

    List<ResumeResponse> searchResumes(String keyword);
}
