package com.example.WorkTopus.chat.service;

import com.example.WorkTopus.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@RequiredArgsConstructor
public class ChatRoomAccessService {

    /*
     * 프로젝트 단체 채팅방 형식
     *
     * 예:
     * project_22_group
     */
    private static final Pattern GROUP_ROOM_PATTERN =
            Pattern.compile(
                    "^project_(\\d+)_group$"
            );


    /*
     * 프로젝트 개인 채팅방 형식
     *
     * 예:
     * project_22_private_3_8
     */
    private static final Pattern PRIVATE_ROOM_PATTERN =
            Pattern.compile(
                    "^project_(\\d+)_private_(\\d+)_(\\d+)$"
            );


    /*
     * 로그인 사용자 조회와
     * 프로젝트 참여 여부 검사
     */
    private final ProjectAccessService
            projectAccessService;


    /*
     * 개인 채팅방에 포함된 두 사용자가
     * 모두 해당 프로젝트 참여자인지 검사
     */
    private final ProjectMemberService
            projectMemberService;


    /*
     * 채팅방 접근 권한 검사
     *
     * 단체방:
     * - 로그인 사용자가 프로젝트 참여자여야 합니다.
     *
     * 개인방:
     * - 로그인 사용자가 프로젝트 참여자여야 합니다.
     * - 로그인 사용자가 개인방 당사자여야 합니다.
     * - 개인방의 두 사용자 모두 프로젝트 참여자여야 합니다.
     *
     * 검사를 통과하면 실제 로그인 Users를 반환합니다.
     */
    public Users requireRoomAccess(
            String roomId,
            Principal principal
    ) {

        if (
                roomId == null
                        || roomId.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "채팅방 번호가 없습니다."
            );
        }


        String normalizedRoomId =
                roomId.trim();


        /*
         * =====================================================
         * 단체 채팅방 검사
         * =====================================================
         */
        Matcher groupMatcher =
                GROUP_ROOM_PATTERN.matcher(
                        normalizedRoomId
                );


        if (groupMatcher.matches()) {

            Long projectId =
                    parseLong(
                            groupMatcher.group(1),
                            "올바르지 않은 프로젝트 번호입니다."
                    );


            /*
             * 현재 로그인 사용자가 프로젝트 참여자인지 확인합니다.
             */
            return projectAccessService
                    .requireProjectMember(
                            projectId,
                            principal
                    );
        }


        /*
         * =====================================================
         * 개인 채팅방 검사
         * =====================================================
         */
        Matcher privateMatcher =
                PRIVATE_ROOM_PATTERN.matcher(
                        normalizedRoomId
                );


        if (privateMatcher.matches()) {

            Long projectId =
                    parseLong(
                            privateMatcher.group(1),
                            "올바르지 않은 프로젝트 번호입니다."
                    );

            Long firstUserNum =
                    parseLong(
                            privateMatcher.group(2),
                            "올바르지 않은 사용자 번호입니다."
                    );

            Long secondUserNum =
                    parseLong(
                            privateMatcher.group(3),
                            "올바르지 않은 사용자 번호입니다."
                    );


            /*
             * 개인방은 항상 작은 userNum이 앞에 위치합니다.
             *
             * 정상:
             * project_22_private_3_8
             *
             * 비정상:
             * project_22_private_8_3
             *
             * 비정상:
             * project_22_private_3_3
             */
            if (
                    firstUserNum >= secondUserNum
            ) {
                throw new IllegalArgumentException(
                        "올바르지 않은 개인 채팅방 번호입니다."
                );
            }


            /*
             * 현재 로그인 사용자가 프로젝트 참여자인지 검사하고
             * 실제 로그인 사용자 정보를 반환받습니다.
             */
            Users loginUser =
                    projectAccessService
                            .requireProjectMember(
                                    projectId,
                                    principal
                            );


            Long loginUserNum =
                    loginUser.getUserNum();


            /*
             * 로그인 사용자가 개인방의
             * 두 사용자 중 한 명인지 확인합니다.
             */
            boolean roomParticipant =
                    loginUserNum.equals(
                            firstUserNum
                    )
                            || loginUserNum.equals(
                            secondUserNum
                    );


            /*
             * 같은 프로젝트 참여자여도
             * 다른 두 사람의 개인방은 구독할 수 없습니다.
             */
            if (!roomParticipant) {
                throw new AccessDeniedException(
                        "해당 개인 채팅방의 참여자가 아닙니다."
                );
            }


            /*
             * 개인방 첫 번째 사용자의 프로젝트 참여 여부
             */
            boolean firstUserIsProjectMember =
                    projectMemberService
                            .isProjectMember(
                                    projectId,
                                    firstUserNum
                            );


            /*
             * 개인방 두 번째 사용자의 프로젝트 참여 여부
             */
            boolean secondUserIsProjectMember =
                    projectMemberService
                            .isProjectMember(
                                    projectId,
                                    secondUserNum
                            );


            /*
             * 둘 중 한 명이라도 프로젝트 참여자가 아니면
             * 해당 개인방 접근을 차단합니다.
             */
            if (
                    !firstUserIsProjectMember
                            || !secondUserIsProjectMember
            ) {
                throw new AccessDeniedException(
                        "프로젝트 참여자 간의 개인 채팅방이 아닙니다."
                );
            }


            return loginUser;
        }


        /*
         * 단체방과 개인방 형식에 모두 맞지 않는 경우
         */
        throw new IllegalArgumentException(
                "올바르지 않은 채팅방 번호입니다."
        );
    }


    /*
     * 채팅방 ID에서 프로젝트 번호 추출
     *
     * project_22_group
     * → 22
     *
     * project_22_private_3_8
     * → 22
     */
    public Long extractProjectId(
            String roomId
    ) {

        if (
                roomId == null
                        || roomId.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "채팅방 번호가 없습니다."
            );
        }


        String normalizedRoomId =
                roomId.trim();


        Matcher groupMatcher =
                GROUP_ROOM_PATTERN.matcher(
                        normalizedRoomId
                );


        if (groupMatcher.matches()) {
            return parseLong(
                    groupMatcher.group(1),
                    "올바르지 않은 프로젝트 번호입니다."
            );
        }


        Matcher privateMatcher =
                PRIVATE_ROOM_PATTERN.matcher(
                        normalizedRoomId
                );


        if (privateMatcher.matches()) {
            return parseLong(
                    privateMatcher.group(1),
                    "올바르지 않은 프로젝트 번호입니다."
            );
        }


        throw new IllegalArgumentException(
                "올바르지 않은 채팅방 번호입니다."
        );
    }


    /*
     * 문자열 숫자를 Long으로 변환
     */
    private Long parseLong(
            String value,
            String errorMessage
    ) {

        try {
            Long number =
                    Long.valueOf(
                            value
                    );


            if (number <= 0) {
                throw new IllegalArgumentException(
                        errorMessage
                );
            }


            return number;

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    errorMessage
            );
        }
    }
}