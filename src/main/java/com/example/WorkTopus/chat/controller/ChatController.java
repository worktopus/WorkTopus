package com.example.WorkTopus.chat.controller;

import com.example.WorkTopus.chat.dto.ChatMessage;
import com.example.WorkTopus.chat.service.ChatService;
import com.example.WorkTopus.chat.service.ProjectMemberService;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private static final int MAX_MESSAGE_LENGTH = 2000;

    /*
     * 단체 채팅방:
     * project_2_group
     */
    private static final Pattern GROUP_ROOM_PATTERN =
            Pattern.compile(
                    "^project_(\\d+)_group$"
            );

    /*
     * 개인 채팅방:
     * project_2_private_1_4
     */
    private static final Pattern PRIVATE_ROOM_PATTERN =
            Pattern.compile(
                    "^project_(\\d+)_private_(\\d+)_(\\d+)$"
            );

    private final ChatService chatService;

    private final ProjectMemberService projectMemberService;

    private final UserService userService;

    private final SimpMessagingTemplate messagingTemplate;


    /*
     * 메시지 전송
     *
     * 클라이언트 전송 주소:
     * /app/chat.send
     */
    @MessageMapping("/chat.send")
    public void send(
            ChatMessage message,
            Principal principal
    ) {
        Users loginUser =
                getLoginUser(principal);

        validateMessage(
                message,
                loginUser.getUserNum()
        );

        /*
         * 프런트에서 보낸 senderNum과 senderName은
         * 신뢰하지 않고 로그인 사용자 정보로 덮어씁니다.
         */
        message.setSenderNum(
                loginUser.getUserNum()
        );

        message.setSenderName(
                loginUser.getName()
        );

        message.setMessage(
                message.getMessage().trim()
        );

        message.setType("TALK");

        message.setCreatedAt(
                OffsetDateTime.now()
        );

        /*
         * 저장 후 messageId 등이 포함된 객체를 받습니다.
         */
        ChatMessage savedMessage =
                chatService.save(message);

        /*
         * 해당 채팅방 구독자에게 메시지 전송
         */
        messagingTemplate.convertAndSend(
                "/topic/chat/" +
                        savedMessage.getRoomId(),
                savedMessage
        );
    }


    /*
     * 채팅방 이전 대화 조회
     *
     * GET /chat/history/{roomId}
     */
    @GetMapping("/chat/history/{roomId}")
    @ResponseBody
    public List<ChatMessage> getHistory(
            @PathVariable String roomId,
            Principal principal
    ) {
        Users loginUser =
                getLoginUser(principal);

        validateRoomAccess(
                roomId,
                null,
                loginUser.getUserNum()
        );

        return chatService.getMessages(
                roomId
        );
    }


    /*
     * 메시지 정보 검증
     */
    private void validateMessage(
            ChatMessage message,
            Long loginUserNum
    ) {
        if (message == null) {
            throw new IllegalArgumentException(
                    "메시지 정보가 없습니다."
            );
        }

        if (message.getProjectId() == null) {
            throw new IllegalArgumentException(
                    "프로젝트 번호가 없습니다."
            );
        }

        if (
                message.getRoomId() == null ||
                        message.getRoomId().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "채팅방 번호가 없습니다."
            );
        }

        if (
                message.getMessage() == null ||
                        message.getMessage().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "메시지 내용을 입력하세요."
            );
        }

        if (
                message.getMessage()
                        .trim()
                        .length() >
                        MAX_MESSAGE_LENGTH
        ) {
            throw new IllegalArgumentException(
                    "메시지는 2000자 이하로 입력하세요."
            );
        }

        validateRoomAccess(
                message.getRoomId(),
                message.getProjectId(),
                loginUserNum
        );
    }


    /*
     * 채팅방 접근 권한 검사
     */
    private void validateRoomAccess(
            String roomId,
            Long expectedProjectId,
            Long loginUserNum
    ) {
        if (
                roomId == null ||
                        roomId.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "채팅방 번호가 없습니다."
            );
        }

        if (loginUserNum == null) {
            throw new AccessDeniedException(
                    "로그인 사용자 번호가 없습니다."
            );
        }

        /*
         * 단체 채팅방 검사
         */
        Matcher groupMatcher =
                GROUP_ROOM_PATTERN.matcher(
                        roomId
                );

        if (groupMatcher.matches()) {
            Long roomProjectId =
                    Long.valueOf(
                            groupMatcher.group(1)
                    );

            validateProjectId(
                    roomProjectId,
                    expectedProjectId
            );

            validateProjectMember(
                    roomProjectId,
                    loginUserNum
            );

            return;
        }

        /*
         * 개인 채팅방 검사
         */
        Matcher privateMatcher =
                PRIVATE_ROOM_PATTERN.matcher(
                        roomId
                );

        if (privateMatcher.matches()) {
            Long roomProjectId =
                    Long.valueOf(
                            privateMatcher.group(1)
                    );

            Long firstUserNum =
                    Long.valueOf(
                            privateMatcher.group(2)
                    );

            Long secondUserNum =
                    Long.valueOf(
                            privateMatcher.group(3)
                    );

            validateProjectId(
                    roomProjectId,
                    expectedProjectId
            );

            /*
             * 작은 userNum이 앞에 있어야 합니다.
             */
            if (
                    firstUserNum >=
                            secondUserNum
            ) {
                throw new IllegalArgumentException(
                        "올바르지 않은 개인 채팅방 번호입니다."
                );
            }

            /*
             * 로그인 사용자가 개인 채팅방의
             * 두 참여자 중 한 명인지 확인합니다.
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
                        "해당 개인 채팅방에 접근할 수 없습니다."
                );
            }

            /*
             * 로그인 사용자도 프로젝트 참여자인지 확인
             */
            validateProjectMember(
                    roomProjectId,
                    loginUserNum
            );

            /*
             * 개인 채팅 상대방들도 모두
             * 같은 프로젝트 참여자인지 확인합니다.
             */
            validateProjectMember(
                    roomProjectId,
                    firstUserNum
            );

            validateProjectMember(
                    roomProjectId,
                    secondUserNum
            );

            return;
        }

        throw new IllegalArgumentException(
                "올바르지 않은 채팅방 번호입니다."
        );
    }


    /*
     * 프로젝트 참여자 여부 검사
     */
    private void validateProjectMember(
            Long projectId,
            Long userNum
    ) {
        boolean projectMember =
                projectMemberService
                        .isProjectMember(
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
     * 요청 projectId와 roomId의 프로젝트 번호 비교
     */
    private void validateProjectId(
            Long roomProjectId,
            Long expectedProjectId
    ) {
        if (
                expectedProjectId != null &&
                        !expectedProjectId.equals(
                                roomProjectId
                        )
        ) {
            throw new IllegalArgumentException(
                    "프로젝트와 채팅방 정보가 일치하지 않습니다."
            );
        }
    }


    /*
     * 현재 로그인 사용자 조회
     *
     * principal.getName()은 로그인 userId입니다.
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
            return userService.findByUserName(
                    principal.getName()
            );

        } catch (IllegalArgumentException exception) {
            throw new AccessDeniedException(
                    "로그인 사용자 정보를 찾을 수 없습니다."
            );
        }
    }
}