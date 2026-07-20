package com.example.WorkTopus.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingSummaryListItem {

    /*
     * 저장된 AI 회의록 PK
     *
     * 상세 조회할 때 사용합니다.
     */
    private Long summaryId;


    /*
     * 프로젝트 PK
     */
    private Long projectId;


    /*
     * 채팅방 ID
     */
    private String roomId;


    /*
     * 목록에서 보여줄 회의요약 내용
     *
     * 현재는 AI가 생성한 전체 요약을 사용합니다.
     */
    private String summary;


    /*
     * 분석한 메시지 개수
     */
    private Integer messageCount;


    /*
     * 회의록 생성 시간
     */
    private OffsetDateTime generatedAt;
}