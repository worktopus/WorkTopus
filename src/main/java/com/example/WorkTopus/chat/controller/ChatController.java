package com.example.WorkTopus.chat.controller;

import com.example.WorkTopus.chat.dto.ChatMessage;
import com.example.WorkTopus.chat.service.ChatRoomAccessService;
import com.example.WorkTopus.chat.service.ChatService;
import com.example.WorkTopus.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.List;


@Controller
@RequiredArgsConstructor
public class ChatController {

    /*
     * 채팅 메시지 최대 길이
     */
    private static final int MAX_MESSAGE_LENGTH =
            2000;


    /*
     * 채팅 메시지 저장과
     * 이전 대화 조회를 처리합니다.
     */
    private final ChatService
            chatService;


    /*
     * 단체방과 개인방 접근 권한을
     * 공통으로 검사합니다.
     */
    private final ChatRoomAccessService
            chatRoomAccessService;


    /*
     * 저장된 메시지를 해당 채팅방 구독자에게
     * 실시간으로 전송합니다.
     */
    private final SimpMessagingTemplate
            messagingTemplate;


    /*
     * =====================================================
     * 채팅 메시지 전송
     * =====================================================
     *
     * 클라이언트 전송 주소:
     *
     * /app/chat.send
     */
    @MessageMapping("/chat.send")
    public void send(
            ChatMessage message,
            Principal principal
    ) {

        /*
         * 메시지 내용과 채팅방 접근 권한을 검사합니다.
         *
         * 모든 검사를 통과하면
         * 실제 로그인 사용자 정보를 반환받습니다.
         */
        Users loginUser =
                validateMessage(
                        message,
                        principal
                );


        /*
         * 프런트에서 보낸 senderNum과 senderName은
         * 브라우저에서 조작될 수 있으므로 사용하지 않습니다.
         /*
         * 프런트에서 보낸 senderNum과 senderName은
         * *
         * Spring Security의 실제 로그인 사용자 정보로
         * 다시 설정합니다.
         */
        message.setSenderNum(
                loginUser.getUserNum()
        );

        message.setSenderName(
                loginUser.getName()
        );


        /*
         * 채팅방 ID의 앞뒤 공백을 제거합니다.
         */
        message.setRoomId(
                message.getRoomId().trim()
        );


        /*
         * 메시지 내용의 앞뒤 공백을 제거합니다.
         */
        message.setMessage(
                message.getMessage().trim()
        );


        /*
         * 현재 전송되는 메시지는
         * 일반 대화 메시지입니다.
         */
        message.setType(
                "TALK"
        );


        /*
         * 브라우저에서 전달한 시간이 아닌
         * 서버의 현재 시간을 사용합니다.
         */
        message.setCreatedAt(
                OffsetDateTime.now()
        );


        /*
         * 메시지를 DB에 저장합니다.
         *
         * 저장 후 messageId 등이 포함된
         * ChatMessage 객체를 반환받습니다.
         */
        ChatMessage savedMessage =
                chatService.save(
                        message
                );


        /*
         * 해당 채팅방을 구독하고 있는 사용자들에게
         * 저장된 메시지를 실시간 전송합니다.
         *
         * 예:
         *
         * /topic/chat/project_22_group
         *
         * /topic/chat/project_22_private_3_8
         */
        messagingTemplate.convertAndSend(
                "/topic/chat/"
                        + savedMessage.getRoomId(),
                savedMessage
        );
    }


    /*
     * =====================================================
     * 채팅방 이전 대화 조회
     * =====================================================
     *
     * GET
     * /chat/history/{roomId}
     *
     * 예:
     *
     * GET
     * /chat/history/project_22_group
     *
     * GET
     * /chat/history/project_22_private_3_8
     */
    @GetMapping(
            "/chat/history/{roomId}"
    )
    @ResponseBody
    public List<ChatMessage> getHistory(
            @PathVariable String roomId,
            Principal principal
    ) {

        /*
         * 이전 대화를 조회하기 전에
         * 현재 로그인 사용자가 해당 방에
         * 접근할 수 있는지 검사합니다.
         */
        chatRoomAccessService
                .requireRoomAccess(
                        roomId,
                        principal
                );


        /*
         * 권한 검사를 통과한 경우에만
         * 해당 채팅방의 메시지를 조회합니다.
         */
        return chatService.getMessages(
                roomId.trim()
        );
    }


    /*
     * =====================================================
     * 메시지 내용과 채팅방 접근 권한 검사
     * =====================================================
     *
     * 모든 검사를 통과하면
     * 실제 로그인 Users 객체를 반환합니다.
     */
    private Users validateMessage(
            ChatMessage message,
            Principal principal
    ) {

        /*
         * 메시지 객체 확인
         */
        if (message == null) {
            throw new IllegalArgumentException(
                    "메시지 정보가 없습니다."
            );
        }


        /*
         * 프로젝트 번호 확인
         */
        if (
                message.getProjectId() == null
                        || message.getProjectId() <= 0
        ) {
            throw new IllegalArgumentException(
                    "올바른 프로젝트 번호가 필요합니다."
            );
        }


        /*
         * 채팅방 번호 확인
         */
        if (
                message.getRoomId() == null
                        || message.getRoomId().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "채팅방 번호가 없습니다."
            );
        }


        /*
         * 메시지 내용 확인
         */
        if (
                message.getMessage() == null
                        || message.getMessage().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "메시지 내용을 입력하세요."
            );
        }


        /*
         * 메시지 최대 길이 확인
         */
        if (
                message.getMessage()
                        .trim()
                        .length()
                        > MAX_MESSAGE_LENGTH
        ) {
            throw new IllegalArgumentException(
                    "메시지는 2000자 이하로 입력하세요."
            );
        }


        /*
         * 채팅방 ID에서 실제 프로젝트 번호를 추출합니다.
         *
         * project_22_group
         * → 22
         *
         * project_22_private_3_8
         * → 22
         */
        Long roomProjectId =
                chatRoomAccessService
                        .extractProjectId(
                                message.getRoomId()
                        );


        /*
         * 메시지의 projectId와
         * roomId에 포함된 프로젝트 번호가
         * 일치하는지 확인합니다.
         *
         * 예:
         *
         * projectId = 22
         * roomId = project_30_group
         *
         * 위와 같은 요청은 차단됩니다.
         */
        if (
                !message.getProjectId()
                        .equals(
                                roomProjectId
                        )
        ) {
            throw new IllegalArgumentException(
                    "프로젝트와 채팅방 정보가 일치하지 않습니다."
            );
        }


        /*
         * 최종 채팅방 접근 권한 검사
         *
         * 단체방:
         * - 프로젝트 참여자인지 검사
         *
         * 개인방:
         * - 프로젝트 참여자인지 검사
         * - 개인방 당사자인지 검사
         * - 두 사용자 모두 프로젝트 참여자인지 검사
         */
        return chatRoomAccessService
                .requireRoomAccess(
                        message.getRoomId(),
                        principal
                );
    }
}