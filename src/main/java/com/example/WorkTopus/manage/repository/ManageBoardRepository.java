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
     * [📊 오라클 정밀 동기화 1]
     * 특정 프로젝트 번호(PROJECT_ID)에 묶여 있으면서 삭제되지 않은(IS_DELETED = 'N')
     * PROJECT_BOARD 테이블의 전체 누적 게시글 수를 실시간으로 카운트합니다.
     */
    @Query(value = "SELECT COUNT(*) FROM PROJECT_BOARD WHERE PROJECT_ID = :workspaceId AND IS_DELETED = 'N'", nativeQuery = true)
    int countTotalPostsByWorkspaceId(@Param("workspaceId") Long workspaceId);

    /**
     * [📝 오라클 정밀 동기화 2]
     * 오라클 PROJECT_BOARD 테이블 스펙에 맞춤형 별칭(Alias)을 부여하여 긁어옵니다.
     * 프론트엔드 자바스크립트(board-list.js 규격)가 즉시 읽어 들일 수 있도록 소문자 Key 포맷 구조로 안전하게 사출합니다.
     * 팀장 공지 지정글(IS_NOTICE = 'Y')이 무조건 최상단에 오고, 그 다음 최신 등록 순서(CREATED_AT DESC)대로 한 줄 정렬됩니다.
     */
    @Query(value = "SELECT BOARD_ID as \"id\", TITLE as \"title\", CONTENT as \"contentPreview\", WRITER_NAME as \"writerName\", TO_CHAR(CREATED_AT, 'YYYY-MM-DD') as \"createdAt\", VIEW_COUNT as \"viewCount\", IS_NOTICE as \"notice\" " +
            "FROM PROJECT_BOARD " +
            "WHERE PROJECT_ID = :workspaceId AND IS_DELETED = 'N' " +
            "ORDER BY IS_NOTICE DESC, CREATED_AT DESC", nativeQuery = true)
    List<Map<String, Object>> findBoardContentsByWorkspaceId(@Param("workspaceId") Long workspaceId);
}
