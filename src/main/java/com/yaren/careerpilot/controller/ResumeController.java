package com.yaren.careerpilot.controller;

import com.yaren.careerpilot.dto.request.JobMatchRequest;
import com.yaren.careerpilot.dto.request.ResumeUploadRequest;
import com.yaren.careerpilot.dto.response.JobMatchResponse;
import com.yaren.careerpilot.dto.response.ResumeAnalysisResponse;
import com.yaren.careerpilot.dto.response.ResumeResponse;
import com.yaren.careerpilot.dto.response.ResumeUploadResponse;
import com.yaren.careerpilot.service.JobMatchService;
import com.yaren.careerpilot.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    private final JobMatchService jobMatchService;

    @PostMapping
    public ResumeUploadResponse uploadResume(
            @Valid @ModelAttribute ResumeUploadRequest request) {

        return resumeService.uploadResume(request);
    }

    @GetMapping
    public List<ResumeResponse> getAllResumes() {

        return resumeService.getAllResumes();
    }

    @GetMapping("/{id}")
    public ResumeResponse getResumeById(
            @PathVariable Long id) {

        return resumeService.getResumeById(id);
    }

    @PutMapping("/{id}")
    public ResumeResponse updateResume(
            @PathVariable Long id,
            @ModelAttribute ResumeUploadRequest request) {

        return resumeService.updateResume(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResume(@PathVariable Long id) {

        resumeService.deleteResume(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public List<ResumeResponse> searchResumes(
            @RequestParam String keyword) {

        return resumeService.searchResumes(keyword);
    }

    @PostMapping("/{id}/analyze")
    public ResponseEntity<ResumeAnalysisResponse> analyzeResume(@PathVariable Long id) {
        return ResponseEntity.ok(resumeService.analyzeResume(id));
    }

    @PostMapping("/{id}/match")
    public ResponseEntity<JobMatchResponse> matchJob(
            @PathVariable Long id,
            @RequestBody JobMatchRequest request) {
        return ResponseEntity.ok(jobMatchService.matchJob(id, request));
    }
}
