package com.yaren.careerpilot.dto.response;

import com.yaren.careerpilot.enums.ResumeStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
public class ResumeResponse {

    private Long id;

    private String fileName;

    private ResumeStatus status;

    private LocalDateTime uploadedAt;
}
