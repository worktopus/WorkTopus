package com.example.WorkTopus.chat.controller;

import com.example.WorkTopus.chat.dto.ChatRoomSummary;
import com.example.WorkTopus.chat.service.ChatReadService;
import com.example.WorkTopus.chat.service.ChatRoomSummaryService;
import com.example.WorkTopus.chat.service.ProjectService;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRoomSummaryController {

    /*
     * 프로젝트 단체 채팅방 형식
     *
     * project_2_group
     */
    private static final Pattern GROUP_ROOM_PATTERN =
            Pattern.compile(
                    "^project_(\\d+)_group$"
            );

    /*
     * 개인 채팅방 형식
     *
     * project_2_private_1_4
     */
    private static final Pattern PRIVATE_ROOM_PATTERN =
            Pattern.compile(
                    "^project_(\\d+)_private_(\\d+)_(\\d+)$"
            );


    private final ChatRoomSummaryService
            chatRoomSummaryService;

    private final ChatReadService
            chatReadService;

    private final ProjectService
            projectService;

    private final UserService
            userService;


    /*
     * 로그인 사용자의 전체 채팅방 목록 조회
     *
     * GET /api/chat/rooms
     */
    @GetMapping("/rooms")
    public List<ChatRoomSummary> getRooms(
            Principal principal
    ) {
        Users loginUser =
                getLoginUser(
                        principal
                );

        return chatRoomSummaryService
                .getRoomSummaries(
                        loginUser
                );
    }


    /*
     * 프로젝트방 또는 개인방 읽음 처리
     *
     * POST /api/chat/rooms/{roomId}/read
     *
     * 예:
     *
     * POST
     * /api/chat/rooms/project_2_group/read
     *
     * POST
     * /api/chat/rooms/project_2_private_1_4/read
     */
    @PostMapping(
            "/rooms/{roomId}/read"
    )
    public ResponseEntity<Void> markRoomAsRead(
            @PathVariable String roomId,
            Principal principal
    ) {
        Users loginUser =
                getLoginUser(
                        principal
                );

        Long userNum =
                loginUser.getUserNum();

        /*
         * 채팅방 ID에서 프로젝트 번호를 추출합니다.
         */
        Long projectId =
                extractProjectId(
                        roomId
                );

        /*
         * 로그인 사용자가 해당 프로젝트에
         * 참여하고 있는지 확인합니다.
         */
        validateProjectMember(
                projectId,
                userNum
        );

        /*
         * 개인방이라면 로그인 사용자가
         * 해당 개인방의 참여자인지 추가 확인합니다.
         */
        validateRoomAccess(
                roomId,
                projectId,
                userNum
        );

        /*
         * 현재 방의 마지막 메시지까지 읽음 처리합니다.
         */
        chatReadService.markRoomAsRead(
                projectId,
                roomId,
                userNum
        );

        return ResponseEntity
                .noContent()
                .build();
    }


    /*
     * 채팅방 ID에서 프로젝트 번호 추출
     */
    private Long extractProjectId(
            String roomId
    ) {
        if (
                roomId == null ||
                        roomId.isBlank()
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
            return Long.parseLong(
                    groupMatcher.group(1)
            );
        }

        Matcher privateMatcher =
                PRIVATE_ROOM_PATTERN.matcher(
                        normalizedRoomId
                );

        if (privateMatcher.matches()) {
            return Long.parseLong(
                    privateMatcher.group(1)
            );
        }

        throw new IllegalArgumentException(
                "올바르지 않은 채팅방 번호입니다."
        );
    }


    /*
     * 프로젝트 참여 여부 확인
     */
    private void validateProjectMember(
            Long projectId,
            Long userNum
    ) {
        if (
                projectId == null ||
                        userNum == null
        ) {
            throw new AccessDeniedException(
                    "프로젝트 접근 권한이 없습니다."
            );
        }

        boolean projectMember =
                projectService.isProjectMember(
                        projectId,
                        userNum
                );

        if (!projectMember) {
            throw new AccessDeniedException(
                    "해당 프로젝트의 참여자가 아닙니다."
            );
        }
    }


    /*
     * 채팅방 접근 권한 확인
     */
    private void validateRoomAccess(
            String roomId,
            Long projectId,
            Long loginUserNum
    ) {
        String normalizedRoomId =
                roomId.trim();

        /*
         * 프로젝트 단체방은 프로젝트 참여자라면
         * 접근할 수 있습니다.
         */
        Matcher groupMatcher =
                GROUP_ROOM_PATTERN.matcher(
                        normalizedRoomId
                );

        if (groupMatcher.matches()) {
            return;
        }

        Matcher privateMatcher =
                PRIVATE_ROOM_PATTERN.matcher(
                        normalizedRoomId
                );

        if (!privateMatcher.matches()) {
            throw new AccessDeniedException(
                    "접근할 수 없는 채팅방입니다."
            );
        }

        Long roomProjectId =
                Long.parseLong(
                        privateMatcher.group(1)
                );

        Long firstUserNum =
                Long.parseLong(
                        privateMatcher.group(2)
                );

        Long secondUserNum =
                Long.parseLong(
                        privateMatcher.group(3)
                );

        /*
         * 주소의 프로젝트 번호가
         * 추출한 프로젝트 번호와 같은지 확인합니다.
         */
        if (!projectId.equals(roomProjectId)) {
            throw new AccessDeniedException(
                    "프로젝트 정보가 일치하지 않습니다."
            );
        }

        /*
         * 로그인 사용자가 개인방 참여자 중
         * 한 명이어야 합니다.
         */
        boolean roomParticipant =
                loginUserNum.equals(
                        firstUserNum
                ) ||
                        loginUserNum.equals(
                                secondUserNum
                        );

        if (!roomParticipant) {
            throw new AccessDeniedException(
                    "해당 개인 채팅방의 참여자가 아닙니다."
            );
        }

        /*
         * 개인방의 두 사용자 모두
         * 해당 프로젝트 참여자인지 확인합니다.
         */
        boolean firstProjectMember =
                projectService.isProjectMember(
                        projectId,
                        firstUserNum
                );

        boolean secondProjectMember =
                projectService.isProjectMember(
                        projectId,
                        secondUserNum
                );

        if (
                !firstProjectMember ||
                        !secondProjectMember
        ) {
            throw new AccessDeniedException(
                    "프로젝트 참여자 간의 채팅방이 아닙니다."
            );
        }
    }


    /*
     * 현재 로그인 사용자 조회
     */
    private Users getLoginUser(
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

        } catch (
                IllegalArgumentException exception
        ) {
            throw new AccessDeniedException(
                    "로그인 사용자 정보를 찾을 수 없습니다."
            );
        }
    }
}