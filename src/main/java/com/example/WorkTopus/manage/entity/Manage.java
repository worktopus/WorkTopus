package com.example.WorkTopus.manage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "PROJECTS")
@Getter
@Setter
@NoArgsConstructor
public class Manage {

    @Id
    // [교정] 자동 생성 시퀀스 충돌을 차단하기 위해 수동 매핑 구조 또는 기본 테이블 매핑 컬럼 정의로 명확화
    @Column(name = "ID")
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

    public void updateLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }
}
