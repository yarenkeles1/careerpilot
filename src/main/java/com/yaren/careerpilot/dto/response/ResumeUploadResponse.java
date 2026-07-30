package com.yaren.careerpilot.dto.response;

import com.yaren.careerpilot.enums.ResumeStatus;
import lombok.Data;

@Data
public class ResumeUploadResponse {

        private Long resumeId;

        private String fileName;

        private ResumeStatus status;
}
