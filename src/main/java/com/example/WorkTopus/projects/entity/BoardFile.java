package com.example.WorkTopus.projects.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "PROJECT_BOARD_FILE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FILE_ID")
    private Long id;

    @Column(name = "BOARD_ID", nullable = false)
    private Long boardId;

    @Column(name = "ORIGINAL_NAME", nullable = false, length = 255)
    private String originalName;

    @Column(name = "STORED_NAME", nullable = false, length = 255)
    private String storedName;

    @Column(name = "FILE_URL", length = 1000)
    private String fileUrl;

    @Column(name = "FILE_EXTENSION", nullable = false, length = 20)
    private String fileExtension;

    @Column(name = "FILE_SIZE", nullable = false)
    private Long fileSize;

    @Column(name = "CONTENT_TYPE", length = 100)
    private String contentType;

    @Column(name = "IS_DELETED", nullable = false, length = 1)
    private String deletedYn = "N";

    @CreationTimestamp
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    public BoardFile(Long boardId, String originalName, String storedName, String fileUrl,
                     String fileExtension, Long fileSize, String contentType) {
        this.boardId = boardId;
        this.originalName = originalName;
        this.storedName = storedName;
        this.fileUrl = fileUrl;
        this.fileExtension = fileExtension;
        this.fileSize = fileSize;
        this.contentType = contentType;
    }

    /**
     * Soft Delete
     */
    public void delete() {
        this.deletedYn = "Y";
        this.deletedAt = LocalDateTime.now();
    }
}
