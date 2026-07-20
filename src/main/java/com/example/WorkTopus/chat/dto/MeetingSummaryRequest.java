package com.example.WorkTopus.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingSummaryRequest {

    /*
     * 프로젝트 PK
     *
     * 현재 테스트:
     * projectId = 2
     *
     * 실제 통합 후:
     * Projects.id 값
     */
    private Long projectId;


    /*
     * 요약할 채팅방 ID
     *
     * AI 회의요약은 기본적으로
     * 프로젝트 단체 채팅방을 대상으로 합니다.
     *
     * 예:
     * project_2_group
     */
    private String roomId;


    /*
     * 요약에 사용할 메시지 목록
     */
    @Builder.Default
    private List<ChatMessage> messages =
            new ArrayList<>();
}