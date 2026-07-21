package com.example.WorkTopus.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresenceStatus {

    /*
     * null이면 사용자가 참여한 모든 프로젝트에
     * 같은 접속 상태를 적용합니다.
     */
    private Long projectId;

    private Long userNum;

    private String name;

    private boolean online;
}