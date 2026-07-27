package com.example.WorkTopus.manage.service;

import com.example.WorkTopus.manage.entity.Manage;
import com.example.WorkTopus.manage.entity.ManageMember;
import com.example.WorkTopus.manage.dto.WorkspaceGeneralUpdateDto;
import com.example.WorkTopus.manage.dto.WorkspaceInviteRequestDto;
import com.example.WorkTopus.manage.dto.ManageMemberRoleUpdateDto;
import com.example.WorkTopus.manage.repository.ManageRepository;
import com.example.WorkTopus.manage.repository.ManageMemberRepository;
import com.example.WorkTopus.projects.repository.BoardRepository; // 📌 정식 게시판 레포지토리 임포트
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceManageService {

    private final JavaMailSender mailSender;
    private final ManageRepository manageRepository;
    private final ManageMemberRepository manageMemberRepository;
    private final BoardRepository boardRepository; // 📌 정식 오라클 게시판 레포지토리 자동 의존성 주입 완료

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
     * 워크스페이스 일반 관리 설정 업데이트
     */
    @Transactional
    public void updateGeneralSettings(Long workspaceId, WorkspaceGeneralUpdateDto dto, Long currentUserId) {
        Long mockLeaderId = 1L;
        if (!mockLeaderId.equals(currentUserId)) {
            throw new SecurityException("팀장 권한이 없습니다.");
        }

        Manage manage = manageRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트 관리 데이터가 존재하지 않습니다. ID: " + workspaceId));

        if (dto.getWorkspaceName() != null && !dto.getWorkspaceName().trim().isEmpty()) {
            manage.setName(dto.getWorkspaceName());
        }

        if (dto.getProjectDescription() != null) {
            manage.setDescription(dto.getProjectDescription());
        }

        if (dto.getVisibility() != null) {
            manage.updateGeneralSettings(manage.getName(), dto.getVisibility(), dto.getArchiveStatus());
        }

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
     * 워크스페이스 전체 데이터 영구 소멸 및 삭제
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
     * 워크스페이스 팀원 초대 프로세스
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

    @Transactional
    public void updateBoardName(Long boardId, String boardName) {
        if (boardName == null || boardName.trim().isEmpty()) {
            throw new IllegalArgumentException("변경할 게시판 명칭이 유효하지 않습니다.");
        }
    }

    @Transactional
    public void hideBoardWithPolicy(Long boardId, String actionPolicy) {
        if (!"CHAT".equals(actionPolicy) && !"POPUP".equals(actionPolicy)) {
            throw new IllegalArgumentException("정의되지 않은 후속 알림 정책 요구사항입니다.");
        }
    }

    @Transactional
    public void updateMemberTask(Long memberId, String assignedRole) {
        ManageMember member = manageMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 팀원 정보가 존재하지 않습니다. ID: " + memberId));

        member.setAssignedRole(assignedRole);
    }

    // ===============================================================================
    // 📌 [수정 완결] 정식 오라클 BoardRepository 테이블 통계 및 콘텐츠 추출 서비스 로직
    // ===============================================================================

    /**
     * 📊 특정 프로젝트의 활성화된(N) 전체 누적 게시글 수 사출
     */
    public int getTotalPostsCount(Long workspaceId) {
        return (int) boardRepository.countByProjectIdAndDeletedYn(workspaceId, "N");
    }

    /**
     * 📝 타임리프 boards.html 파싱 전용 정식 Page 객체 공급 파이프라인
     */
    public org.springframework.data.domain.Page<?> getIntegratedBoardPage(Long workspaceId, org.springframework.data.domain.Pageable pageable) {
        return boardRepository.findByProjectIdAndDeletedYnOrderByNoticeYnDescCreatedAtDesc(workspaceId, "N", pageable);
    }

    /**
     * 📂 자바스크립트 fetch 호출 시 오라클 실시간 리스트 명단을 List<Board> 형태로 사출
     */
    public List getRealBoardContents(Long workspaceId) {
        org.springframework.data.domain.Page boardPage =
                boardRepository.findByProjectIdAndDeletedYnOrderByCreatedAtDesc(workspaceId, "N", org.springframework.data.domain.PageRequest.of(0, 9999));
        return boardPage.getContent();
    }

    /**
     * 📌 중요 게시글 필독 상단 고정 제어 플래그 변환 (네이티브 쿼리 하이브리드 고정)
     */
    @Transactional
    public void togglePostPin(Long postId, boolean isPinned) {
        System.out.println("====== [오라클 DB 연동] BOARD 테이블 데이터 공지 플래그 갱신 가동 ======");

        String noticeValue = isPinned ? "Y" : "N";
        String sql = "UPDATE BOARD SET NOTICE_YN = :noticeValue WHERE ID = :postId";

        int updatedCount = em.createNativeQuery(sql)
                .setParameter("noticeValue", noticeValue)
                .setParameter("postId", postId)
                .executeUpdate();

        System.out.println("▶ 오라클 업데이트 처리 완료 결과 레코드 수: " + updatedCount + "건");

        if (updatedCount == 0) {
            throw new IllegalArgumentException("오라클 DB 내에 해당 게시글 데이터가 존재하지 않습니다. ID: " + postId);
        }
    }

    /**
     * 👥 [오라클 DB 권한 조인 엔진]
     * 로그인한 사용자의 ID(이메일)를 통해 USERS 테이블과 PROJECT_MEMBER 테이블을 조인하여
     * 현재 프로젝트의 진짜 OWNER(팀장) 권한을 가진 유저인지 실시간으로 검증합니다.
     */
    public boolean checkIfProjectOwner(Long projectId, String loginUserId) {
        System.out.println("====== [오라클 권한 체크] OWNER 여부 정밀 검증 가동 ======");
        System.out.println("▶ 대상 프로젝트: " + projectId + " | 로그인 유저: " + loginUserId);

        try {
            // 💡 [정밀 SQL 조인 매커니즘 구현]
            // 유저 테이블(USERS)의 USER_ID와 프로젝트 멤버 테이블(PROJECT_MEMBER)의 USER_NUM을 매칭하여 ROLE을 조회합니다.
            String sql = "SELECT m.ROLE " +
                    "FROM PROJECT_MEMBER m " +
                    "JOIN USERS u ON m.USER_NUM = u.USER_NUM " +
                    "WHERE m.PROJECT_ID = :projectId AND u.USER_ID = :loginUserId";

            String role = (String) em.createNativeQuery(sql)
                    .setParameter("projectId", projectId)
                    .setParameter("loginUserId", loginUserId)
                    .getSingleResult();

            System.out.println("▶ 오라클 DB 실시간 조회 권한 결과 문자열: " + role);

            // 💡 데이터베이스에 적재된 ROLE 값이 대문자 'OWNER'가 맞다면 true를 반환하여 권한을 강제 개방합니다.
            return "OWNER".equalsIgnoreCase(role);

        } catch (jakarta.persistence.NoResultException e) {
            // 해당 프로젝트에 참여 멤버 데이터 자체가 없거나 매칭 실패 시 안전하게 false 사출
            System.out.println("⚠ [권한 검증 결과] 해당 유저의 프로젝트 멤버 레코드가 존재하지 않습니다.");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
