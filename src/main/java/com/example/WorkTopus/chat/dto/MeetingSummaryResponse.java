package com.example.WorkTopus.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingSummaryResponse {

    /*
     * 프로젝트 PK
     */
    private Long projectId;


    /*
     * 요약한 채팅방 ID
     */
    private String roomId;


    /*
     * AI가 생성한 전체 회의 요약
     */
    private String summary;


    /*
     * 주요 결정 사항
     */
    @Builder.Default
    private List<String> decisions =
            new ArrayList<>();


    /*
     * 해야 할 일
     */
    @Builder.Default
    private List<String> actionItems =
            new ArrayList<>();


    /*
     * 중요 키워드
     */
    @Builder.Default
    private List<String> keywords =
            new ArrayList<>();


    /*
     * 요약에 사용된 메시지 수
     */
    private int messageCount;


    /*
     * 요약 생성 시간
     */
    private OffsetDateTime generatedAt;
}