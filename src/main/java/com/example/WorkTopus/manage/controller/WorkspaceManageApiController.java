package com.example.WorkTopus.manage.controller;

import com.example.WorkTopus.manage.dto.WorkspaceGeneralUpdateDto;
import com.example.WorkTopus.manage.dto.WorkspaceInviteRequestDto;
import com.example.WorkTopus.manage.service.WorkspaceManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/manage")
@RequiredArgsConstructor
public class WorkspaceManageApiController {

    private final WorkspaceManageService workspaceManageService;

    /**
     * 4-1 일반 관리 설정 데이터 저장 및 이미지 업로드 API
     */
    @PostMapping("/{workspaceId}/general-update")
    public ResponseEntity<?> updateGeneralSettings(
            @PathVariable("workspaceId") Long workspaceId,
            @ModelAttribute WorkspaceGeneralUpdateDto dto) {

        try {
            // 현재 로그인한 팀장의 유저 고유 ID (인증 모듈 연동 전 테스트용 1L 처리)
            Long currentUserId = 1L;

            workspaceManageService.updateGeneralSettings(workspaceId, dto, currentUserId);
            return ResponseEntity.ok().body(Map.of("message", "설정이 성공적으로 저장되었습니다."));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 4-1 워크스페이스(프로젝트) 전체 완전 삭제 API
     */
    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<?> deleteWorkspace(@PathVariable("workspaceId") Long workspaceId) {
        try {
            Long currentUserId = 1L;
            workspaceManageService.deleteWorkspace(workspaceId, currentUserId);
            return ResponseEntity.ok().body(Map.of("message", "완전 삭제 처리가 완료되었습니다."));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "삭제 처리 중 서버 오류가 발생했습니다."));
        }
    }

    /**
     * 4-2-1 팀원 초대 이메일 리스트 발송 및 승인 대기 처리 API
     * (JSON 규격 수신을 위해 @RequestBody 매핑 필수)
     */
    @PostMapping("/invite")
    public ResponseEntity<?> inviteTeamMembers(@RequestBody WorkspaceInviteRequestDto dto) {
        try {
            // 인증 전 팀장 유저 ID 강제 매핑 1L
            Long currentUserId = 1L;

            workspaceManageService.inviteTeamMembers(dto, currentUserId);
            return ResponseEntity.ok().body(Map.of("message", "선택한 이메일로 팀원 초대가 정상적으로 완료되었습니다."));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "초대 발송 처리 중 서버 내부 오류가 발생했습니다."));
        }
    }
}
