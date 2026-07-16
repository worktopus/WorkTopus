package com.example.WorkTopus.manage.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManageMemberRoleUpdateDto {
    private Long memberId;      // 변경 대상 팀원 매핑 테이블의 PK ID
    private String projectRole; // 변경할 새로운 직급 코드 (SUB_LEADER, MEMBER 등)
}
