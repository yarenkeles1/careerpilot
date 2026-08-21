package com.yaren.careerpilot.service.impl;

import com.yaren.careerpilot.dto.request.JobMatchRequest;
import com.yaren.careerpilot.dto.response.JobMatchResponse;
import com.yaren.careerpilot.entity.JobMatchAnalysis;
import com.yaren.careerpilot.entity.Resume;
import com.yaren.careerpilot.exception.ResumeNotFoundException;
import com.yaren.careerpilot.repository.JobMatchAnalysisRepository;
import com.yaren.careerpilot.repository.ResumeRepository;
import com.yaren.careerpilot.service.JobMatchService;
import com.yaren.careerpilot.service.JobMatcherAiService;
import com.yaren.careerpilot.service.WebScraperService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobMatchServiceImpl implements JobMatchService {

    private final ResumeRepository resumeRepository;

    private final JobMatchAnalysisRepository jobMatchAnalysisRepository;

    private final JobMatcherAiService jobMatcherAiService;

    private final WebScraperService webScraperService;

    @Override
    @Transactional
    public JobMatchResponse matchJob(Long resumeId, JobMatchRequest request) {

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResumeNotFoundException("Resume not found."));

        String finalJobDescription = request.getJobDescription();

        if (request.getJobUrl() != null && !request.getJobUrl().trim().isEmpty()) {
            String scrapedText = webScraperService.extractTextFromUrl(request.getJobUrl());
            if (scrapedText != null && !scrapedText.trim().isEmpty()) {
                finalJobDescription = scrapedText;
            }
        }

        if (finalJobDescription == null || finalJobDescription.trim().isEmpty()) {
            throw new RuntimeException("Job description text or URL is missing.");
        }

        var existing = jobMatchAnalysisRepository
                .findByResumeIdAndJobDescription(resumeId, finalJobDescription);

        if (existing.isPresent()) {
            var match = existing.get();
            return new JobMatchResponse(
                    match.getMatchScore(),
                    match.getMatchedSkills(),
                    match.getMissingSkills(),
                    match.getStrongAreas(),
                    match.getGapAreas(),
                    match.getRecommendations()
            );
        }

        if (resume.getExtractedText() == null || resume.getExtractedText().isEmpty()) {
            throw new RuntimeException("No readable text was found in this resume.");
        }

        JobMatchResponse aiResponse = jobMatcherAiService.matchResumeToJob(
                resume.getExtractedText(),
                finalJobDescription
        );

        JobMatchAnalysis analysis = JobMatchAnalysis.builder()
                .resume(resume)
                .jobDescription(finalJobDescription)
                .matchScore(aiResponse.matchScore())
                .matchedSkills(aiResponse.matchedSkills())
                .missingSkills(aiResponse.missingSkills())
                .strongAreas(aiResponse.strongAreas())
                .gapAreas(aiResponse.gapAreas())
                .recommendations(aiResponse.recommendations())
                .build();

        jobMatchAnalysisRepository.save(analysis);

        return aiResponse;
    }
}