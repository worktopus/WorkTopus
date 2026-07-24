package com.example.WorkTopus.Notification.entity;

import com.example.WorkTopus.entity.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICATION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NOTIFICATION_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_NUM", nullable = false)
    private Users user;

    @Column(name = "MESSAGE", nullable = false, length = 500)
    private String message;

    @Column(name = "URL", length = 255)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "NOTIFICATION_TYPE", length = 30)
    private NotificationType type;

    @Column(name = "IS_READ", nullable = false, length = 1)
    private String readYn = "N";

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Notification(Users user, String message, String url, NotificationType type) {
        this.user = user;
        this.message = message;
        this.url = url;
        this.type = type;
        this.readYn = "N";
    }

   // 알림 읽음 처리
    public void markAsRead() {
        this.readYn = "Y";
    }

}