package com.yaren.careerpilot.service.impl;

import com.yaren.careerpilot.dto.request.ResumeUploadRequest;
import com.yaren.careerpilot.dto.response.ResumeResponse;
import com.yaren.careerpilot.dto.response.ResumeUploadResponse;
import com.yaren.careerpilot.entity.Resume;
import com.yaren.careerpilot.enums.ResumeStatus;
import com.yaren.careerpilot.exception.*;
import com.yaren.careerpilot.repository.ResumeRepository;
import com.yaren.careerpilot.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;

    private static final List<String> ALLOWED_EXTENSIONS =
            List.of(".pdf", ".docx");

    private static final List<String> ALLOWED_CONTENT_TYPES =
            List.of(
                    "application/pdf",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            );

    private static final long MAX_FILE_SIZE =
            5 * 1024 * 1024;

    private static final String UPLOAD_DIRECTORY = "uploads";

    @Override
    public ResumeUploadResponse uploadResume(ResumeUploadRequest request) {

        MultipartFile file = request.getFile();

        validateFile(file);

        String filePath = saveFile(file);

        Resume resume = createResume(file, filePath);

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

    private String saveFile(MultipartFile file){

        Path uploadPath = Paths.get(UPLOAD_DIRECTORY);

        String originalFileName = file.getOriginalFilename();

        String extension = getFileExtension(originalFileName);

        String uuid = UUID.randomUUID().toString();

        String uniqueFileName = uuid + extension;

        Path targetPath = uploadPath.resolve(uniqueFileName);

        try{
            Files.createDirectories(uploadPath);

            Files.copy(file.getInputStream(), targetPath);

            return targetPath.toString();

        }catch (IOException e){

            throw new RuntimeException("Failed to save resume.", e);
        }
    }

    private String getFileExtension(String fileName) {

        int lastDot = fileName.lastIndexOf(".");

        if (lastDot == -1) {
            throw new IllegalArgumentException("File extension is missing.");
        }

        return fileName.substring(lastDot);
    }

    private Resume createResume(MultipartFile file, String filePath) {

        Resume resume = new Resume();

        resume.setFileName(file.getOriginalFilename());

        resume.setFilePath(filePath);

        resume.setStatus(ResumeStatus.UPLOADED);

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

        return resumeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<ResumeResponse> searchResumes(String keyword) {

        return resumeRepository
                .findByFileNameContainingIgnoreCase(keyword)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ResumeResponse getResumeById(Long id) {

        Optional<Resume> optional = resumeRepository.findById(id);

        if (optional.isEmpty()) {
            throw new ResumeNotFoundException("Resume not found.");
        }

        Resume resume = optional.get();

        return mapToResponse(resume);
    }

    @Override
    public ResumeResponse updateResume(Long id,
                                       ResumeUploadRequest request) {

        Optional<Resume> optional =
                resumeRepository.findById(id);

        if (optional.isEmpty()) {
            throw new ResumeNotFoundException("Resume not found.");
        }

        Resume resume = optional.get();

        MultipartFile file = request.getFile();

        validateFile(file);

        String oldFilePath = resume.getFilePath();

        String newFilePath = saveFile(file);

        resume.setFileName(file.getOriginalFilename());
        resume.setFilePath(newFilePath);
        resume.setStatus(ResumeStatus.UPLOADED);

        Resume updatedResume =
                resumeRepository.save(resume);

        deleteFile(oldFilePath);

        return mapToResponse(updatedResume);
    }

    @Override
    public void deleteResume(Long id) {

        Optional<Resume> optional =
                resumeRepository.findById(id);

        if (optional.isEmpty()) {
            throw new ResumeNotFoundException("Resume not found.");
        }

        Resume resume = optional.get();

        String filePath = resume.getFilePath();

        resumeRepository.delete(resume);

        deleteFile(filePath);
    }

    private void deleteFile(String filePath) {

        Path path = Paths.get(filePath);

        try {

            Files.deleteIfExists(path);

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to delete old resume.", e);
        }
    }
}

