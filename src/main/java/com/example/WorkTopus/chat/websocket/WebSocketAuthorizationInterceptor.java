package com.example.WorkTopus.chat.websocket;

import com.example.WorkTopus.chat.service.ChatRoomAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.security.Principal;


@Component
@RequiredArgsConstructor
public class WebSocketAuthorizationInterceptor
        implements ChannelInterceptor {

    /*
     * 채팅방 접근 권한을 공통으로 검사하는 서비스입니다.
     */
    private final ChatRoomAccessService
            chatRoomAccessService;


    /*
     * 채팅방 실시간 메시지 구독 주소 앞부분
     *
     * 예:
     *
     * /topic/chat/project_22_group
     */
    private static final String CHAT_SUBSCRIBE_PREFIX =
            "/topic/chat/";


    /*
     * 클라이언트가 서버로 전송하는
     * WebSocket 메시지를 처리하기 전에 실행됩니다.
     */
    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {

        /*
         * 현재 STOMP 메시지의 명령,
         * 목적지, 로그인 사용자 정보를지의 명령,
         * 목적 읽습니다.
         */
        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(
                        message
                );


        StompCommand command =
                accessor.getCommand();


        /*
         * STOMP 명령 정보가 없는 내부 메시지는
         * 그대로 통과시킵니다.
         */
        if (command == null) {
            return message;
        }


        /*
         * 이번 단계에서는 채팅방 구독 요청만 검사합니다.
         *
         * CONNECT, SEND, DISCONNECT 등의 요청은
         * 기존 처리로 넘깁니다.
         */
        if (
                command != StompCommand.SUBSCRIBE
        ) {
            return message;
        }


        String destination =
                accessor.getDestination();


        /*
         * 목적지가 없으면 정상적인 구독 요청이 아닙니다.
         */
        if (
                destination == null
                        || destination.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "구독 주소가 없습니다."
            );
        }


        /*
         * 접속 상태 알림처럼 채팅방이 아닌 구독은
         * 이번 채팅방 권한 검사 대상에서 제외합니다.
         *
         * 예:
         *
         * /topic/presence
         */
        if (
                !destination.startsWith(
                        CHAT_SUBSCRIBE_PREFIX
                )
        ) {
            return message;
        }


        /*
         * Spring Security가 확인한
         * 실제 로그인 사용자 정보입니다.
         */
        Principal principal =
                accessor.getUser();


        if (
                principal == null
                        || principal.getName() == null
                        || principal.getName().isBlank()
        ) {
            throw new AccessDeniedException(
                    "로그인이 필요합니다."
            );
        }


        /*
         * 전체 목적지에서 실제 roomId만 추출합니다.
         *
         * /topic/chat/project_22_group
         *
         * → project_22_group
         */
        String roomId =
                destination.substring(
                        CHAT_SUBSCRIBE_PREFIX.length()
                );


        if (roomId.isBlank()) {
            throw new IllegalArgumentException(
                    "채팅방 번호가 없습니다."
            );
        }


        /*
         * 채팅방 접근 권한 검사
         *
         * 단체방:
         * - 프로젝트 참여자인지 확인
         *
         * 개인방:
         * - 프로젝트 참여자인지 확인
         * - 로그인 사용자가 개인방 당사자인지 확인
         * - 두 사용자 모두 프로젝트 참여자인지 확인
         */
        chatRoomAccessService
                .requireRoomAccess(
                        roomId,
                        principal
                );


        /*
         * 모든 권한 검사를 통과한 구독 요청만
         * 실제 메시지 채널로 전달합니다.
         */
        return message;
    }
}