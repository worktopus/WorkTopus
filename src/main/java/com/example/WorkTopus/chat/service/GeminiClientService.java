package com.example.WorkTopus.chat.service;

import com.example.WorkTopus.chat.config.GeminiConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiClientService {

    /*
     * Gemini Interactions API
     */
    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/interactions";


    private final GeminiConfig geminiConfig;


    /*
     * Gemini에 텍스트를 전달하고
     * 최종 텍스트 응답을 반환합니다.
     */
    public String generateText(
            String prompt
    ) {

        validatePrompt(prompt);
        validateApiKey();


        /*
         * Gemini 요청 JSON
         *
         * {
         *   "model": "gemini-3.5-flash",
         *   "input": "질문 내용"
         * }
         */
        Map<String, Object> requestBody =
                Map.of(
                        "model",
                        geminiConfig.getModel(),

                        "input",
                        prompt.trim()
                );


        /*
         * Gemini API 호출
         */
        Map<?, ?> response =
                RestClient
                        .create()
                        .post()
                        .uri(GEMINI_API_URL)
                        .header(
                                "x-goog-api-key",
                                geminiConfig.getApiKey()
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .body(requestBody)
                        .retrieve()
                        .body(Map.class);


        if (response == null) {

            throw new IllegalStateException(
                    "Gemini 응답이 없습니다."
            );
        }


        String result =
                extractOutputText(response);


        if (
                result == null
                        || result.isBlank()
        ) {

            throw new IllegalStateException(
                    "Gemini 응답에서 텍스트를 찾을 수 없습니다."
            );
        }


        return result.trim();
    }


    /*
     * Gemini 응답 구조에서
     * model_output → content → text
     * 값을 추출합니다.
     */
    private String extractOutputText(
            Map<?, ?> response
    ) {

        Object stepsObject =
                response.get("steps");


        if (
                !(stepsObject instanceof List<?> steps)
        ) {

            return null;
        }


        StringBuilder result =
                new StringBuilder();


        for (
                Object stepObject :
                steps
        ) {

            if (
                    !(stepObject
                            instanceof Map<?, ?> step)
            ) {

                continue;
            }


            /*
             * thought 등은 제외하고
             * 실제 모델 출력만 사용
             */
            if (
                    !"model_output".equals(
                            String.valueOf(
                                    step.get("type")
                            )
                    )
            ) {

                continue;
            }


            Object contentObject =
                    step.get("content");


            if (
                    !(contentObject
                            instanceof List<?> contents)
            ) {

                continue;
            }


            for (
                    Object contentObjectItem :
                    contents
            ) {

                if (
                        !(contentObjectItem
                                instanceof Map<?, ?> content)
                ) {

                    continue;
                }


                if (
                        !"text".equals(
                                String.valueOf(
                                        content.get("type")
                                )
                        )
                ) {

                    continue;
                }


                Object text =
                        content.get("text");


                if (text == null) {

                    continue;
                }


                if (!result.isEmpty()) {

                    result.append(
                            System.lineSeparator()
                    );
                }


                result.append(text);
            }
        }


        return result.toString();
    }


    private void validateApiKey() {

        if (
                !geminiConfig.hasApiKey()
        ) {

            throw new IllegalStateException(
                    "GEMINI_API_KEY 환경변수가 설정되지 않았습니다."
            );
        }
    }


    private void validatePrompt(
            String prompt
    ) {

        if (
                prompt == null
                        || prompt.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Gemini에 전달할 내용이 없습니다."
            );
        }
    }
}