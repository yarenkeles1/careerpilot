package com.yaren.careerpilot.service.impl;

import com.yaren.careerpilot.dto.request.ResumeUploadRequest;
import com.yaren.careerpilot.dto.response.ResumeAnalysisResponse;
import com.yaren.careerpilot.dto.response.ResumeResponse;
import com.yaren.careerpilot.dto.response.ResumeUploadResponse;
import com.yaren.careerpilot.entity.Resume;
import com.yaren.careerpilot.entity.ResumeAnalysis;
import com.yaren.careerpilot.entity.User;
import com.yaren.careerpilot.enums.ResumeStatus;
import com.yaren.careerpilot.exception.*;
import com.yaren.careerpilot.repository.ResumeAnalysisRepository;
import com.yaren.careerpilot.repository.ResumeRepository;
import com.yaren.careerpilot.repository.UserRepository;
import com.yaren.careerpilot.service.FileStorageService;
import com.yaren.careerpilot.service.ResumeAnalyzerAiService;
import com.yaren.careerpilot.service.ResumeParserService;
import com.yaren.careerpilot.service.ResumeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;

    private final UserRepository userRepository;

    private final ResumeParserService resumeParserService;

    private final ResumeAnalyzerAiService aiService;

    private final ResumeAnalysisRepository resumeAnalysisRepository;

    private static final List<String> ALLOWED_EXTENSIONS =
            List.of(".pdf", ".docx");

    private static final List<String> ALLOWED_CONTENT_TYPES =
            List.of(
                    "application/pdf",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            );

    private static final long MAX_FILE_SIZE =
            5 * 1024 * 1024;

    private final FileStorageService fileStorageService;

    @Override
    public ResumeUploadResponse uploadResume(ResumeUploadRequest request) {

        MultipartFile file = request.getFile();

        User user = getCurrentUser();

        validateFile(file);

        String filePath = fileStorageService.store(file);

        Resume resume = createResume(file, filePath, user);

        String extractedText = resumeParserService.extractText(file);

        resume.setExtractedText(extractedText);

        Resume savedResume = resumeRepository.save(resume);

        return createResponse(savedResume);
    }

    private void validateFile(MultipartFile file){

        validateFileIsNotEmpty(file);

        validateFileExtension(file);

        validateContentType(file);

        validateFileSize(file);

    }

    private void validateFileIsNotEmpty(MultipartFile file){

        if (file == null || file.isEmpty()) {
            throw new EmptyFileException("Resume file is required.");
        }
    }

    private void validateFileExtension(MultipartFile file){

        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank()){
            throw new IllegalArgumentException("File name is missing.");
        }

        fileName = fileName.toLowerCase(Locale.ROOT);

        boolean valid = false;

        for (String extension : ALLOWED_EXTENSIONS){

            if(fileName.endsWith(extension)){
                valid = true;
                break;
            }
        }

        if(!valid){
            throw new InvalidFileExtensionException(
                    "Only PDF and DOCX files are allowed.");
        }
    }

    private void validateContentType(MultipartFile file){

        String contentType = file.getContentType();

        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("Content type is missing.");
        }
        boolean valid = false;

        for (String allowedType : ALLOWED_CONTENT_TYPES) {

            if (contentType.equals(allowedType)) {
                valid = true;
                break;
            }

        }

        if (!valid) {
            throw new InvalidContentTypeException(
                    "Only PDF and DOCX files are allowed.");
        }
    }

    private void validateFileSize(MultipartFile file){

        long fileSize = file.getSize();

        if(fileSize > MAX_FILE_SIZE){
            throw new FileTooLargeException(
                    "Maximum file size is 5 MB.");
        }
    }

    private Resume createResume(MultipartFile file, String filePath, User user) {

        Resume resume = new Resume();

        resume.setFileName(file.getOriginalFilename());

        resume.setFilePath(filePath);

        resume.setStatus(ResumeStatus.UPLOADED);

        resume.setUser(user);

        return resume;
    }

    private ResumeUploadResponse createResponse(Resume resume) {

        ResumeUploadResponse response = new ResumeUploadResponse();

        response.setResumeId(resume.getId());

        response.setFileName(resume.getFileName());

        response.setStatus(resume.getStatus());

        return response;
    }

    private ResumeResponse mapToResponse(Resume resume) {

        ResumeResponse response = new ResumeResponse();

        response.setId(resume.getId());

        response.setFileName(resume.getFileName());

        response.setStatus(resume.getStatus());

        response.setUploadedAt(resume.getUploadedAt());

        return response;
    }

    @Override
    public List<ResumeResponse> getAllResumes() {

        User user = getCurrentUser();

        return resumeRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<ResumeResponse> searchResumes(String keyword) {

        User user = getCurrentUser();

        return resumeRepository
                .findByUserIdAndFileNameContainingIgnoreCase(user.getId(), keyword)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ResumeResponse getResumeById(Long id) {

        User user = getCurrentUser();

        Resume resume = getOwnedResumeOrThrow(id, user);

        return mapToResponse(resume);
    }

    @Override
    public ResumeResponse updateResume(Long id, ResumeUploadRequest request) {

        User user = getCurrentUser();

        Resume resume = getOwnedResumeOrThrow(id, user);

        MultipartFile file = request.getFile();

        validateFile(file);

        String oldFilePath = resume.getFilePath();

        String newFilePath = fileStorageService.store(file);

        String extractedText = resumeParserService.extractText(file);

        resume.setExtractedText(extractedText);

        resumeAnalysisRepository.findByResumeId(id)
                .ifPresent(analysis -> resumeAnalysisRepository.delete(analysis));

        resume.setFileName(file.getOriginalFilename());

        resume.setFilePath(newFilePath);

        resume.setStatus(ResumeStatus.UPLOADED);

        Resume updatedResume = resumeRepository.save(resume);

        try {
            fileStorageService.delete(oldFilePath);
        } catch (FileStorageException e) {
            log.warn("Failed to delete old physical file for resume id={}, path={}", id, oldFilePath, e);
        }

        return mapToResponse(updatedResume);
    }

    @Override
    @Transactional
    public void deleteResume(Long id) {
        User user = getCurrentUser();
        Resume resume = getOwnedResumeOrThrow(id, user);

        resumeAnalysisRepository.findByResumeId(id)
                .ifPresent(analysis -> resumeAnalysisRepository.delete(analysis));
        String filePath = resume.getFilePath();
        resumeRepository.delete(resume);
        try {
            fileStorageService.delete(filePath);
        } catch (FileStorageException e) {
            log.warn("Failed to delete physical file for resume id={}, path={}", id, filePath, e);
        }
    }

    private Resume getOwnedResumeOrThrow(Long id, User user) {

        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResumeNotFoundException("Resume not found."));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new ResumeNotFoundException("Resume not found.");
        }

        return resume;
    }

    private User getCurrentUser() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found."));
    }

    @Override
    @Transactional
    public ResumeAnalysisResponse analyzeResume(Long id) {

        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResumeNotFoundException("Resume not found"));
        if (!resume.getUser().getId().equals(getCurrentUser().getId())) {
            throw new RuntimeException("You do not have the authority to analyze this resume.");
        }

        var existingAnalysis = resumeAnalysisRepository.findByResumeId(id);
        if (existingAnalysis.isPresent()) {
            var analysis = existingAnalysis.get();
            return new ResumeAnalysisResponse(
                    analysis.getOverallScore(),
                    analysis.getAtsScore(),
                    analysis.getStrengths(),
                    analysis.getWeaknesses(),
                    analysis.getMissingKeywords(),
                    analysis.getRecommendedRoles(),
                    analysis.getActionableAdvice()
            );
        }
        if (resume.getExtractedText() == null || resume.getExtractedText().isEmpty()) {
            throw new RuntimeException("No readable text was found in this resume.");
        }

        ResumeAnalysisResponse aiResponse = aiService.analyzeResume(resume.getExtractedText());

        ResumeAnalysis newAnalysis = ResumeAnalysis.builder()
                .resume(resume)
                .overallScore(aiResponse.overallScore())
                .atsScore(aiResponse.atsScore())
                .strengths(aiResponse.strengths())
                .weaknesses(aiResponse.weaknesses())
                .missingKeywords(aiResponse.missingKeywords())
                .recommendedRoles(aiResponse.recommendedRoles())
                .actionableAdvice(aiResponse.actionableAdvice())
                .build();

        resumeAnalysisRepository.save(newAnalysis);
        return aiResponse;
    }
}