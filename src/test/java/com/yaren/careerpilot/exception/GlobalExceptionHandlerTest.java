package com.yaren.careerpilot.exception;

import com.yaren.careerpilot.exception.support.GlobalExceptionTestController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GlobalExceptionTestController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnBadRequest_WhenEmptyFileExceptionThrown() throws Exception {

        mockMvc.perform(get("/test/empty-file"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Resume file is required."))
                .andExpect(jsonPath("$.path").value("/test/empty-file"));
    }

    @Test
    void shouldReturnBadRequest_WhenInvalidFileExtensionExceptionThrown() throws Exception {

        mockMvc.perform(get("/test/invalid-extension"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Only PDF and DOCX files are allowed."));
    }

    @Test
    void shouldReturnBadRequest_WhenInvalidContentTypeExceptionThrown() throws Exception {

        mockMvc.perform(get("/test/invalid-content-type"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequest_WhenFileTooLargeExceptionThrown() throws Exception {

        mockMvc.perform(get("/test/too-large"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequest_WhenIllegalArgumentExceptionThrown() throws Exception {

        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("File name is missing."));
    }

    @Test
    void shouldReturnNotFound_WhenResumeNotFoundExceptionThrown() throws Exception {

        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Resume not found."))
                .andExpect(jsonPath("$.path").value("/test/not-found"));
    }

    @Test
    void shouldReturnInternalServerError_WhenUnexpectedExceptionThrown() throws Exception {

        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Unexpected error occurred."));
    }
}