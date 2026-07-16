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
}
