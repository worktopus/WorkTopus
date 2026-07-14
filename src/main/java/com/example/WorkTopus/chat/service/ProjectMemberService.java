package com.example.WorkTopus.chat.service;

import com.example.WorkTopus.chat.dto.ProjectMember;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ProjectMemberService {

    /*
     * 임시 프로젝트 정보
     *
     * 실제 프로젝트 DB가 연결되면 삭제합니다.
     */
    private static final Long TEMP_PROJECT_ID = 2L;

    /*
     * 현재 임시 owner 사용자 번호
     *
     * owner=true인 팀원에게 화면에서 ⭐가 표시됩니다.
     */
    private static final Long TEMP_OWNER_USER_NUM = 2L;


    /*
     * 프로젝트별 팀원 목록
     *
     * Key   : projectId
     * Value : 프로젝트 참여자 목록
     */
    private final Map<Long, CopyOnWriteArrayList<ProjectMember>>
            membersByProject =
            new ConcurrentHashMap<>();


    /*
     * 임시 팀원 데이터 생성
     */
    public ProjectMemberService() {

        CopyOnWriteArrayList<ProjectMember> members =
                new CopyOnWriteArrayList<>();

        members.add(
                createTemporaryMember(
                        1L,
                        "신승민"
                )
        );

        members.add(
                createTemporaryMember(
                        2L,
                        "김철수"
                )
        );

        members.add(
                createTemporaryMember(
                        3L,
                        "홍길동"
                )
        );

        members.add(
                createTemporaryMember(
                        4L,
                        "이영희"
                )
        );

        membersByProject.put(
                TEMP_PROJECT_ID,
                members
        );
    }


    /*
     * 임시 팀원 한 명 생성
     */
    private ProjectMember createTemporaryMember(
            Long userNum,
            String name
    ) {
        return ProjectMember.builder()
                .userNum(userNum)
                .userId("")
                .name(name)
                .online(false)
                .owner(
                        TEMP_OWNER_USER_NUM.equals(
                                userNum
                        )
                )
                .build();
    }


    /*
     * 프로젝트 팀원 전체 조회
     */
    public List<ProjectMember> getMembers(
            Long projectId
    ) {
        if (projectId == null) {
            return List.of();
        }

        List<ProjectMember> members =
                membersByProject.get(
                        projectId
                );

        if (members == null) {
            return List.of();
        }

        return List.copyOf(
                members
        );
    }


    /*
     * userNum으로 팀원 한 명 조회
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

        return getMembers(projectId)
                .stream()
                .filter(member ->
                        userNum.equals(
                                member.getUserNum()
                        )
                )
                .findFirst()
                .orElse(null);
    }


    /*
     * 프로젝트 참여 여부 확인
     */
    public boolean isProjectMember(
            Long projectId,
            Long userNum
    ) {
        return getMember(
                projectId,
                userNum
        ) != null;
    }


    /*
     * 프로젝트 owner 여부 확인
     */
    public boolean isProjectOwner(
            Long projectId,
            Long userNum
    ) {
        ProjectMember member =
                getMember(
                        projectId,
                        userNum
                );

        return member != null &&
                member.isOwner();
    }


    /*
     * 팀원 추가 또는 정보 갱신
     *
     * 같은 userNum이 있으면 기존 정보를 교체합니다.
     */
    public ProjectMember addOrUpdateMember(
            Long projectId,
            ProjectMember member
    ) {
        validateProjectId(projectId);
        validateMember(member);

        CopyOnWriteArrayList<ProjectMember> members =
                membersByProject.computeIfAbsent(
                        projectId,
                        key ->
                                new CopyOnWriteArrayList<>()
                );

        members.removeIf(existingMember ->
                member.getUserNum().equals(
                        existingMember.getUserNum()
                )
        );

        members.add(member);

        return member;
    }


    /*
     * 로그인 사용자를 프로젝트 팀원 목록에 등록
     *
     * owner 값은 ProjectService에서 전달합니다.
     */
    public ProjectMember registerLoginMember(
            Long projectId,
            Long userNum,
            String userId,
            String name,
            boolean owner
    ) {
        if (userNum == null) {
            throw new IllegalArgumentException(
                    "로그인 사용자 번호가 없습니다."
            );
        }

        ProjectMember loginMember =
                ProjectMember.builder()
                        .userNum(userNum)
                        .userId(
                                userId == null
                                        ? ""
                                        : userId
                        )
                        .name(
                                name == null ||
                                        name.isBlank()
                                        ? "이름 없음"
                                        : name
                        )
                        .online(true)
                        .owner(owner)
                        .build();

        return addOrUpdateMember(
                projectId,
                loginMember
        );
    }


    /*
     * 프로젝트 팀원 삭제
     */
    public boolean removeMember(
            Long projectId,
            Long userNum
    ) {
        if (
                projectId == null ||
                        userNum == null
        ) {
            return false;
        }

        CopyOnWriteArrayList<ProjectMember> members =
                membersByProject.get(
                        projectId
                );

        if (members == null) {
            return false;
        }

        return members.removeIf(member ->
                userNum.equals(
                        member.getUserNum()
                )
        );
    }


    /*
     * 팀원 접속 상태 변경
     *
     * owner 상태는 그대로 유지합니다.
     */
    public boolean updateOnlineStatus(
            Long projectId,
            Long userNum,
            boolean online
    ) {
        ProjectMember member =
                getMember(
                        projectId,
                        userNum
                );

        if (member == null) {
            return false;
        }

        ProjectMember updatedMember =
                ProjectMember.builder()
                        .userNum(
                                member.getUserNum()
                        )
                        .userId(
                                member.getUserId()
                        )
                        .name(
                                member.getName()
                        )
                        .online(online)
                        .owner(
                                member.isOwner()
                        )
                        .build();

        addOrUpdateMember(
                projectId,
                updatedMember
        );

        return true;
    }


    /*
     * owner 지정 변경
     *
     * 한 프로젝트에는 owner가 한 명이라고 가정합니다.
     * 새 owner를 설정하면 기존 owner는 해제합니다.
     */
    public boolean changeProjectOwner(
            Long projectId,
            Long newOwnerUserNum
    ) {
        if (
                projectId == null ||
                        newOwnerUserNum == null
        ) {
            return false;
        }

        CopyOnWriteArrayList<ProjectMember> members =
                membersByProject.get(
                        projectId
                );

        if (members == null) {
            return false;
        }

        boolean ownerExists =
                members.stream()
                        .anyMatch(member ->
                                newOwnerUserNum.equals(
                                        member.getUserNum()
                                )
                        );

        if (!ownerExists) {
            return false;
        }

        List<ProjectMember> updatedMembers =
                members.stream()
                        .map(member ->
                                ProjectMember.builder()
                                        .userNum(
                                                member.getUserNum()
                                        )
                                        .userId(
                                                member.getUserId()
                                        )
                                        .name(
                                                member.getName()
                                        )
                                        .online(
                                                member.isOnline()
                                        )
                                        .owner(
                                                newOwnerUserNum.equals(
                                                        member.getUserNum()
                                                )
                                        )
                                        .build()
                        )
                        .toList();

        members.clear();
        members.addAll(
                updatedMembers
        );

        return true;
    }


    /*
     * 프로젝트 팀원 수
     */
    public int getMemberCount(
            Long projectId
    ) {
        return getMembers(
                projectId
        ).size();
    }


    /*
     * 프로젝트 접속자 수
     */
    public int getOnlineMemberCount(
            Long projectId
    ) {
        return (int) getMembers(projectId)
                .stream()
                .filter(
                        ProjectMember::isOnline
                )
                .count();
    }


    /*
     * 프로젝트 번호 검증
     */
    private void validateProjectId(
            Long projectId
    ) {
        if (projectId == null) {
            throw new IllegalArgumentException(
                    "프로젝트 번호가 없습니다."
            );
        }
    }


    /*
     * 팀원 정보 검증
     */
    private void validateMember(
            ProjectMember member
    ) {
        if (member == null) {
            throw new IllegalArgumentException(
                    "팀원 정보가 없습니다."
            );
        }

        if (member.getUserNum() == null) {
            throw new IllegalArgumentException(
                    "팀원의 사용자 번호가 없습니다."
            );
        }

        if (
                member.getName() == null ||
                        member.getName().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "팀원 이름이 없습니다."
            );
        }
    }
}