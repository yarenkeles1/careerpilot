package com.yaren.careerpilot.service.impl;

import com.yaren.careerpilot.service.ResumeParserService;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class ResumeParserServiceImpl implements ResumeParserService {
    @Override
    public String extractText(MultipartFile file) {
        try (InputStream stream = file.getInputStream()) {
            Tika tika = new Tika();

            return tika.parseToString(stream);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while reading the file: " + e.getMessage(), e);
        }
    }
}
