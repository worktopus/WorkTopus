package com.example.WorkTopus.chat.websocket;

import com.example.WorkTopus.chat.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class WebSocketPresenceEventListener {

    private final PresenceService
            presenceService;


    /*
     * WebSocket 연결 성공
     */
    @EventListener
    public void handleConnected(
            SessionConnectedEvent event
    ) {
        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(
                        event.getMessage()
                );

        Principal principal =
                accessor.getUser();

        presenceService.connect(
                accessor.getSessionId(),
                principal
        );
    }


    /*
     * WebSocket 연결 종료
     */
    @EventListener
    public void handleDisconnected(
            SessionDisconnectEvent event
    ) {
        presenceService.disconnect(
                event.getSessionId()
        );
    }
}