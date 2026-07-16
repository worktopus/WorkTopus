package com.example.WorkTopus.chat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model:gemini-3.5-flash}")
    private String model;


    public String getApiKey() {
        return apiKey;
    }


    public String getModel() {
        return model;
    }


    public boolean hasApiKey() {
        return apiKey != null
                && !apiKey.isBlank();
    }
}