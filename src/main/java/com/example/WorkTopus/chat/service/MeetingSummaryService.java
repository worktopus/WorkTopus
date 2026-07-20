package com.example.WorkTopus.chat.service;

import com.example.WorkTopus.chat.dto.ChatMessage;
import com.example.WorkTopus.chat.dto.MeetingSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingSummaryService {

    /*
     * DB에 저장된 채팅 메시지 조회
     */
    private final ChatService chatService;


    /*
     * Gemini API 호출
     */
    private final GeminiClientService geminiClientService;


    /*
     * Gemini가 반환한 JSON 문자열을
     * Java 객체로 변환할 때 사용합니다.
     *
     * 한 번 생성해서 계속 재사용합니다.
     */
    private final ObjectMapper objectMapper =
            new ObjectMapper();


    /*
     * 프로젝트 단체채팅을
     * Gemini로 회의요약합니다.
     *
     * 예:
     *
     * projectId = 2
     *
     * 조회 대상:
     * project_2_group
     */
    public MeetingSummaryResponse summarizeProjectGroup(
            Long projectId
    ) {

        validateProjectId(
                projectId
        );


        /*
         * 프로젝트 단체채팅방 ID
         */
        String roomId =
                "project_"
                        + projectId
                        + "_group";


        /*
         * CHAT_MESSAGE DB에서
         * 해당 프로젝트 단체채팅을 조회합니다.
         */
        List<ChatMessage> messages =
                chatService
                        .getProjectGroupMessages(
                                projectId
                        );


        /*
         * 요약할 메시지가 없는 경우
         */
        if (
                messages == null
                        || messages.isEmpty()
        ) {

            throw new IllegalStateException(
                    "요약할 단체 채팅 메시지가 없습니다."
            );
        }


        /*
         * 채팅 메시지를
         * Gemini에게 전달할 프롬프트로 변환합니다.
         */
        String prompt =
                buildSummaryPrompt(
                        projectId,
                        roomId,
                        messages
                );


        /*
         * Gemini API 호출
         */
        String geminiResponse =
                geminiClientService
                        .generateText(
                                prompt
                        );


        /*
         * Gemini JSON 응답을
         * MeetingSummaryResponse로 변환합니다.
         */
        return parseSummaryResponse(
                projectId,
                roomId,
                messages.size(),
                geminiResponse
        );
    }


    /*
     * 실제 채팅 메시지를
     * Gemini가 이해할 수 있는 프롬프트로 만듭니다.
     */
    private String buildSummaryPrompt(
            Long projectId,
            String roomId,
            List<ChatMessage> messages
    ) {

        StringBuilder chatContent =
                new StringBuilder();


        /*
         * DB에서 조회한 메시지를
         *
         * [시간] 이름: 메시지
         *
         * 형태로 변환합니다.
         */
        for (
                ChatMessage chatMessage :
                messages
        ) {

            if (chatMessage == null) {
                continue;
            }


            String message =
                    chatMessage.getMessage();


            /*
             * 내용 없는 메시지는
             * AI 요약 대상에서 제외
             */
            if (
                    message == null
                            || message.isBlank()
            ) {

                continue;
            }


            String senderName =
                    chatMessage.getSenderName();


            if (
                    senderName == null
                            || senderName.isBlank()
            ) {

                senderName =
                        "알 수 없는 사용자";
            }


            /*
             * 메시지 생성 시간이 있으면
             * 같이 전달합니다.
             */
            if (
                    chatMessage.getCreatedAt()
                            != null
            ) {

                chatContent
                        .append("[")
                        .append(
                                chatMessage
                                        .getCreatedAt()
                        )
                        .append("] ");
            }


            chatContent
                    .append(senderName)
                    .append(": ")
                    .append(
                            message.trim()
                    )
                    .append(
                            System.lineSeparator()
                    );
        }


        /*
         * 실제 Gemini 프롬프트
         *
         * 중요한 점:
         *
         * JSON 외의 설명을 하지 말라고
         * 명확하게 지시합니다.
         */
        return """
                당신은 WorkTopus 프로젝트 협업 서비스의
                AI 회의록 도우미입니다.

                아래 프로젝트 단체채팅 내용을 분석하고
                실제 회의에서 중요한 내용을 요약하세요.

                다음 기준을 반드시 지켜주세요.

                1. 단순한 인사나 잡담은 제외하세요.
                2. 프로젝트 진행과 관련된 핵심 내용을 요약하세요.
                3. 실제로 합의된 내용만 결정사항에 작성하세요.
                4. 담당자와 업무가 명확한 경우 할 일에 작성하세요.
                5. 존재하지 않는 내용은 만들어내지 마세요.
                6. 모든 내용은 한국어로 작성하세요.
                7. 반드시 아래 JSON 형식만 반환하세요.
                8. JSON 앞뒤에 설명이나 마크다운을 추가하지 마세요.

                반환 JSON 형식:

                {
                  "summary": "전체 회의 내용 요약",
                  "decisions": [
                    "결정 사항 1",
                    "결정 사항 2"
                  ],
                  "actionItems": [
                    "담당자: 해야 할 일",
                    "담당자: 해야 할 일"
                  ],
                  "keywords": [
                    "키워드1",
                    "키워드2",
                    "키워드3"
                  ]
                }

                프로젝트 ID:
                """
                + projectId
                + """



                채팅방 ID:
                """
                + roomId
                + """



                다음은 실제 프로젝트 단체채팅 내용입니다.

                --------------------
                """
                + chatContent
                + """

                --------------------

                위 채팅 내용만 근거로 분석하세요.
                반드시 JSON만 반환하세요.
                """;
    }


    /*
     * Gemini가 반환한 JSON을
     * MeetingSummaryResponse로 변환합니다.
     */
    private MeetingSummaryResponse
    parseSummaryResponse(
            Long projectId,
            String roomId,
            int messageCount,
            String geminiResponse
    ) {

        try {

            /*
             * 혹시 Gemini가
             *
             * ```json
             * {
             *   ...
             * }
             * ```
             *
             * 형태로 반환해도 처리할 수 있도록
             * JSON 부분만 정리합니다.
             */
            String json =
                    cleanJsonResponse(
                            geminiResponse
                    );


            /*
             * JSON 문자열 분석
             */
            JsonNode root =
                    objectMapper
                            .readTree(
                                    json
                            );


            /*
             * 전체 요약
             */
            String summary =
                    getTextValue(
                            root,
                            "summary"
                    );


            /*
             * 결정 사항
             */
            List<String> decisions =
                    getStringList(
                            root,
                            "decisions"
                    );


            /*
             * 할 일
             */
            List<String> actionItems =
                    getStringList(
                            root,
                            "actionItems"
                    );


            /*
             * 키워드
             */
            List<String> keywords =
                    getStringList(
                            root,
                            "keywords"
                    );


            /*
             * 최종 응답 DTO 생성
             */
            return MeetingSummaryResponse
                    .builder()
                    .projectId(
                            projectId
                    )
                    .roomId(
                            roomId
                    )
                    .summary(
                            summary
                    )
                    .decisions(
                            decisions
                    )
                    .actionItems(
                            actionItems
                    )
                    .keywords(
                            keywords
                    )
                    .messageCount(
                            messageCount
                    )
                    .generatedAt(
                            OffsetDateTime.now()
                    )
                    .build();

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Gemini 회의요약 응답을 JSON으로 변환하지 못했습니다."
                            + System.lineSeparator()
                            + "Gemini 응답: "
                            + geminiResponse,
                    exception
            );
        }
    }


    /*
     * JSON 문자열 정리
     *
     * 정상:
     *
     * {
     *   "summary": "..."
     * }
     *
     *
     * 마크다운이 포함된 경우:
     *
     * ```json
     * {
     *   "summary": "..."
     * }
     * ```
     *
     * 둘 다 처리합니다.
     */
    private String cleanJsonResponse(
            String response
    ) {

        if (
                response == null
                        || response.isBlank()
        ) {

            throw new IllegalStateException(
                    "Gemini 회의요약 응답이 없습니다."
            );
        }


        String cleaned =
                response.trim();


        /*
         * ```json 제거
         */
        if (
                cleaned.startsWith(
                        "```json"
                )
        ) {

            cleaned =
                    cleaned.substring(
                            7
                    );
        }


        /*
         * ``` 제거
         */
        if (
                cleaned.startsWith(
                        "```"
                )
        ) {

            cleaned =
                    cleaned.substring(
                            3
                    );
        }


        if (
                cleaned.endsWith(
                        "```"
                )
        ) {

            cleaned =
                    cleaned.substring(
                            0,
                            cleaned.length()
                                    - 3
                    );
        }


        cleaned =
                cleaned.trim();


        /*
         * JSON 앞뒤에 혹시 설명이 붙어 있으면
         * 첫 번째 { 부터 마지막 } 까지만 사용합니다.
         */
        int jsonStart =
                cleaned.indexOf(
                        "{"
                );


        int jsonEnd =
                cleaned.lastIndexOf(
                        "}"
                );


        if (
                jsonStart < 0
                        || jsonEnd < 0
                        || jsonStart > jsonEnd
        ) {

            throw new IllegalStateException(
                    "Gemini 응답에서 JSON을 찾을 수 없습니다."
            );
        }


        return cleaned.substring(
                jsonStart,
                jsonEnd + 1
        );
    }


    /*
     * JSON 문자열 값 조회
     *
     * 예:
     *
     * "summary": "회의 내용"
     */
    private String getTextValue(
            JsonNode root,
            String fieldName
    ) {

        JsonNode value =
                root.get(
                        fieldName
                );


        if (
                value == null
                        || value.isNull()
        ) {

            return "";
        }


        return value
                .asText()
                .trim();
    }


    /*
     * JSON 배열을
     * List<String>으로 변환합니다.
     *
     * 예:
     *
     * "decisions": [
     *   "통합 테스트 진행",
     *   "DB 연결 완료"
     * ]
     */
    private List<String> getStringList(
            JsonNode root,
            String fieldName
    ) {

        List<String> result =
                new ArrayList<>();


        JsonNode arrayNode =
                root.get(
                        fieldName
                );


        if (
                arrayNode == null
                        || !arrayNode.isArray()
        ) {

            return result;
        }


        for (
                JsonNode item :
                arrayNode
        ) {

            if (
                    item == null
                            || item.isNull()
            ) {

                continue;
            }


            String value =
                    item
                            .asText()
                            .trim();


            if (
                    !value.isBlank()
            ) {

                result.add(
                        value
                );
            }
        }


        return result;
    }


    /*
     * 프로젝트 ID 검증
     */
    private void validateProjectId(
            Long projectId
    ) {

        if (
                projectId == null
                        || projectId <= 0
        ) {

            throw new IllegalArgumentException(
                    "올바른 projectId가 필요합니다."
            );
        }
    }
}