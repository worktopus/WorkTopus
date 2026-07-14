package com.example.WorkTopus.chat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Value("${gemini.api-key}")
    private String apiKey;


    /*
     * 실제 API 키 자체는 외부로 반환하지 않습니다.
     */
    public String getApiKey() {
        return apiKey;
    }


    /*
     * API 키가 설정되어 있는지만 확인합니다.
     */
    public boolean hasApiKey() {
        return apiKey != null &&
                !apiKey.isBlank();
    }
}