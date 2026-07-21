package com.example.WorkTopus.Notification.service;


import com.example.WorkTopus.Notification.entity.Notification;
import com.example.WorkTopus.Notification.entity.NotificationType;
import com.example.WorkTopus.Notification.repository.NotificationRepository;
import com.example.WorkTopus.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    // 1. 알림 생성 (댓글/공지/초대 등록 시 호출할 공통 메서드)
    @Override
    public void createNotification(Users user, String message, String url, NotificationType type) {
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .url(url)
                .type(type)
                .build();
        notificationRepository.save(notification);
    }

    // 2. 마이페이지/헤더용 알림 목록 조회
    @Transactional(readOnly = true)
    @Override
    public List<Notification> getNotifications(Long userNum) {
        return notificationRepository.findByUser_UserNumOrderByCreatedAtDesc(userNum);
    }

    // 3. 안 읽은 알림 개수 (헤더 종 모양 뱃지용)
    @Transactional(readOnly = true)
    @Override
    public long getUnreadCount(Long userNum) {
        return notificationRepository.countByUser_UserNumAndReadYn(userNum, "N");
    }

    // 4. 알림 읽음 처리
    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다."));
        notification.markAsRead(); // 엔티티의 readYn = "Y" 변경
    }
}