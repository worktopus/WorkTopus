package com.example.WorkTopus.projects.repository;

import com.example.WorkTopus.projects.entity.BoardFile;
import com.example.WorkTopus.projects.dto.response.ProjectFileResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardFileRepository extends JpaRepository<BoardFile, Long> {

    List<BoardFile> findByBoardIdAndDeletedYnOrderByCreatedAtAsc(
            Long boardId,
            String deletedYn
    );

    @Query("""
        SELECT new com.example.WorkTopus.projects.dto.response.ProjectFileResponse(
            f.id,
            f.boardId,
            f.originalName,
            f.fileExtension,
            f.fileSize,
            b.title,
            b.writerName,
            f.createdAt
        )
        FROM BoardFile f
        JOIN Board b ON b.id = f.boardId
        WHERE b.projectId = :projectId
          AND b.deletedYn = 'N'
          AND f.deletedYn = 'N'
        ORDER BY f.createdAt DESC
    """)
    List<ProjectFileResponse> findProjectFiles(
            @Param("projectId") Long projectId
    );

    @Query("""
        SELECT new com.example.WorkTopus.projects.dto.response.ProjectFileResponse(
            f.id,
            f.boardId,
            f.originalName,
            f.fileExtension,
            f.fileSize,
            b.title,
            b.writerName,
            f.createdAt
        )
        FROM BoardFile f
        JOIN Board b ON b.id = f.boardId
        WHERE b.projectId = :projectId
          AND b.deletedYn = 'N'
          AND f.deletedYn = 'N'
        ORDER BY f.createdAt DESC
    """)
    List<ProjectFileResponse> findRecentProjectFiles(
            @Param("projectId") Long projectId,
            org.springframework.data.domain.Pageable pageable
    );

    @Query("""
        SELECT COUNT(f)
        FROM BoardFile f
        JOIN Board b ON b.id = f.boardId
        WHERE b.projectId = :projectId
          AND b.deletedYn = 'N'
          AND f.deletedYn = 'N'
    """)
    long countProjectFiles(@Param("projectId") Long projectId);
}
