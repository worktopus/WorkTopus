package com.example.WorkTopus.manage.service;

import com.example.WorkTopus.manage.entity.Manage;
import com.example.WorkTopus.manage.entity.ManageMember;
import com.example.WorkTopus.manage.dto.WorkspaceGeneralUpdateDto;
import com.example.WorkTopus.manage.dto.WorkspaceInviteRequestDto;
import com.example.WorkTopus.manage.dto.ManageMemberRoleUpdateDto;
import com.example.WorkTopus.manage.repository.ManageRepository;
import com.example.WorkTopus.manage.repository.ManageMemberRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceManageService {

    private final JavaMailSender mailSender;
    private final ManageRepository manageRepository;
    private final ManageMemberRepository manageMemberRepository;

    @PersistenceContext
    private final EntityManager em;

    /**
     * 특정 워크스페이스에 참여 중인 전체 팀원 목록 조회
     */
    public List<ManageMember> getWorkspaceMembers(Long workspaceId) {
        return manageMemberRepository.findByWorkspaceId(workspaceId);
    }

    /**
     * 팀원 직급 수정 비즈니스 로직
     */
    @Transactional
    public void updateMemberRole(ManageMemberRoleUpdateDto dto) {
        ManageMember member = manageMemberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("해당 팀원 정보가 존재하지 않습니다. ID: " + dto.getMemberId()));

        if ("LEADER".equals(member.getProjectRole())) {
            throw new IllegalStateException("팀장의 직급은 강제로 변경할 수 없습니다.");
        }

        member.updateProjectRole(dto.getProjectRole());
    }

    /**
     * 팀원 제외 비즈니스 로직
     */
    @Transactional
    public void kickMember(Long memberId) {
        ManageMember member = manageMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 팀원 정보가 존재하지 않습니다. ID: " + memberId));

        if ("LEADER".equals(member.getProjectRole())) {
            throw new IllegalStateException("팀장은 워크스페이스에서 제외할 수 없습니다.");
        }

        manageMemberRepository.delete(member);
    }

    /**
     * 4-1 워크스페이스 일반 관리 설정 업데이트 (강제 삽입 구역 완전 제거 완료)
     */
    @Transactional
    public void updateGeneralSettings(Long workspaceId, WorkspaceGeneralUpdateDto dto, Long currentUserId) {
        Long mockLeaderId = 1L;
        if (!mockLeaderId.equals(currentUserId)) {
            throw new SecurityException("팀장 권한이 없습니다.");
        }

        // [교정] 오라클 데이터 독립성 유지를 위해 기존의 강제 네이티브 INSERT 구역을 완전히 들어냈습니다.

        Manage manage = manageRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트 관리 데이터가 존재하지 않습니다. ID: " + workspaceId));

        manage.updateGeneralSettings(dto.getWorkspaceName(), dto.getVisibility(), dto.getArchiveStatus());

        if (dto.getNewLeaderId() != null) {
            manage.setOwnerId(dto.getNewLeaderId());
        }

        if (Boolean.TRUE.equals(dto.getIsLogoDeleted())) {
            manage.updateLogoPath(null);
        } else if (dto.getLogoFile() != null && !dto.getLogoFile().isEmpty()) {
            String originalFileName = dto.getLogoFile().getOriginalFilename();
            manage.updateLogoPath(originalFileName);
        }
    }
    /**
     * 4-1 워크스페이스 전체 데이터 영구 소멸 및 삭제
     */
    @Transactional
    public void deleteWorkspace(Long workspaceId, Long currentUserId) {
        Long mockLeaderId = 1L;
        if (!mockLeaderId.equals(currentUserId)) {
            throw new SecurityException("워크스페이스 완전 삭제 권한은 팀장에게만 있습니다.");
        }

        Manage manage = manageRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("해당 워크스페이스 관리 데이터가 존재하지 않습니다. ID: " + workspaceId));

        manageRepository.delete(manage);
    }

    /**
     * 4-2-1 워크스페이스 팀원 초대 프로세스 (동적 문구 및 초대 코드 반영 완료)
     */
    @Transactional
    public void inviteTeamMembers(WorkspaceInviteRequestDto dto, Long currentUserId) {
        Long mockLeaderId = 1L;
        if (!mockLeaderId.equals(currentUserId)) {
            throw new SecurityException("팀장만 팀원을 초대할 수 있는 권한이 있습니다.");
        }

        if ((dto.getEmails() == null || dto.getEmails().isEmpty()) && (dto.getEmail() == null || dto.getEmail().trim().isEmpty())) {
            throw new IllegalArgumentException("초대할 이메일 주소가 존재하지 않습니다.");
        }

        List<String> targetEmails = dto.getEmails();
        if (targetEmails == null || targetEmails.isEmpty()) {
            targetEmails = List.of(dto.getEmail().trim());
        }

        for (String email : targetEmails) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("worktopus7@gmail.com");
                message.setTo(email);
                message.setSubject("[WorkTopus] 프로젝트 워크스페이스 팀원 초대장입니다.");

                String customMessage = (dto.getMessage() != null && !dto.getMessage().trim().isEmpty())
                        ? dto.getMessage()
                        : "WorkTopus 프로젝트 워크스페이스에 초대합니다.";
                String realInviteCode = (dto.getCode() != null && !dto.getCode().trim().isEmpty())
                        ? dto.getCode()
                        : "CWEXN8";

                String mailContent = new StringBuilder()
                        .append(customMessage).append("\n\n")
                        .append("🔗 프로젝트 참여 인증 초대 코드 : ").append(realInviteCode).append("\n")
                        .append("시스템 가입 및 로그인 후 위 코드를 입력하여 팀에 합류해 주세요.\n\n")
                        .append("감사합니다.\n")
                        .append("- WorkTopus 시스템 관리자 배상 -")
                        .toString();

                message.setText(mailContent);
                mailSender.send(message);

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("이메일 발송 실패: " + e.getMessage());
            }
        }
    }

    /** [게시판관리 - 이름 수정 비즈니스 로직 확장부] */
    @Transactional
    public void updateBoardName(Long boardId, String boardName) {
        if (boardName == null || boardName.trim().isEmpty()) {
            throw new IllegalArgumentException("변경할 게시판 명칭이 유효하지 않습니다.");
        }
    }

    /** [게시판관리 - 안전 숨김 및 후속 알림 정책 비즈니스 로직 확장부] */
    @Transactional
    public void hideBoardWithPolicy(Long boardId, String actionPolicy) {
        if (!"CHAT".equals(actionPolicy) && !"POPUP".equals(actionPolicy)) {
            throw new IllegalArgumentException("정의되지 않은 후속 알림 정책 요구사항입니다.");
        }
    }

    /** [추가 요구사항 - 담당 역할 Dirty Checking 자동 저장 서비스 로직] */
    @Transactional
    public void updateMemberTask(Long memberId, String assignedRole) {
        ManageMember member = manageMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 팀원 정보가 존재하지 않습니다. ID: " + memberId));

        // 엔티티 필드에 세팅하여 JPA 변경 감지(Dirty Checking)로 오라클 DB 실시간 업데이트 수행
        member.setAssignedRole(assignedRole);
    }

}

