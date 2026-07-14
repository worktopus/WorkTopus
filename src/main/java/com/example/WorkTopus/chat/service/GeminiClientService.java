package com.example.WorkTopus.chat.service;

import com.example.WorkTopus.chat.config.GeminiConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiClientService {

    /*
     * Gemini Interactions API 주소
     */
    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com"
                    + "/v1beta/interactions";


    private final GeminiConfig geminiConfig;


    /*
     * Gemini에 텍스트 요청
     */
    public String generateText(
            String prompt
    ) {
        validatePrompt(
                prompt
        );

        validateApiKey();


        /*
         * Gemini에 전달할 요청 본문
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
         *
         * API Key는 URL이나 JavaScript가 아니라
         * 서버에서 x-goog-api-key 헤더로 전달합니다.
         */
        Map<?, ?> response =
                RestClient.create()
                        .post()
                        .uri(
                                GEMINI_API_URL
                        )
                        .header(
                                "x-goog-api-key",
                                geminiConfig
                                        .getApiKey()
                        )
                        .contentType(
                                org.springframework
                                        .http
                                        .MediaType
                                        .APPLICATION_JSON
                        )
                        .body(
                                requestBody
                        )
                        .retrieve()
                        .body(
                                Map.class
                        );


        if (response == null) {
            throw new IllegalStateException(
                    "Gemini 응답이 없습니다."
            );
        }


        /*
         * Gemini Interactions API 응답에서
         * 최종 텍스트를 추출합니다.
         */
        String outputText =
                extractOutputText(
                        response
                );


        if (
                outputText == null ||
                        outputText.isBlank()
        ) {
            throw new IllegalStateException(
                    "Gemini 응답에서 텍스트를 찾을 수 없습니다."
            );
        }


        return outputText.trim();
    }


    /*
     * Gemini 응답 구조:
     *
     * steps
     *  └─ type: model_output
     *      └─ content
     *          └─ type: text
     *              text: 실제 응답
     */
    private String extractOutputText(
            Map<?, ?> response
    ) {
        Object stepsObject =
                response.get(
                        "steps"
                );


        if (
                !(stepsObject instanceof List<?> steps)
        ) {
            return null;
        }


        StringBuilder result =
                new StringBuilder();


        for (Object stepObject : steps) {

            if (
                    !(stepObject instanceof Map<?, ?> step)
            ) {
                continue;
            }


            Object stepType =
                    step.get(
                            "type"
                    );


            /*
             * thought 같은 내부 단계는 제외하고
             * 실제 모델 출력만 사용합니다.
             */
            if (
                    !"model_output".equals(
                            String.valueOf(
                                    stepType
                            )
                    )
            ) {
                continue;
            }


            Object contentObject =
                    step.get(
                            "content"
                    );


            if (
                    !(contentObject
                            instanceof List<?> contents)
            ) {
                continue;
            }


            for (
                    Object contentItem :
                    contents
            ) {

                if (
                        !(contentItem
                                instanceof Map<?, ?> content)
                ) {
                    continue;
                }


                Object contentType =
                        content.get(
                                "type"
                        );


                if (
                        !"text".equals(
                                String.valueOf(
                                        contentType
                                )
                        )
                ) {
                    continue;
                }


                Object text =
                        content.get(
                                "text"
                        );


                if (text == null) {
                    continue;
                }


                if (!result.isEmpty()) {
                    result.append(
                            System.lineSeparator()
                    );
                }


                result.append(
                        text
                );
            }
        }


        return result.toString();
    }


    /*
     * API Key 확인
     */
    private void validateApiKey() {

        if (
                !geminiConfig.hasApiKey()
        ) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY 환경변수가 설정되지 않았습니다."
            );
        }
    }


    /*
     * 요청 내용 확인
     */
    private void validatePrompt(
            String prompt
    ) {

        if (
                prompt == null ||
                        prompt.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Gemini에 전달할 내용이 없습니다."
            );
        }
    }
}