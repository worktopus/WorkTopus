package com.example.WorkTopus.Notification.repository;

import com.example.WorkTopus.Notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 1. 특정 유저의 알림 목록 최신순 조회 (마이페이지 알림 탭용)
    List<Notification> findByUser_UserNumOrderByCreatedAtDesc(Long userNum);

    // 2. 특정 유저의 읽지 않은('N') 알림 목록만 조회
    List<Notification> findByUser_UserNumAndReadYnOrderByCreatedAtDesc(Long userNum, String readYn);

    // 3. 특정 유저의 읽지 않은 알림 개수 조회 (헤더 종 모양 뱃지)
    long countByUser_UserNumAndReadYn(Long userNum, String readYn);

    List<Notification> findByUser_UserNumAndReadYn(Long userNum, String readYn);

}