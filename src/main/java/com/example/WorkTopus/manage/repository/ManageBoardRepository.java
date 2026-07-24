package com.example.WorkTopus.manage.repository;

import com.example.WorkTopus.manage.entity.Manage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ManageBoardRepository extends JpaRepository<Manage, Long> {

    /**
     * [오라클 실시간 연동 1]
     * 특정 워크스페이스 번호에 묶여 있는 PROJECT_BOARD 테이블의 전체 누적 게시글 수를 카운트합니다.
     */
    @Query(value = "SELECT COUNT(*) FROM PROJECT_BOARD WHERE PROJECT_ID = :workspaceId", nativeQuery = true)
    int countTotalPostsByWorkspaceId(@Param("workspaceId") Long workspaceId);

    /**
     * [오라클 실시간 연동 2]
     * 카테고리명(NOTICE, FREE 등)과 완벽히 일치하는 글 목록을
     * 팀장 필독 고정(IS_NOTICE='Y')이 가장 먼저 오고, 그 다음 최신 등록 순서대로 정렬하여 긁어옵니다.
     */
    @Query(value = "SELECT BOARD_ID as \"id\", TITLE as \"title\", WRITER_NAME as \"writer\", TO_CHAR(UPDATED_AT, 'YYYY-MM-DD') as \"date\", VIEW_COUNT as \"views\", IS_NOTICE as \"isPinned\" " +
            "FROM PROJECT_BOARD " +
            "WHERE PROJECT_ID = :workspaceId AND CATEGORY = :category " +
            "ORDER BY IS_NOTICE DESC, UPDATED_AT DESC", nativeQuery = true)
    List<Map<String, Object>> findBoardContentsByWorkspaceIdAndCategory(@Param("workspaceId") Long workspaceId, @Param("category") String category);
}
