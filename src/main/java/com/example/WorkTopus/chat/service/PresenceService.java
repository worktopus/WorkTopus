package com.example.WorkTopus.chat.service;

import com.example.WorkTopus.chat.dto.PresenceStatus;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private static final String PRESENCE_TOPIC =
            "/topic/presence";

    private final UserService userService;

    private final SimpMessagingTemplate
            messagingTemplate;

    /*
     * WebSocket 세션 ID별 사용자 정보
     */
    private final ConcurrentHashMap<String, ConnectedUser>
            connectedUsersBySession =
            new ConcurrentHashMap<>();

    /*
     * 사용자별 활성 WebSocket 세션
     *
     * 브라우저 탭을 여러 개 열었을 때
     * 한 탭만 닫아도 오프라인이 되는 문제를 막습니다.
     */
    private final ConcurrentHashMap<Long, Set<String>>
            sessionsByUser =
            new ConcurrentHashMap<>();


    /*
     * WebSocket 접속
     */
    public void connect(
            String sessionId,
            Principal principal
    ) {
        if (
                sessionId == null ||
                        sessionId.isBlank() ||
                        principal == null ||
                        principal.getName() == null ||
                        principal.getName().isBlank()
        ) {
            return;
        }

        Users loginUser;

        try {
            loginUser =
                    userService.findByUserId(
                            principal.getName()
                    );

        } catch (IllegalArgumentException exception) {
            return;
        }

        ConnectedUser connectedUser =
                new ConnectedUser(
                        loginUser.getUserNum(),
                        loginUser.getName()
                );

        connectedUsersBySession.put(
                sessionId,
                connectedUser
        );

        AtomicBoolean firstSession =
                new AtomicBoolean(false);

        sessionsByUser.compute(
                connectedUser.userNum(),
                (userNum, sessions) -> {

                    Set<String> activeSessions =
                            sessions;

                    if (activeSessions == null) {
                        activeSessions =
                                ConcurrentHashMap.newKeySet();
                    }

                    firstSession.set(
                            activeSessions.isEmpty()
                    );

                    activeSessions.add(sessionId);

                    return activeSessions;
                }
        );

        /*
         * 사용자의 첫 번째 접속일 때만
         * 온라인 상태를 전송합니다.
         */
        if (firstSession.get()) {
            publishPresence(
                    connectedUser,
                    true
            );
        }
    }


    /*
     * WebSocket 접속 해제
     */
    public void disconnect(
            String sessionId
    ) {
        if (
                sessionId == null ||
                        sessionId.isBlank()
        ) {
            return;
        }

        ConnectedUser connectedUser =
                connectedUsersBySession.remove(
                        sessionId
                );

        if (connectedUser == null) {
            return;
        }

        AtomicBoolean lastSession =
                new AtomicBoolean(false);

        sessionsByUser.computeIfPresent(
                connectedUser.userNum(),
                (userNum, sessions) -> {

                    sessions.remove(sessionId);

                    if (sessions.isEmpty()) {
                        lastSession.set(true);

                        return null;
                    }

                    return sessions;
                }
        );

        /*
         * 모든 브라우저 탭과 연결이 종료된 경우에만
         * 오프라인 상태를 전송합니다.
         */
        if (lastSession.get()) {
            publishPresence(
                    connectedUser,
                    false
            );
        }
    }


    /*
     * 현재 접속 여부
     */
    public boolean isOnline(
            Long userNum
    ) {
        if (userNum == null) {
            return false;
        }

        Set<String> sessions =
                sessionsByUser.get(userNum);

        return sessions != null &&
                !sessions.isEmpty();
    }


    /*
     * 프런트의 /topic/presence 구독자에게 전송
     */
    private void publishPresence(
            ConnectedUser connectedUser,
            boolean online
    ) {
        PresenceStatus status =
                PresenceStatus.builder()
                        .projectId(null)
                        .userNum(
                                connectedUser.userNum()
                        )
                        .name(
                                connectedUser.name()
                        )
                        .online(online)
                        .build();

        messagingTemplate.convertAndSend(
                PRESENCE_TOPIC,
                status
        );
    }


    private record ConnectedUser(
            Long userNum,
            String name
    ) {
    }
}