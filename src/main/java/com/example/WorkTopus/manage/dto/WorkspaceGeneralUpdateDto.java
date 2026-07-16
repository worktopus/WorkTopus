package com.example.WorkTopus.manage.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class WorkspaceGeneralUpdateDto {

    // 4-1 일반 관리 폼 바인딩 필드
    private String workspaceName;      // 수정용 워크스페이스 명칭
    private String visibility;         // 공개 범위 (PUBLIC, PRIVATE 등)
    private Boolean isLogoDeleted;     // 기존 이미지 삭제 수행 여부 플래그
    private MultipartFile logoFile;    // 업로드된 새 로고 파일 객체

    // [제안 기능 확장 데이터]
    private String archiveStatus;      // 프로젝트 영구 동결/보관 상태코드 (ACTIVE / ARCHIVED)
    private Long newLeaderId;          // 권한 전역 위임 처리용 인계 대상 팀원 ID
}
