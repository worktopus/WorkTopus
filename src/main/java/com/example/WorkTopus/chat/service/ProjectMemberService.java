package com.example.WorkTopus.chat.service;

import com.example.WorkTopus.chat.dto.ProjectMember;
import com.example.WorkTopus.entity.ProjectRole;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectMemberService {

    /*
     * 실제 PROJECT_MEMBER 테이블 조회
     */
    private final ProjectMemberRepository
            projectMemberRepository;

    private final PresenceService
            presenceService;


    /*
     * 특정 프로젝트의 실제 참여자 전체 조회
     */
    public List<ProjectMember> getMembers(
            Long projectId
    ) {
        if (projectId == null) {
            return List.of();
        }

        return projectMemberRepository
                .findByProject_IdOrderByJoinedAtAsc(
                        projectId
                )
                .stream()
                .map(this::convertToDto)
                .toList();
    }


    /*
     * 프로젝트 번호와 사용자 번호로
     * 참여자 한 명 조회
     */
    public ProjectMember getMember(
            Long projectId,
            Long userNum
    ) {
        if (
                projectId == null ||
                        userNum == null
        ) {
            return null;
        }

        return projectMemberRepository
                .findByProject_IdAndUser_UserNum(
                        projectId,
                        userNum
                )
                .map(this::convertToDto)
                .orElse(null);
    }


    /*
     * 프로젝트 참여 여부 확인
     */
    public boolean isProjectMember(
            Long projectId,
            Long userNum
    ) {
        if (
                projectId == null ||
                        userNum == null
        ) {
            return false;
        }

        return projectMemberRepository
                .existsByProject_IdAndUser_UserNum(
                        projectId,
                        userNum
                );
    }


    /*
     * 프로젝트 OWNER 여부 확인
     */
    public boolean isProjectOwner(
            Long projectId,
            Long userNum
    ) {
        if (
                projectId == null ||
                        userNum == null
        ) {
            return false;
        }

        return projectMemberRepository
                .existsByProject_IdAndUser_UserNumAndRole(
                        projectId,
                        userNum,
                        ProjectRole.OWNER
                );
    }


    /*
     * 프로젝트 전체 참여자 수
     */
    public int getMemberCount(
            Long projectId
    ) {
        if (projectId == null) {
            return 0;
        }

        return Math.toIntExact(
                projectMemberRepository
                        .countByProject_Id(
                                projectId
                        )
        );
    }


    /*
     * 접속자 수
     *
     * 현재는 실제 WebSocket 접속 상태 저장 기능이
     * 없으므로 0을 반환합니다.
     */
    public int getOnlineMemberCount(
            Long projectId
    ) {
        if (projectId == null) {
            return 0;
        }

        return (int) getMembers(projectId)
                .stream()
                .filter(
                        ProjectMember::isOnline
                )
                .count();
    }

    /*
     * PROJECT_MEMBER Entity를
     * 채팅 화면용 DTO로 변환
     */
    private ProjectMember convertToDto(
            com.example.WorkTopus.entity.ProjectMember
                    projectMember
    ) {
        if (
                projectMember == null ||
                        projectMember.getUser() == null
        ) {
            throw new IllegalArgumentException(
                    "프로젝트 참여자 정보가 올바르지 않습니다."
            );
        }

        Users user =
                projectMember.getUser();

        boolean owner =
                ProjectRole.OWNER.equals(
                        projectMember.getRole()
                );

        return ProjectMember.builder()
                .userNum(
                        user.getUserNum()
                )
                .userId(
                        user.getUserId()
                )
                .name(
                        user.getName()
                )

                /*
                 * 접속 상태는 이후 WebSocket 접속자
                 * 관리 단계에서 연결합니다.
                 */
                .online(
                        presenceService.isOnline(
                                user.getUserNum()
                        )
                )

                /*
                 * PROJECT_MEMBER.ROLE이 OWNER이면 true
                 */
                .owner(owner)
                .build();
    }


}