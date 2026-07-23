package com.example.WorkTopus.Notification.service; // 또는 현재 패키지 경로

import com.example.WorkTopus.Notification.entity.Notification;
import com.example.WorkTopus.Notification.entity.NotificationType;
import com.example.WorkTopus.entity.Users; // Users 위치에 맞게 import

import java.util.List;

public interface NotificationService {

    // 1. 알림 생성
    void createNotification(Users user, String message, String url, NotificationType type);

    // 2. 알림 목록 조회
    List<Notification> getNotifications(Long userNum);

    // 3. 안 읽은 알림 개수
    long getUnreadCount(Long userNum);

    // 4. 알림 읽음 처리
    void markAsRead(Long notificationId);

    void deleteNotification(Long id);

    void markAllAsRead(Long userNum);
}