package com.example.WorkTopus.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMember {

    /*
     * USERS 테이블 숫자 PK
     */
    private Long userNum;

    /*
     * 로그인 문자열 아이디
     */
    private String userId;

    /*
     * 화면 표시 이름
     */
    private String name;

    /*
     * 현재 접속 여부
     */
    @Builder.Default
    private boolean online = false;

    /*
     * 프로젝트 생성자 또는 팀장 여부
     *
     * owner=true인 사용자에게
     * 프로젝트 목록에서 ⭐가 표시됩니다.
     */
    @Builder.Default
    private boolean owner = false;


    /*
     * 기존 임시 생성 코드 호환용
     */
    public ProjectMember(
            Long userNum,
            String name
    ) {
        this.userNum = userNum;
        this.userId = "";
        this.name = name;
        this.online = false;
        this.owner = false;
    }
}