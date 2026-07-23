package com.example.WorkTopus.manage.controller;

import com.example.WorkTopus.manage.entity.Manage;
import com.example.WorkTopus.manage.dto.WorkspaceGeneralUpdateDto;
import com.example.WorkTopus.manage.dto.WorkspaceInviteRequestDto;
import com.example.WorkTopus.manage.service.WorkspaceManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class WorkspaceManageApiController {

    private final WorkspaceManageService workspaceManageService;

    /**
     * [4-1 변경 사항] 프로젝트 이름 단독 비동기 수정 API
     * 기존 general-update 주소를 이름 전용 파이프라인으로 매핑을 명확히 고정합니다.
     */
    @PostMapping("/api/manage/{workspaceId}/update-name")
    public ResponseEntity<?> updateProjectName(
            @PathVariable("workspaceId") Long workspaceId,
            @ModelAttribute WorkspaceGeneralUpdateDto dto) {
        try {
            System.out.println("=========================================");
            System.out.println("▶ [이름 수정 API 요청] Workspace ID : " + workspaceId);
            System.out.println("▶ [수신 데이터] 변경할 이름 : " + (dto != null ? dto.getWorkspaceName() : "null"));
            System.out.println("=========================================");

            Long currentUserId = 1L;
            workspaceManageService.updateGeneralSettings(workspaceId, dto, currentUserId);
            return ResponseEntity.ok().body(Map.of("message", "프로젝트 이름이 성공적으로 저장되었습니다."));
        } catch (Exception e) {
            System.err.println("❌ [이름 저장 실패 서버 에러 로그]");
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 📌  프로젝트 내용(설명) 단독 비동기 수정 API
     * 말씀하신 대로 기존 워크스페이스 전체 완전 삭제(@DeleteMapping) 자리를 대체하여 깔끔하게 조립했습니다.
     */
    @PostMapping("/api/manage/{workspaceId}/update-description")
    public ResponseEntity<?> updateProjectDescription(
            @PathVariable("workspaceId") Long workspaceId,
            @ModelAttribute WorkspaceGeneralUpdateDto dto) {
        try {
            System.out.println("=========================================");
            System.out.println("▶ [내용 수정 API 요청] Workspace ID : " + workspaceId);
            System.out.println("▶ [수신 데이터] 변경할 내용 : " + (dto != null ? dto.getProjectDescription() : "null"));
            System.out.println("=========================================");

            Long currentUserId = 1L;
            workspaceManageService.updateGeneralSettings(workspaceId, dto, currentUserId);
            return ResponseEntity.ok().body(Map.of("message", "프로젝트 내용이 성공적으로 저장되었습니다."));
        } catch (Exception e) {
            System.err.println("❌ [내용 저장 실패 서버 에러 로그]");
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**  기존 비동기 JSON 수신용 초대 API 주소 (기존 유지) */
    @PostMapping("/api/manage/invite")
    public ResponseEntity<?> inviteTeamMembers(@RequestBody WorkspaceInviteRequestDto dto) {
        try {
            Long currentUserId = 1L;
            workspaceManageService.inviteTeamMembers(dto, currentUserId);
            return ResponseEntity.ok().body(Map.of("message", "팀원 초자가 정상적으로 완료되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /** 일반 HTML 폼 전송 방식 초대 기능 (기존 유지) */
    @PostMapping("/project/manage/invite/send")
    public ResponseEntity<?> handleFormSubmitInvite(
            @RequestParam(value = "emails", required = false) List<String> emails) {
        try {
            Long currentUserId = 1L;
            if (emails == null || emails.isEmpty()) {
                throw new IllegalArgumentException("입력된 이메일 데이터가 전송되지 않았습니다.");
            }
            WorkspaceInviteRequestDto dto = new WorkspaceInviteRequestDto();
            dto.setWorkspaceId(45L);
            dto.setEmails(emails);
            workspaceManageService.inviteTeamMembers(dto, currentUserId);
            return ResponseEntity.ok().body(
                    "<div style='padding: 40px; text-align: center; font-family: sans-serif; line-height: 1.6;'>" +
                            "   <h2 style='color: #28a745; margin-bottom: 10px;'>🎉 구글 이메일 초대장 발송 완료!</h2>" +
                            "   <p style='color: #555; margin-bottom: 25px;'>선택하신 팀원들의 이메일 계정으로 초대 메세지가 성공적으로 전송되었습니다.</p>" +
                            "   <a href='/manage/45/members' style='display: inline-block; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 4px; font-weight: bold;'>[팀원 관리 페이지로 이동]</a>" +
                            "</div>"
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    "<div style='padding: 40px; text-align: center; font-family: sans-serif; color: #dc3545;'>" +
                            "   <h3>❌ 초대장 발송 실패</h3>" +
                            "   <p>" + e.getMessage() + "</p>" +
                            "   <br><a href='javascript:history.back()' style='color: #007bff;'>[뒤로 가기]</a>" +
                            "</div>"
            );
        }
    }

    /** 팀원 직급(역할) 비동기 수정 API (기존 유지) */
    @PostMapping("/api/manage/member/role-update")
    public ResponseEntity<?> updateMemberRole(@RequestBody com.example.WorkTopus.manage.dto.ManageMemberRoleUpdateDto dto) {
        try {
            workspaceManageService.updateMemberRole(dto);
            return ResponseEntity.ok().body(Map.of("message", "팀원의 직급이 정상적으로 변경되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**   팀원 워크스페이스 제외(추방) 비동기 API (기존 유지) */
    @DeleteMapping("/api/manage/member/{memberId}")
    public ResponseEntity<?> kickMember(@PathVariable("memberId") Long memberId) {
        try {
            workspaceManageService.kickMember(memberId);
            return ResponseEntity.ok().body(Map.of("message", "해당 팀원이 워크스페이스에서 제외되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** 자바스크립트 렌더러용 실시간 팀원 목록 반환 API (기존 유지) */
    @GetMapping("/api/manage/{workspaceId}/members-data")
    public ResponseEntity<?> getMembersJsonData(
            @PathVariable("workspaceId") Long workspaceId) {
        List<?> members = workspaceManageService.getWorkspaceMembers(workspaceId);
        return ResponseEntity.ok(members);
    }

    /** [게시판관리 - 이름 비동기 수정 연동 API] (기존 유지) */
    @PostMapping("/api/manage/board/update-name")
    public ResponseEntity<?> updateBoardName(@RequestBody Map<String, Object> payload) {
        try {
            Long boardId = Long.parseLong(payload.get("boardId").toString());
            String boardName = payload.get("boardName").toString();

            // workspaceManageService.updateBoardName(boardId, boardName);

            return ResponseEntity.ok().body(Map.of("status", "SUCCESS"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** [게시판관리 - 안전 숨김 조치 및 후속 정책 연동 API] (기존 유지) */
    @DeleteMapping("/api/manage/board/{boardId}/hide-policy")
    public ResponseEntity<?> hideBoardWithPolicy(
            @PathVariable("boardId") Long boardId,
            @RequestBody Map<String, String> payload) {
        try {
            String actionPolicy = payload.get("actionPolicy");

            // workspaceManageService.hideBoardWithPolicy(boardId, actionPolicy);

            return ResponseEntity.ok().body(Map.of("status", "SUCCESS"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * [보완 가동] 담당 역할 비동기 저장 API (자동 저장 + 버튼 클릭 공용 파이프라인)
     * 자바스크립트에서 넘어오는 데이터의 Null 값 방지 처리를 추가하여 오라클 안정성을 제고했습니다.
     */
    @PostMapping("/api/manage/member/task-update")
    public ResponseEntity<?> updateMemberTask(@RequestBody Map<String, Object> payload) {
        try {
            if (payload == null || !payload.containsKey("memberId")) {
                throw new IllegalArgumentException("필수 요청 데이터가 누락되었습니다.");
            }

            Long memberId = Long.parseLong(payload.get("memberId").toString());
            // 역할 값이 공백이거나 없을 경우 오라클에 빈 값 처리가 정상 반영되도록 안전 필터링
            String assignedRole = payload.get("assignedRole") != null ? payload.get("assignedRole").toString() : "";

            System.out.println("=========================================");
            System.out.println("▶ [담당 역할 저장 API 가동]");
            System.out.println("▶ 대상 회원 ID (memberId) : " + memberId);
            System.out.println("▶ 반영할 역할 (assignedRole) : '" + assignedRole + "'");
            System.out.println("=========================================");

            // 서비스 레이어 호출 (Dirty Checking 또는 MyBatis Mapper 연동 가동)
            workspaceManageService.updateMemberTask(memberId, assignedRole);

            return ResponseEntity.ok().body(Map.of("message", "SUCCESS"));
        } catch (Exception e) {
            System.err.println("❌ [담당 역할 오라클 반영 실패 에러 로그]");
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
