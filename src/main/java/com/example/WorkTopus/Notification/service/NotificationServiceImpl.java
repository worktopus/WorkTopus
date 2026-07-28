package com.example.WorkTopus.Notification.service;


import com.example.WorkTopus.Notification.entity.Notification;
import com.example.WorkTopus.Notification.entity.NotificationType;
import com.example.WorkTopus.Notification.repository.NotificationRepository;
import com.example.WorkTopus.entity.ProjectMember;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final ProjectMemberRepository projectMemberRepository;

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

    // 칸반 검토 요청 알림 구현
    @Override
    public void createKanbanReviewNotification(Long projectId, Long currentSenderNum, String cardTitle) {
        // 1. 해당 프로젝트의 멤버 목록 조회
        List<ProjectMember> members = projectMemberRepository.findByProject_Id(projectId);

        String message = "[" + cardTitle + "] 카드가 검토 상태로 전환되었습니다.";
        String url = "/projects/" + projectId + "/boards/kanban";

        // 2. 본인을 제외한 모든 멤버에게 알림 생성
        for (ProjectMember member : members) {
            Users user = member.getUser(); // 멤버 엔티티에서 Users 객체 추출

            // 본인에게는 알림을 보내지 않음
            if (user.getUserNum().equals(currentSenderNum)) {
                continue;
            }

            // 기존 createNotification 재활용
            // NotificationType.KANBAN_REVIEW 또는 적절한 NotificationType 지정
            createNotification(user, message, url, NotificationType.KANBAN_REVIEW);
        }
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

    // 알림 삭제
    @Transactional
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    // 전체 알림 일괄 읽음 처리
    @Override
    public void markAllAsRead(Long userNum) {
        List<Notification> unreadNotifications = notificationRepository.findByUser_UserNumAndReadYn(userNum, "N");
        for (Notification notification : unreadNotifications) {
            notification.markAsRead(); // 엔티티의 readYn = "Y" 변경
        }
    }

}