package com.yaren.careerpilot.controller;

import com.yaren.careerpilot.dto.request.ResumeUploadRequest;
import com.yaren.careerpilot.dto.response.ResumeUploadResponse;
import com.yaren.careerpilot.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping
    public ResumeUploadResponse uploadResume(
            @Valid @ModelAttribute ResumeUploadRequest request) {

        return resumeService.uploadResume(request);
    }
}
