package com.example.WorkTopus.chat.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig
        implements WebSocketMessageBrokerConfigurer {

    /*
     * STOMP 구독 요청의
     * 채팅방 접근 권한을 검사하는 인터셉터입니다.
     */
    private final WebSocketAuthorizationInterceptor
            webSocketAuthorizationInterceptor;


    /*
     * 메시지 브로커 설정
     */
    @Override
    public void configureMessageBroker(
            MessageBrokerRegistry registry
    ) {

        /*
         * 서버가 클라이언트에게 전달하는 주소
         *
         * 예:
         *
         * /topic/chat/project_22_group
         * /topic/presence
         */
        registry.enableSimpleBroker(
                "/topic"
        );


        /*
         * 클라이언트가 서버의
         * @MessageMapping 메서드로 보내는 주소 앞부분
         *
         * 예:
         *
         * /app/chat.send
         */
        registry.setApplicationDestinationPrefixes(
                "/app"
        );
    }


    /*
     * 클라이언트에서 서버로 들어오는
     * STOMP 메시지 채널 설정
     */
    @Override
    public void configureClientInboundChannel(
            ChannelRegistration registration
    ) {

        /*
         * SUBSCRIBE 요청이 들어오면
         * WebSocketAuthorizationInterceptor를 실행합니다.
         */
        registration.interceptors(
                webSocketAuthorizationInterceptor
        );
    }


    /*
     * WebSocket 연결 주소 설정
     */
    @Override
    public void registerStompEndpoints(
            StompEndpointRegistry registry
    ) {

        /*
         * 브라우저 연결 주소:
         *
         * /chat
         *
         * SockJS를 사용하여 WebSocket을 지원하지 않는
         * 환경에서도 대체 연결 방식을 사용할 수 있습니다.
         */
        registry.addEndpoint(
                        "/chat"
                )
                .setAllowedOriginPatterns(
                        "*"
                )
                .withSockJS();
    }
}