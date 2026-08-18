package com.yaren.careerpilot.exception.support;

import com.yaren.careerpilot.exception.EmptyFileException;
import com.yaren.careerpilot.exception.FileTooLargeException;
import com.yaren.careerpilot.exception.InvalidContentTypeException;
import com.yaren.careerpilot.exception.InvalidFileExtensionException;
import com.yaren.careerpilot.exception.ResumeNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GlobalExceptionTestController {

    @GetMapping("/test/empty-file")
    public void throwEmptyFile() {
        throw new EmptyFileException("Resume file is required.");
    }

    @GetMapping("/test/invalid-extension")
    public void throwInvalidExtension() {
        throw new InvalidFileExtensionException("Only PDF and DOCX files are allowed.");
    }

    @GetMapping("/test/invalid-content-type")
    public void throwInvalidContentType() {
        throw new InvalidContentTypeException("Only PDF and DOCX files are allowed.");
    }

    @GetMapping("/test/too-large")
    public void throwTooLarge() {
        throw new FileTooLargeException("Maximum file size is 5 MB.");
    }

    @GetMapping("/test/illegal-argument")
    public void throwIllegalArgument() {
        throw new IllegalArgumentException("File name is missing.");
    }

    @GetMapping("/test/not-found")
    public void throwNotFound() {
        throw new ResumeNotFoundException("Resume not found.");
    }

    @GetMapping("/test/unexpected")
    public void throwUnexpected() {
        throw new RuntimeException("Something went wrong.");
    }
}