package com.example.WorkTopus.Notification.controller;

import com.example.WorkTopus.Notification.entity.Notification;
import com.example.WorkTopus.Notification.service.NotificationService;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.repository.UserRepository; // 💡 UserRepository 경로 확인 필요
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationApiController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    // 1. 내 알림 목록 조회 API
    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        // 시큐리티 로그인 ID(이메일 또는 아이디)로 Users 엔티티 조회
        Users loginUser = userRepository.findByUserId(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("로그인 유저 정보가 없습니다."));

        List<Notification> notifications = notificationService.getNotifications(loginUser.getUserNum());
        return ResponseEntity.ok(notifications);
    }

    // 2. 안 읽은 알림 개수 조회 API
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(0L);
        }

        Users loginUser = userRepository.findByUserId(principal.getName()).orElse(null);
        if (loginUser == null) {
            return ResponseEntity.ok(0L);
        }

        long count = notificationService.getUnreadCount(loginUser.getUserNum());
        return ResponseEntity.ok(count);
    }

    // 3. 알림 읽음 처리 API
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable("id") Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    // 4. 개별 알림 삭제 API
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable("id") Long id) {
        notificationService.deleteNotification(id); // NotificationService에 delete 메서드 호출
        return ResponseEntity.ok().build();
    }

    // 5. 알림 전체 일괄 읽음 처리 API
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        Users loginUser = userRepository.findByUserId(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("로그인 유저 정보가 없습니다."));

        notificationService.markAllAsRead(loginUser.getUserNum());
        return ResponseEntity.ok().build();
    }
}