package com.yaren.careerpilot.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ResumeUploadRequest {

    @NotNull(message = "Resume file is required.")
    private MultipartFile file;

}
