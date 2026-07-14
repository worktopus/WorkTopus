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
public class MeetingSummarySaveResponse {

    /*
     * 저장된 AI 회의록 PK
     */
    private Long summaryId;


    /*
     * 프로젝트 PK
     */
    private Long projectId;


    /*
     * 저장된 채팅방 ID
     */
    private String roomId;


    /*
     * 저장 완료 시간
     */
    private OffsetDateTime savedAt;


    /*
     * 사용자에게 보여줄 메시지
     */
    private String message;
}