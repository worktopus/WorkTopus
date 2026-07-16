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
     * 4-1 일반 관리 설정 데이터 저장 및 이미지 업로드 API
     */
    @PostMapping("/api/manage/{workspaceId}/general-update")
    public ResponseEntity<?> updateGeneralSettings(
            @PathVariable("workspaceId") Long workspaceId,
            @ModelAttribute WorkspaceGeneralUpdateDto dto) {
        try {
            // [디버깅 로그] 프론트에서 데이터가 정상적으로 넘어오는지 체크
            System.out.println("=========================================");
            System.out.println("▶ [API 요청 수신] Workspace ID : " + workspaceId);
            System.out.println("▶ [수신 데이터] 변경할 이름 : " + (dto != null ? dto.getWorkspaceName() : "null"));
            System.out.println("=========================================");

            Long currentUserId = 1L; // 임시 목업 아이디 고정값 유지
            workspaceManageService.updateGeneralSettings(workspaceId, dto, currentUserId);

            return ResponseEntity.ok().body(Map.of("message", "설정이 성공적으로 저장되었습니다."));
        } catch (Exception e) {
            // 서버 콘솔창에 빨간색으로 진짜 에러 이유를 강제로 출력하게 만듭니다.
            System.err.println("❌ [설정 저장 실패 서버 에러 로그]");
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 4-1 워크스페이스 전체 완전 삭제 API
     */
    @DeleteMapping("/api/manage/{workspaceId}")
    public ResponseEntity<?> deleteWorkspace(@PathVariable("workspaceId") Long workspaceId) {
        try {
            Long currentUserId = 1L;
            workspaceManageService.deleteWorkspace(workspaceId, currentUserId);
            return ResponseEntity.ok().body(Map.of("message", "완전 삭제 처리가 완료되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 4-2-1 기존 비동기 JSON 수신용 초대 API 주소
     */
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

    /**
     * 일반 HTML 폼 전송 방식 초대 기능
     */
    @PostMapping("/project/manage/invite/send")
    public ResponseEntity<?> handleFormSubmitInvite(
            @RequestParam(value = "emails", required = false) List<String> emails) {
        try {
            Long currentUserId = 1L;

            if (emails == null || emails.isEmpty()) {
                throw new IllegalArgumentException("입력된 이메일 데이터가 전송되지 않았습니다.");
            }

            WorkspaceInviteRequestDto dto = new WorkspaceInviteRequestDto();
            dto.setWorkspaceId(45L); // 현재 화면 주소에 맞춤형 연동
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

    /**
     * 4-2. 팀원 직급(역할) 비동기 수정 API
     */
    @PostMapping("/api/manage/member/role-update")
    public ResponseEntity<?> updateMemberRole(@RequestBody com.example.WorkTopus.manage.dto.ManageMemberRoleUpdateDto dto) {
        try {
            workspaceManageService.updateMemberRole(dto);
            return ResponseEntity.ok().body(Map.of("message", "팀원의 직급이 정상적으로 변경되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 4-2-2. 팀원 워크스페이스 제외(추방) 비동기 API
     */
    @DeleteMapping("/api/manage/member/{memberId}")
    public ResponseEntity<?> kickMember(@PathVariable("memberId") Long memberId) {
        try {
            workspaceManageService.kickMember(memberId);
            return ResponseEntity.ok().body(Map.of("message", "해당 팀원이 워크스페이스에서 제외되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 자바스크립트 렌더러용 실시간 팀원 목록 반환 API
     */
    @GetMapping("/api/manage/{workspaceId}/members-data")
    public ResponseEntity getMembersJsonData(
            @PathVariable("workspaceId") Long workspaceId) {
        List members = workspaceManageService.getWorkspaceMembers(workspaceId);
        return ResponseEntity.ok(members);
    }


}
