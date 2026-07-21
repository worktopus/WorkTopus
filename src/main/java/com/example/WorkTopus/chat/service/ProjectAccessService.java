package com.example.WorkTopus.chat.service;

import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class ProjectAccessService {

    /*
     * 실제 PROJECT_MEMBER 조회 서비스
     */
    private final ProjectMemberService
            projectMemberService;

    /*
     * 실제 로그인 사용자 조회 서비스
     */
    private final UserService
            userService;


    /*
     * 현재 사용자가 해당 프로젝트 참여자인지 검사
     *
     * 참여자이면 로그인 Users를 반환하고,
     * 참여자가 아니면 접근을 거부합니다.
     */
    public Users requireProjectMember(
            Long projectId,
            Principal principal
    ) {
        validateProjectId(projectId);

        Users loginUser =
                getLoginUser(principal);

        boolean member =
                projectMemberService
                        .isProjectMember(
                                projectId,
                                loginUser.getUserNum()
                        );

        if (!member) {
            throw new AccessDeniedException(
                    "해당 프로젝트의 참여자가 아닙니다."
            );
        }

        return loginUser;
    }


    /*
     * 현재 로그인 사용자 조회
     */
    public Users getLoginUser(
            Principal principal
    ) {
        if (
                principal == null ||
                        principal.getName() == null ||
                        principal.getName().isBlank()
        ) {
            throw new AccessDeniedException(
                    "로그인이 필요합니다."
            );
        }

        try {
            return userService.findByUserId(
                    principal.getName()
            );

        } catch (IllegalArgumentException exception) {
            throw new AccessDeniedException(
                    "로그인 사용자 정보를 찾을 수 없습니다."
            );
        }
    }


    /*
     * 프로젝트 번호 검증
     */
    private void validateProjectId(
            Long projectId
    ) {
        if (
                projectId == null ||
                        projectId <= 0
        ) {
            throw new IllegalArgumentException(
                    "올바른 프로젝트 번호가 필요합니다."
            );
        }
    }
}