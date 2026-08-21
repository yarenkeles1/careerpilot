package com.yaren.careerpilot.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

@Configuration
public class AiConfig {

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("qwen3:8b")
                .temperature(0.3)
                .timeout(Duration.ofSeconds(300))
                .build();
    }

    @Bean
    public com.yaren.careerpilot.service.ResumeAnalyzerAiService resumeAnalyzerAiService(ChatLanguageModel chatLanguageModel) {
        return dev.langchain4j.service.AiServices.builder(com.yaren.careerpilot.service.ResumeAnalyzerAiService.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }

    @Bean
    public com.yaren.careerpilot.service.JobMatcherAiService jobMatcherAiService(ChatLanguageModel chatLanguageModel) {
        return dev.langchain4j.service.AiServices.builder(com.yaren.careerpilot.service.JobMatcherAiService.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }
}