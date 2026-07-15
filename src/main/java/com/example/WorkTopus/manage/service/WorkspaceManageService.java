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
    private final ManageMemberRepository manageMemberRepository; // 팀원 리포지토리 의존성 추가

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
     * 4-1 워크스페이스 일반 관리 설정 업데이트
     */
    @Transactional
    public void updateGeneralSettings(Long workspaceId, WorkspaceGeneralUpdateDto dto, Long currentUserId) {
        Long mockLeaderId = 1L;
        if (!mockLeaderId.equals(currentUserId)) {
            throw new SecurityException("팀장 권한이 없습니다.");
        }

        boolean isExist = manageRepository.existsById(workspaceId);

        if (!isExist) {
            em.createNativeQuery(
                            "INSERT INTO MANAGES (ID, CREATED_AT, DESCRIPTION, INVITE_CODE, NAME, OWNER_ID, VISIBILITY, ARCHIVE_STATUS) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")
                    .setParameter(1, workspaceId)
                    .setParameter(2, LocalDateTime.now())
                    .setParameter(3, "새로 생성된 협업 워크스페이스 룸")
                    .setParameter(4, "INV-" + workspaceId)
                    .setParameter(5, "Worktopus")
                    .setParameter(6, mockLeaderId)
                    .setParameter(7, "PUBLIC")
                    .setParameter(8, "ACTIVE")
                    .executeUpdate();

            em.flush();
            em.clear();
        }

        Manage manage = manageRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("데이터 정합성 오류 발생. ID: " + workspaceId));

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
     * 4-2-1 워크스페이스 팀원 초대 프로세스
     */
    @Transactional
    public void inviteTeamMembers(WorkspaceInviteRequestDto dto, Long currentUserId) {
        Long mockLeaderId = 1L;
        if (!mockLeaderId.equals(currentUserId)) {
            throw new SecurityException("팀장만 팀원을 초대할 수 있는 권한이 있습니다.");
        }

        if (dto.getEmails() == null || dto.getEmails().isEmpty()) {
            throw new IllegalArgumentException("초대할 이메일 주소가 존재하지 않습니다.");
        }

        for (String email : dto.getEmails()) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("worktopus7@gmail.com");
                message.setTo(email);
                message.setSubject("[WorkTopus] 프로젝트 워크스페이스 팀원 초대장입니다.");

                String mailContent = new StringBuilder()
                        .append("안녕하세요, WorkTopus 팀 협업 플랫폼입니다.\n\n")
                        .append("팀장님으로부터 프로젝트 워크스페이스 초대장이 도착했습니다.\n")
                        .append("아래 가상 링크를 클릭하거나 복사하여 브라우저에 붙여넣으면 가입 절차가 진행됩니다.\n\n")
                        .append("🔗 프로젝트 참여하기 가상 링크: ")
                        .append("http://localhost:8080/manage/").append(dto.getWorkspaceId()).append("/invite/accept\n\n")
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
}
