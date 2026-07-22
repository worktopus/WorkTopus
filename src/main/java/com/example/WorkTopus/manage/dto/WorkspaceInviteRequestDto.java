package com.example.WorkTopus.manage.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class WorkspaceInviteRequestDto {

    // 초대 대상 워크스페이스 고유 ID
    private Long workspaceId;

    // 4-2-1 화면에서 동적으로 추가 입력된 초대 타겟 이메일 리스트
    private List<String> emails;

    // [보완 추가] manage-invite.js 에서 동적으로 작성하여 밀어주는 단일 이메일 변수명 동기화
    private String email;

    // [보완 추가] 사용자가 화면에 직접 입력한 초대 메시지 본문 수집 필드
    private String message;

    // [보완 추가] 사용자가 화면에 직접 입력한 수동 인증 코드 수집 필드
    private String code;
}
