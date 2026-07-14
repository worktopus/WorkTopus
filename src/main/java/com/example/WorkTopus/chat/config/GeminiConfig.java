package com.example.WorkTopus.chat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;


    /*
     * Gemini API Key
     *
     * 외부 Controller나 브라우저로
     * 직접 반환하면 안 됩니다.
     */
    public String getApiKey() {
        return apiKey;
    }


    /*
     * 사용할 Gemini 모델
     */
    public String getModel() {
        return model;
    }


    /*
     * API Key 설정 여부 확인
     */
    public boolean hasApiKey() {
        return apiKey != null &&
                !apiKey.isBlank();
    }
}