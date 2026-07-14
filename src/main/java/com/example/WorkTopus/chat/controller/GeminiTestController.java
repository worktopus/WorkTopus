package com.example.WorkTopus.chat.controller;

import com.example.WorkTopus.chat.service.GeminiClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class GeminiTestController {

    private final GeminiClientService
            geminiClientService;


    /*
     * Gemini 연결 테스트
     *
     * POST
     * /api/ai/test
     */
    @PostMapping("/test")
    public Map<String, String> testGemini() {

        String result =
                geminiClientService
                        .generateText(
                                """
                                당신은 WorkTopus라는
                                프로젝트 협업 서비스의
                                AI 회의 도우미입니다.

                                한국어로 대답하세요.

                                "Gemini 연결 성공"이라는
                                의미가 포함된
                                짧은 문장 하나만 작성하세요.
                                """
                        );


        return Map.of(
                "result",
                result
        );
    }
}