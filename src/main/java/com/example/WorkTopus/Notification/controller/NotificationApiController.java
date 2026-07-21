package com.example.WorkTopus.Notification.controller;

import com.example.WorkTopus.Notification.entity.Notification;
import com.example.WorkTopus.Notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationApiController {

    private final NotificationService notificationService;

    // 1. 내 알림 목록 조회 API (마이페이지 알림 탭용)
    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(/* @AuthenticationPrincipal 또는 Session 유저정보 */) {
        // 로그인한 유저의 userNum을 가져옵니다. (예: Long userNum = loginUser.getUserNum();)
        Long userNum = 1L; // 테스트용 임시 userNum
        List<Notification> notifications = notificationService.getNotifications(userNum);
        return ResponseEntity.ok(notifications);
    }

    // 2. 안 읽은 알림 개수 조회 API (헤더 종 모양 뱃지)
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        Long userNum = 1L; // 테스트용 임시 userNum
        long count = notificationService.getUnreadCount(userNum);
        return ResponseEntity.ok(count);
    }

    // 3. 알림 읽음 처리 API
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable("id") Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}