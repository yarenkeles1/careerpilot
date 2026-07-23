package com.yaren.careerpilot.service.impl;

import com.yaren.careerpilot.dto.request.ResumeUploadRequest;
import com.yaren.careerpilot.dto.response.ResumeUploadResponse;
import com.yaren.careerpilot.repository.ResumeRepository;
import com.yaren.careerpilot.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;

    @Override
    public ResumeUploadResponse uploadResume(ResumeUploadRequest request) {

        return null;
    }
}

