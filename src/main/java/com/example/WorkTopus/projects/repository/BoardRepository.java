package com.example.WorkTopus.projects.repository;

import com.example.WorkTopus.projects.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    // 목록 조회 + 페이징
    Page<Board> findByProjectIdAndDeletedYnOrderByNoticeYnDescCreatedAtDesc(
            Long projectId,
            String deletedYn,
            Pageable pageable
    );

    Page<Board> findByProjectIdAndDeletedYnOrderByCreatedAtDesc(
            Long projectId,
            String deletedYn,
            Pageable pageable
    );

    long countByProjectIdAndDeletedYn(Long projectId, String deletedYn);

    // 상세/수정/삭제용 단건 조회
    Optional<Board> findByIdAndProjectIdAndDeletedYn(
            Long boardId,
            Long projectId,
            String deletedYn
    );

    // 검색
    @Query("""
        SELECT b
        FROM Board b
        WHERE b.projectId = :projectId
          AND b.deletedYn = 'N'
          AND (
              LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(b.writerName) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(b.category) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
        ORDER BY b.noticeYn DESC, b.createdAt DESC
    """)
    Page<Board> searchBoards(
            @Param("projectId") Long projectId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    Optional<Board> findByIdAndProjectId(Long boardId, Long projectId);
}
