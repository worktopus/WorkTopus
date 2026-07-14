package com.example.worktopus.projects.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "PROJECT_BOARD")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOARD_ID")
    private Long id;

    @Column(name = "PROJECT_ID", nullable = false)
    private Long projectId;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Lob
    @Column(name = "CONTENT")
    private String content;

    @Column(name = "WRITER_NAME", nullable = false, length = 100)
    private String writerName;

    @Column(name = "VIEW_COUNT", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "IS_NOTICE", nullable = false, length = 1)
    private String noticeYn = "N";

    @Column(name = "IS_DELETED", nullable = false, length = 1)
    private String deletedYn = "N";

    @CreationTimestamp
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    @Column(name = "CATEGORY", nullable = false, length = 30)
    private String category = "FREE";

    @Column(name = "TAG", length = 200)
    private String tag;

    public Board(Long projectId, String title, String content, String writerName, boolean notice, String category, String tag) {
        this.projectId = projectId;
        this.title = title;
        this.content = content;
        this.writerName = writerName;
        this.noticeYn = notice ? "Y" : "N";
        this.category = category;
        this.tag = tag;
    }

    /**
     * 게시글 수정
     */
    public void update(String title, String content, boolean notice, String category, String tag) {
        this.title = title;
        this.content = content;
        this.noticeYn = notice ? "Y" : "N";
        this.category = category;
        this.tag = tag;
    }

    /**
     * 조회수 증가
     */
    public void increaseViewCount() {
        this.viewCount++;
    }

    /**
     * Soft Delete
     */
    public void delete() {
        this.deletedYn = "Y";
        this.deletedAt = LocalDateTime.now();
    }
}
