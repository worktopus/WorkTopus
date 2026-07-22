package com.example.WorkTopus.chat.service;

import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.security.Principal;


/*
 * 프로젝트 관련 API에서 공통으로 사용하는
 * 로그인 사용자 및 프로젝트 참여 권한 검사 서비스입니다.
 *
 * 채팅 API에서 공통으로 사용하는
 * 로그인 사용자 및 프로젝트 참여 권한 검사, 읽음 처리, AI 회의요약 등의 컨트롤러에서
 * 동일한 권한 검사 코드를 반복하지 않기 위해 사용합니다.
 */
@Service
@RequiredArgsConstructor
public class ProjectAccessService {

    /*
     * PROJECT_MEMBER 테이블을 조회하여
     * 사용자의 프로젝트 참여 여부를 확인합니다.
     */
    private final ProjectMemberService projectMemberService;


    /*
     * 로그인한 사용자의 USER_ID를 이용해
     * 실제 USERS 엔티티를 조회합니다.
     */
    private final UserService userService;


    /*
     * 현재 로그인한 사용자가 해당 프로젝트 참여자인지 검사합니다.
     *
     * 참여자이면 로그인 Users 객체를 반환합니다.
     * 참여자가 아니면 AccessDeniedException을 발생시켜
     * 요청을 차단합니다.
     */
    public Users requireProjectMember(
            Long projectId,
            Principal principal
    ) {

        /*
         * 프로젝트 번호가 null이거나
         * 0 이하인 잘못된 값인지 확인합니다.
         */
        validateProjectId(projectId);


        /*
         * Spring Security의 Principal을 이용하여
         * 현재 로그인한 실제 사용자 정보를 조회합니다.
         */
        Users loginUser =
                getLoginUser(principal);


        /*
         * PROJECT_MEMBER 테이블에서
         *
         * PROJECT_ID = projectId
         * USER_NUM = loginUser.userNum
         *
         * 조합이 존재하는지 확인합니다.
         */
        boolean isProjectMember =
                projectMemberService
                        .isProjectMember(
                                projectId,
                                loginUser.getUserNum()
                        );


        /*
         * 참여자 정보가 존재하지 않으면
         * 해당 프로젝트 API 사용을 차단합니다.
         */
        if (!isProjectMember) {
            throw new AccessDeniedException(
                    "해당 프로젝트의 참여자가 아닙니다."
            );
        }


        /*
         * 이후 컨트롤러에서 로그인 사용자의
         * userNum, name 등을 사용할 수 있도록 반환합니다.
         */
        return loginUser;
    }


    /*
     * 현재 로그인한 사용자의 실제 USERS 정보를 조회합니다.
     *
     * Principal.getName()에는 Spring Security 로그인에 사용된
     * 문자열 USER_ID가 들어 있습니다.
     */
    public Users getLoginUser(
            Principal principal
    ) {

        /*
         * Principal이 없거나 이름이 비어 있으면
         * 로그인하지 않은 요청으로 처리합니다.
         */
        if (
                principal == null
                        || principal.getName() == null
                        || principal.getName().isBlank()
        ) {
            throw new AccessDeniedException(
                    "로그인이 필요합니다."
            );
        }


        try {

            /*
             * 로그인 USER_ID로 실제 USERS 엔티티를 조회합니다.
             */
            return userService.findByUserId(
                    principal.getName()
            );

        } catch (IllegalArgumentException exception) {

            /*
             * Spring Security에는 인증 정보가 있지만
             * USERS 테이블에서 사용자를 찾지 못한 경우입니다.
             */
            throw new AccessDeniedException(
                    "로그인 사용자 정보를 찾을 수 없습니다."
            );
        }
    }


    /*
     * 프로젝트 번호 유효성 검사
     */
    private void validateProjectId(
            Long projectId
    ) {

        if (
                projectId == null
                        || projectId <= 0
        ) {
            throw new IllegalArgumentException(
                    "올바른 프로젝트 번호가 필요합니다."
            );
        }
    }
}