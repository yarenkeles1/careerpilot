package com.yaren.careerpilot.controller;

import com.yaren.careerpilot.dto.response.ResumeResponse;
import com.yaren.careerpilot.dto.response.ResumeUploadResponse;
import com.yaren.careerpilot.enums.ResumeStatus;
import com.yaren.careerpilot.exception.ResumeNotFoundException;
import com.yaren.careerpilot.service.ResumeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ResumeController.class)
class ResumeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResumeService resumeService;

    @Test
    void uploadResume_ShouldReturnCreated_WhenFileIsValid() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file", "cv.pdf", "application/pdf", "content".getBytes()
        );

        ResumeUploadResponse response = new ResumeUploadResponse();
        response.setResumeId(1L);
        response.setFileName("cv.pdf");
        response.setStatus(ResumeStatus.UPLOADED);

        when(resumeService.uploadResume(any()))
                .thenReturn(response);

        mockMvc.perform(multipart("/api/v1/resumes").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumeId").value(1L))
                .andExpect(jsonPath("$.fileName").value("cv.pdf"))
                .andExpect(jsonPath("$.status").value("UPLOADED"));
    }

    @Test
    void getAllResumes_ShouldReturnList() throws Exception {

        ResumeResponse response = new ResumeResponse();
        response.setId(1L);
        response.setFileName("cv.pdf");
        response.setStatus(ResumeStatus.UPLOADED);
        response.setUploadedAt(LocalDateTime.now());

        when(resumeService.getAllResumes())
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/resumes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].fileName").value("cv.pdf"));
    }

    @Test
    void getResumeById_ShouldReturnResume_WhenExists() throws Exception {

        ResumeResponse response = new ResumeResponse();
        response.setId(1L);
        response.setFileName("cv.pdf");
        response.setStatus(ResumeStatus.UPLOADED);

        when(resumeService.getResumeById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/resumes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getResumeById_ShouldReturnNotFound_WhenResumeDoesNotExist() throws Exception {

        when(resumeService.getResumeById(999L))
                .thenThrow(new ResumeNotFoundException("Resume not found."));

        mockMvc.perform(get("/api/v1/resumes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resume not found."));
    }

    @Test
    void searchResumes_ShouldRouteToSearchEndpoint_NotToGetById() throws Exception {

        ResumeResponse response = new ResumeResponse();
        response.setId(1L);
        response.setFileName("java-cv.pdf");

        when(resumeService.searchResumes("java"))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/resumes/search").param("keyword", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fileName").value("java-cv.pdf"));

        verify(resumeService).searchResumes("java");
    }

    @Test
    void deleteResume_ShouldReturnNoContent_WhenResumeExists() throws Exception {

        mockMvc.perform(delete("/api/v1/resumes/1"))
                .andExpect(status().isNoContent());

        verify(resumeService).deleteResume(1L);
    }

    @Test
    void deleteResume_ShouldReturnNotFound_WhenResumeDoesNotExist() throws Exception {

        org.mockito.Mockito.doThrow(new ResumeNotFoundException("Resume not found."))
                .when(resumeService).deleteResume(999L);

        mockMvc.perform(delete("/api/v1/resumes/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateResume_ShouldReturnOk_WhenFileIsValid() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file", "new-cv.pdf", "application/pdf", "content".getBytes()
        );

        ResumeResponse response = new ResumeResponse();
        response.setId(1L);
        response.setFileName("new-cv.pdf");
        response.setStatus(ResumeStatus.UPLOADED);

        when(resumeService.updateResume(eq(1L), any()))
                .thenReturn(response);

        var multipartRequest = multipart("/api/v1/resumes/1").file(file);
        multipartRequest.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        mockMvc.perform(multipartRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("new-cv.pdf"));
    }
}