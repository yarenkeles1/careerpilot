package com.yaren.careerpilot.dto.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class ResumeUploadRequest {

    @NotNull(message = "Resume file is required.")
    private MultipartFile file;

}
