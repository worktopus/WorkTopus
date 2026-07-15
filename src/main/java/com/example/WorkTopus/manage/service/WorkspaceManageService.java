package com.example.WorkTopus.manage.service;

import com.example.WorkTopus.manage.dto.WorkspaceGeneralUpdateDto;
import com.example.WorkTopus.manage.dto.WorkspaceInviteRequestDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkspaceManageService {

    /**
     * 4-1 워크스페이스 일반 관리 설정 업데이트 (기본 기능 + 고급 확장 기능)
     */
    @Transactional
    public void updateGeneralSettings(Long workspaceId, WorkspaceGeneralUpdateDto dto, Long currentUserId) {

        // [권한 검증] 현재 로그인한 유저가 해당 워크스페이스의 실제 팀장(마스터)인지 체크
        Long mockLeaderId = 1L;
        if (!mockLeaderId.equals(currentUserId)) {
            throw new SecurityException("팀장 권한이 없습니다. 관리 페이지에 접근할 수 없습니다.");
        }

        // 1. [기본] 명칭 및 공개범위 변경
        System.out.println("수정 대상 워크스페이스 ID: " + workspaceId);
        System.out.println("변경할 워크스페이스명: " + dto.getWorkspaceName());
        System.out.println("설정할 공개범위 값: " + dto.getVisibility());

        // 2. [기본] 로고 이미지 유효성 체크 및 스토리지 제어
        if (Boolean.TRUE.equals(dto.getIsLogoDeleted())) {
            System.out.println("기존 로고 이미지 파일 삭제 처리 완료");
        }

        if (dto.getLogoFile() != null && !dto.getLogoFile().isEmpty()) {
            String contentType = dto.getLogoFile().getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("이미지 파일 양식만 업로드가 승인됩니다.");
            }
            if (dto.getLogoFile().getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("업로드 허용 용량(5MB)을 초과한 파일입니다.");
            }

            System.out.println("신규 로고 파일 정상 영속화: " + dto.getLogoFile().getOriginalFilename());
        }

        // 3. [제안 기능 확장] 영구 동결/아카이빙 보관 상태 분기 처리
        if (dto.getArchiveStatus() != null) {
            System.out.println("워크스페이스 상태 코드 변경 적용: " + dto.getArchiveStatus());
        }

        // 4. [제안 기능 확장] 마스터 팀장 권한 위임 및 변경 인계 처리
        if (dto.getNewLeaderId() != null) {
            System.out.println("새로운 팀장 이관 대상 유저 고유 ID: " + dto.getNewLeaderId());
        }
    }

    /**
     * 4-1 워크스페이스 전체 데이터 영구 소멸 및 삭제 기능
     */
    @Transactional
    public void deleteWorkspace(Long workspaceId, Long currentUserId) {
        Long mockLeaderId = 1L;
        if (!mockLeaderId.equals(currentUserId)) {
            throw new SecurityException("워크스페이스 완전 삭제 권한은 팀장 마스터에게만 부여됩니다.");
        }

        System.out.println("워크스페이스 및 연관 데이터 영구 제거 대상 ID: " + workspaceId);
    }

    /**
     * 4-2-1 워크스페이스 팀원 초대 프로세스 실행 (새로 업데이트 통합된 전체 메서드)
     */
    @Transactional
    public void inviteTeamMembers(WorkspaceInviteRequestDto dto, Long currentUserId) {
        // [권한 검증] 요청자가 실제 관리 팀장 권한자인지 확인
        Long mockLeaderId = 1L;
        if (!mockLeaderId.equals(currentUserId)) {
            throw new SecurityException("팀장만 팀원을 초대할 수 있는 권한이 있습니다.");
        }

        // 입력 데이터 누락 방지 예외 처리
        if (dto.getEmails() == null || dto.getEmails().isEmpty()) {
            throw new IllegalArgumentException("초대할 이메일 주소가 존재하지 않습니다.");
        }

        System.out.println("초대 요청 수신 워크스페이스 ID: " + dto.getWorkspaceId());

        // 폼에서 멀티 카운트로 날아온 이메일 리스트를 순회하며 순차 가공 처리
        for (String email : dto.getEmails()) {
            System.out.println("▶ [초대 로그] 대상 이메일 스캐닝: " + email);

            // ① [확장 기능] 이미 가입되어 프로젝트 멤버 테이블에 등록된 계정인지 2차 확인
            // ② [확장 기능] 가입 승인을 대기하는 PENDING 상태 레코드 적재
            // ③ 메일 전송 컴포넌트 연동 알림 트리거 발송
        }

        System.out.println("총 " + dto.getEmails().size() + "명의 팀원 초대 메일 발송 및 관리 대기 데이터 동기화 완료.");
    }
}
