package com.example.WorkTopus.manage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "MANAGES")
@Getter
@Setter
@NoArgsConstructor
public class Manage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "manage_seq")
    @SequenceGenerator(name = "manage_seq", sequenceName = "MANAGES_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "INVITE_CODE")
    private String inviteCode;

    @Column(name = "NAME")
    private String name;

    @Column(name = "OWNER_ID")
    private Long ownerId;

    @Column(name = "VISIBILITY")
    private String visibility;

    @Column(name = "LOGO_PATH")
    private String logoPath;

    @Column(name = "ARCHIVE_STATUS")
    private String archiveStatus;

    // 비즈니스 로직: 설정값 업데이트
    public void updateGeneralSettings(String name, String visibility, String archiveStatus) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
        if (visibility != null && !visibility.trim().isEmpty()) {
            this.visibility = visibility.trim();
        }
        if (archiveStatus != null && !archiveStatus.trim().isEmpty()) {
            this.archiveStatus = archiveStatus.trim();
        }
    }

    // 비즈니스 로직: 로고 이미지 경로 변경
    public void updateLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }
}
