package com.example.worktopus.projects.repository;

import com.example.worktopus.projects.entity.BoardFile;
import com.example.worktopus.projects.dto.response.ProjectFileResponse;
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
        SELECT new com.example.worktopus.projects.dto.response.ProjectFileResponse(
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
}
