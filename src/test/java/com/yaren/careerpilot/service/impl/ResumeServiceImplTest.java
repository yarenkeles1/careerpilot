package com.yaren.careerpilot.service.impl;

import com.yaren.careerpilot.dto.request.ResumeUploadRequest;
import com.yaren.careerpilot.dto.response.ResumeResponse;
import com.yaren.careerpilot.dto.response.ResumeUploadResponse;
import com.yaren.careerpilot.entity.Resume;
import com.yaren.careerpilot.enums.ResumeStatus;
import com.yaren.careerpilot.exception.*;
import com.yaren.careerpilot.repository.ResumeRepository;
import com.yaren.careerpilot.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceImplTest {

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ResumeServiceImpl resumeService;

    @Test
    void getResumeById_ShouldReturnResumeResponse_WhenResumeExists() {

        Resume resume = new Resume();
        resume.setId(1L);
        resume.setFileName("java-cv.pdf");

        when(resumeRepository.findById(1L))
                .thenReturn(Optional.of(resume));

        ResumeResponse response =
                resumeService.getResumeById(1L);

        assertEquals(1L, response.getId());
        assertEquals("java-cv.pdf", response.getFileName());
    }

    @Test
    void getResumeById_ShouldThrowException_WhenResumeDoesNotExist() {

        when(resumeRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResumeNotFoundException.class,
                () -> resumeService.getResumeById(999L)
        );

        verify(resumeRepository).findById(999L);
    }

    @Test
    void getAllResumes_ShouldReturnResumeResponseList_WhenResumesExist() {

        Resume resume1 = new Resume();
        resume1.setId(1L);
        resume1.setFileName("cv1.pdf");

        Resume resume2 = new Resume();
        resume2.setId(2L);
        resume2.setFileName("cv2.pdf");

        when(resumeRepository.findAll())
                .thenReturn(List.of(resume1, resume2));

        List<ResumeResponse> responses =
                resumeService.getAllResumes();

        assertEquals(2, responses.size());

        assertEquals(1L, responses.get(0).getId());
        assertEquals("cv1.pdf", responses.get(0).getFileName());

        assertEquals(2L, responses.get(1).getId());
        assertEquals("cv2.pdf", responses.get(1).getFileName());

        verify(resumeRepository).findAll();
    }

    @Test
    void getAllResumes_ShouldReturnEmptyList_WhenNoResumesExist() {

        when(resumeRepository.findAll())
                .thenReturn(List.of());

        List<ResumeResponse> responses =
                resumeService.getAllResumes();

        assertEquals(0, responses.size());

        verify(resumeRepository).findAll();
    }

    @Test
    void deleteResume_ShouldDeleteResume_WhenResumeExists() {

        Resume resume = new Resume();

        resume.setId(1L);
        resume.setFileName("cv.pdf");
        resume.setFilePath("uploads/cv.pdf");

        when(resumeRepository.findById(1L))
                .thenReturn(Optional.of(resume));

        resumeService.deleteResume(1L);

        verify(resumeRepository).findById(1L);
        verify(resumeRepository).delete(resume);
    }

    @Test
    void deleteResume_ShouldThrowException_WhenResumeDoesNotExist() {

        when(resumeRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResumeNotFoundException.class,
                () -> resumeService.deleteResume(999L)
        );

        verify(resumeRepository).findById(999L);

        verify(resumeRepository, never())
                .delete(any(Resume.class));
    }

    @Test
    void searchResumes_ShouldReturnMatchingResumes() {

        Resume resume1 = new Resume();
        resume1.setId(1L);
        resume1.setFileName("java-cv.pdf");

        Resume resume2 = new Resume();
        resume2.setId(2L);
        resume2.setFileName("java-backend-cv.pdf");

        when(resumeRepository.findByFileNameContainingIgnoreCase("java"))
                .thenReturn(List.of(resume1, resume2));

        List<ResumeResponse> responses =
                resumeService.searchResumes("java");

        assertEquals(2, responses.size());

        assertEquals(1L, responses.get(0).getId());
        assertEquals("java-cv.pdf", responses.get(0).getFileName());

        assertEquals(2L, responses.get(1).getId());
        assertEquals("java-backend-cv.pdf", responses.get(1).getFileName());

        verify(resumeRepository)
                .findByFileNameContainingIgnoreCase("java");
    }

    @Test
    void searchResumes_ShouldReturnEmptyList_WhenNoResumeMatches() {

        when(resumeRepository.findByFileNameContainingIgnoreCase("python"))
                .thenReturn(List.of());

        List<ResumeResponse> responses =
                resumeService.searchResumes("python");

        assertEquals(0, responses.size());

        verify(resumeRepository)
                .findByFileNameContainingIgnoreCase("python");
    }
    @Test
    void uploadResume_ShouldUploadResume_WhenFileIsValid() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.pdf",
                "application/pdf",
                "test resume content".getBytes()
        );

        ResumeUploadRequest request = new ResumeUploadRequest();
        request.setFile(file);

        Resume savedResume = new Resume();
        savedResume.setId(1L);
        savedResume.setFileName("cv.pdf");
        savedResume.setFilePath("uploads/test-cv.pdf");
        savedResume.setStatus(ResumeStatus.UPLOADED);

        when(resumeRepository.save(any(Resume.class)))
                .thenReturn(savedResume);

        ResumeUploadResponse response =
                resumeService.uploadResume(request);

        assertEquals(1L, response.getResumeId());
        assertEquals("cv.pdf", response.getFileName());
        assertEquals(ResumeStatus.UPLOADED, response.getStatus());

        verify(resumeRepository).save(any(Resume.class));
    }

    @Test
    void uploadResume_ShouldUploadResume_WhenPdfFileIsValid() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.pdf",
                "application/pdf",
                "test resume content".getBytes()
        );

        ResumeUploadRequest request = new ResumeUploadRequest();
        request.setFile(file);

        Resume savedResume = new Resume();
        savedResume.setId(10L);
        savedResume.setFileName("cv.pdf");
        savedResume.setFilePath("uploads/test.pdf");
        savedResume.setStatus(ResumeStatus.UPLOADED);

        when(resumeRepository.save(any(Resume.class)))
                .thenReturn(savedResume);

        ResumeUploadResponse response =
                resumeService.uploadResume(request);

        assertEquals(10L, response.getResumeId());
        assertEquals("cv.pdf", response.getFileName());
        assertEquals(ResumeStatus.UPLOADED, response.getStatus());

        verify(resumeRepository).save(any(Resume.class));
    }

    @Test
    void uploadResume_ShouldUploadResume_WhenDocxFileIsValid() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "test docx content".getBytes()
        );

        ResumeUploadRequest request = new ResumeUploadRequest();
        request.setFile(file);

        Resume savedResume = new Resume();
        savedResume.setId(11L);
        savedResume.setFileName("cv.docx");
        savedResume.setFilePath("uploads/test.docx");
        savedResume.setStatus(ResumeStatus.UPLOADED);

        when(resumeRepository.save(any(Resume.class)))
                .thenReturn(savedResume);

        ResumeUploadResponse response =
                resumeService.uploadResume(request);

        assertEquals(11L, response.getResumeId());
        assertEquals("cv.docx", response.getFileName());
        assertEquals(ResumeStatus.UPLOADED, response.getStatus());

        verify(resumeRepository).save(any(Resume.class));
    }

    @Test
    void uploadResume_ShouldThrowException_WhenFileIsEmpty() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.pdf",
                "application/pdf",
                new byte[0]
        );

        ResumeUploadRequest request = new ResumeUploadRequest();
        request.setFile(file);

        assertThrows(
                EmptyFileException.class,
                () -> resumeService.uploadResume(request)
        );

        verify(resumeRepository, never())
                .save(any(Resume.class));
    }

    @Test
    void uploadResume_ShouldThrowException_WhenFileIsNull() {

        ResumeUploadRequest request = new ResumeUploadRequest();
        request.setFile(null);

        assertThrows(
                EmptyFileException.class,
                () -> resumeService.uploadResume(request)
        );

        verify(resumeRepository, never())
                .save(any(Resume.class));
    }

    @Test
    void uploadResume_ShouldThrowException_WhenFileExtensionIsInvalid() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.txt",
                "text/plain",
                "test content".getBytes()
        );

        ResumeUploadRequest request = new ResumeUploadRequest();
        request.setFile(file);

        assertThrows(
                InvalidFileExtensionException.class,
                () -> resumeService.uploadResume(request)
        );

        verify(resumeRepository, never())
                .save(any(Resume.class));
    }

    @Test
    void uploadResume_ShouldThrowException_WhenFileNameIsMissing() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "",
                "application/pdf",
                "test content".getBytes()
        );

        ResumeUploadRequest request = new ResumeUploadRequest();
        request.setFile(file);

        assertThrows(
                IllegalArgumentException.class,
                () -> resumeService.uploadResume(request)
        );

        verify(resumeRepository, never())
                .save(any(Resume.class));
    }

    @Test
    void uploadResume_ShouldThrowException_WhenContentTypeIsInvalid() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.pdf",
                "text/plain",
                "test content".getBytes()
        );

        ResumeUploadRequest request = new ResumeUploadRequest();
        request.setFile(file);

        assertThrows(
                InvalidContentTypeException.class,
                () -> resumeService.uploadResume(request)
        );

        verify(resumeRepository, never())
                .save(any(Resume.class));
    }

    @Test
    void uploadResume_ShouldThrowException_WhenContentTypeIsMissing() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.pdf",
                null,
                "test content".getBytes()
        );

        ResumeUploadRequest request = new ResumeUploadRequest();
        request.setFile(file);

        assertThrows(
                IllegalArgumentException.class,
                () -> resumeService.uploadResume(request)
        );

        verify(resumeRepository, never())
                .save(any(Resume.class));
    }

    @Test
    void uploadResume_ShouldThrowException_WhenFileIsTooLarge() {

        byte[] largeFile = new byte[5 * 1024 * 1024 + 1];

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large-cv.pdf",
                "application/pdf",
                largeFile
        );

        ResumeUploadRequest request = new ResumeUploadRequest();
        request.setFile(file);

        assertThrows(
                FileTooLargeException.class,
                () -> resumeService.uploadResume(request)
        );

        verify(resumeRepository, never())
                .save(any(Resume.class));
    }

    @Test
    void updateResume_ShouldThrowException_WhenResumeDoesNotExist() {

        when(resumeRepository.findById(999L))
                .thenReturn(Optional.empty());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "new-cv.pdf",
                "application/pdf",
                "new resume".getBytes()
        );

        ResumeUploadRequest request = new ResumeUploadRequest();
        request.setFile(file);

        assertThrows(
                ResumeNotFoundException.class,
                () -> resumeService.updateResume(999L, request)
        );

        verify(resumeRepository).findById(999L);

        verify(resumeRepository, never())
                .save(any(Resume.class));
    }

    @Test
    void updateResume_ShouldThrowException_WhenNewFileIsInvalid() {

        Resume resume = new Resume();
        resume.setId(1L);
        resume.setFileName("old-cv.pdf");
        resume.setFilePath("uploads/old-cv.pdf");
        resume.setStatus(ResumeStatus.UPLOADED);

        when(resumeRepository.findById(1L))
                .thenReturn(Optional.of(resume));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.txt",
                "text/plain",
                "invalid".getBytes()
        );

        ResumeUploadRequest request = new ResumeUploadRequest();
        request.setFile(file);

        assertThrows(
                InvalidFileExtensionException.class,
                () -> resumeService.updateResume(1L, request)
        );

        verify(resumeRepository).findById(1L);

        verify(resumeRepository, never())
                .save(any(Resume.class));
    }

    @Test
    void updateResume_ShouldUpdateResume_WhenNewFileIsValid() {

        Resume resume = new Resume();
        resume.setId(1L);
        resume.setFileName("old-cv.pdf");
        resume.setFilePath("uploads/old-cv.pdf");
        resume.setStatus(ResumeStatus.UPLOADED);

        when(resumeRepository.findById(1L))
                .thenReturn(Optional.of(resume));

        MockMultipartFile newFile = new MockMultipartFile(
                "file", "new-cv.pdf", "application/pdf", "new resume".getBytes()
        );

        ResumeUploadRequest request = new ResumeUploadRequest();
        request.setFile(newFile);

        when(fileStorageService.store(newFile))
                .thenReturn("uploads/new-cv.pdf");

        when(resumeRepository.save(any(Resume.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResumeResponse response =
                resumeService.updateResume(1L, request);

        assertEquals(1L, response.getId());
        assertEquals("new-cv.pdf", response.getFileName());
        assertEquals(ResumeStatus.UPLOADED, response.getStatus());

        verify(resumeRepository).findById(1L);
        verify(resumeRepository).save(resume);
        verify(fileStorageService).store(newFile);
        verify(fileStorageService).delete("uploads/old-cv.pdf");
    }

    @Test
    void deleteResume_ShouldDeleteDatabaseRecordAndPhysicalFile_WhenResumeExists() {

        Resume resume = new Resume();
        resume.setId(1L);
        resume.setFileName("delete-test.pdf");
        resume.setFilePath("uploads/delete-test.pdf");

        when(resumeRepository.findById(1L))
                .thenReturn(Optional.of(resume));

        resumeService.deleteResume(1L);

        verify(resumeRepository).findById(1L);
        verify(resumeRepository).delete(resume);
        verify(fileStorageService).delete("uploads/delete-test.pdf");
    }

    @Test
    void uploadResume_ShouldPassFileStoragePathToRepository_WhenFileIsValid() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.pdf",
                "application/pdf",
                "test resume content".getBytes()
        );

        ResumeUploadRequest request = new ResumeUploadRequest();
        request.setFile(file);

        when(fileStorageService.store(file))
                .thenReturn("uploads/generated-uuid.pdf");

        when(resumeRepository.save(any(Resume.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Resume> captor = ArgumentCaptor.forClass(Resume.class);

        resumeService.uploadResume(request);

        verify(fileStorageService).store(file);
        verify(resumeRepository).save(captor.capture());

        Resume savedResume = captor.getValue();

        assertEquals("cv.pdf", savedResume.getFileName());
        assertEquals("uploads/generated-uuid.pdf", savedResume.getFilePath());
        assertEquals(ResumeStatus.UPLOADED, savedResume.getStatus());
    }

    @Test
    void uploadResume_ShouldUploadResume_WhenExtensionIsUppercase() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "CV.PDF",
                "application/pdf",
                "test resume content".getBytes()
        );

        ResumeUploadRequest request = new ResumeUploadRequest();
        request.setFile(file);

        when(fileStorageService.store(file))
                .thenReturn("uploads/generated-uuid.pdf");

        Resume savedResume = new Resume();
        savedResume.setId(1L);
        savedResume.setFileName("CV.PDF");
        savedResume.setStatus(ResumeStatus.UPLOADED);

        when(resumeRepository.save(any(Resume.class)))
                .thenReturn(savedResume);

        ResumeUploadResponse response =
                resumeService.uploadResume(request);

        assertEquals("CV.PDF", response.getFileName());
        assertEquals(ResumeStatus.UPLOADED, response.getStatus());
    }

    @Test
    void getResumeById_ShouldMapAllFields_WhenResumeExists() {

        Resume resume = new Resume();
        resume.setId(1L);
        resume.setFileName("java-cv.pdf");
        resume.setStatus(ResumeStatus.UPLOADED);

        when(resumeRepository.findById(1L))
                .thenReturn(Optional.of(resume));

        ResumeResponse response =
                resumeService.getResumeById(1L);

        assertEquals(1L, response.getId());
        assertEquals("java-cv.pdf", response.getFileName());
        assertEquals(ResumeStatus.UPLOADED, response.getStatus());
    }

    @Test
    void deleteResume_ShouldNotThrow_WhenFileStorageDeleteFails() {

        Resume resume = new Resume();
        resume.setId(1L);
        resume.setFileName("cv.pdf");
        resume.setFilePath("uploads/cv.pdf");

        when(resumeRepository.findById(1L))
                .thenReturn(Optional.of(resume));

        doThrow(new FileStorageException("disk error", new RuntimeException()))
                .when(fileStorageService).delete("uploads/cv.pdf");

        assertDoesNotThrow(() -> resumeService.deleteResume(1L));

        verify(resumeRepository).delete(resume);
        verify(fileStorageService).delete("uploads/cv.pdf");
    }

    @Test
    void updateResume_ShouldNotThrow_WhenOldFileDeleteFails() {

        Resume resume = new Resume();
        resume.setId(1L);
        resume.setFileName("old-cv.pdf");
        resume.setFilePath("uploads/old-cv.pdf");
        resume.setStatus(ResumeStatus.UPLOADED);

        when(resumeRepository.findById(1L))
                .thenReturn(Optional.of(resume));

        MockMultipartFile newFile = new MockMultipartFile(
                "file", "new-cv.pdf", "application/pdf", "new resume".getBytes()
        );

        ResumeUploadRequest request = new ResumeUploadRequest();
        request.setFile(newFile);

        when(fileStorageService.store(newFile))
                .thenReturn("uploads/new-cv.pdf");

        when(resumeRepository.save(any(Resume.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doThrow(new FileStorageException("disk error", new RuntimeException()))
                .when(fileStorageService).delete("uploads/old-cv.pdf");

        assertDoesNotThrow(() -> resumeService.updateResume(1L, request));

        verify(resumeRepository).save(resume);
        verify(fileStorageService).delete("uploads/old-cv.pdf");
    }
}