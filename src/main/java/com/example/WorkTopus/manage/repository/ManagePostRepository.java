package com.example.WorkTopus.manage.repository;

import com.example.WorkTopus.manage.dto.ManagePostListItemDto;
import com.example.WorkTopus.manage.dto.ManagePostPageDto;
import com.example.WorkTopus.manage.dto.ManagePostStatisticsDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ManagePostRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public ManagePostStatisticsDto findStatistics(Long projectId) {
        String sql = """
                SELECT
            (SELECT COUNT(*)
               FROM PROJECT_BOARD PB
              WHERE PB.PROJECT_ID = :projectId
                AND NVL(PB.IS_DELETED, 'N') = 'N') AS TOTAL_POSTS,

            (SELECT COUNT(*)
               FROM PROJECT_BOARD PB
              WHERE PB.PROJECT_ID = :projectId
                AND NVL(PB.IS_DELETED, 'N') = 'N'
                AND NVL(PB.IS_NOTICE, 'N') = 'Y') AS NOTICE_POSTS,

            (SELECT COUNT(*)
               FROM PROJECT_BOARD_COMMENT BC
               JOIN PROJECT_BOARD PB
                 ON PB.BOARD_ID = BC.BOARD_ID
              WHERE PB.PROJECT_ID = :projectId
                AND NVL(PB.IS_DELETED, 'N') = 'N') AS TOTAL_COMMENTS,

            (SELECT COUNT(*)
               FROM PROJECT_BOARD_FILE BF
               JOIN PROJECT_BOARD PB
                 ON PB.BOARD_ID = BF.BOARD_ID
              WHERE PB.PROJECT_ID = :projectId
                AND NVL(PB.IS_DELETED, 'N') = 'N'
                AND NVL(BF.IS_DELETED, 'N') = 'N') AS TOTAL_FILES
        FROM DUAL
        """;

        Object[] row = (Object[]) entityManager.createNativeQuery(sql)
                .setParameter("projectId", projectId)
                .getSingleResult();

        return new ManagePostStatisticsDto(
                toLong(row[0]),
                toLong(row[1]),
                toLong(row[2]),
                toLong(row[3])
        );
    }

    private String toStringValue(Object value) {
        return value == null ? "" : value.toString();
    }

    public ManagePostPageDto findPosts(
            Long projectId,
            String category,
            String keyword,
            int page,
            int size
    ) {
        page = Math.max(page, 0);
        size = size <= 0 ? 10 : size;

        StringBuilder where = new StringBuilder("""
            FROM PROJECT_BOARD PB
            WHERE PB.PROJECT_ID = :projectId
              AND NVL(PB.IS_DELETED, 'N') = 'N'
            """);

        boolean hasCategory =
                category != null
                        && !category.isBlank()
                        && !"ALL".equalsIgnoreCase(category);

        boolean hasKeyword =
                keyword != null
                        && !keyword.isBlank();

        if (hasCategory) {
            where.append(" AND PB.CATEGORY = :category ");
        }

        if (hasKeyword) {
            where.append("""
                AND (
                    LOWER(PB.TITLE) LIKE :keyword
                    OR LOWER(PB.WRITER_NAME) LIKE :keyword
                )
                """);
        }

        Query countQuery =
                entityManager.createNativeQuery("SELECT COUNT(*) " + where);


        bindParameters(
                countQuery,
                projectId,
                category,
                keyword,
                hasCategory,
                hasKeyword
        );

        long totalElements = toLong(countQuery.getSingleResult());

        String dataSql = """
            SELECT
                PB.BOARD_ID,
                PB.TITLE,
                PB.CATEGORY,
                PB.WRITER_NAME,
                NVL(PB.VIEW_COUNT, 0),
                PB.CREATED_AT,
                NVL(PB.IS_NOTICE, 'N')
            """ + where + """
             ORDER BY
                NVL(PB.IS_NOTICE, 'N') DESC,
                PB.CREATED_AT DESC
            """;

        Query dataQuery = entityManager.createNativeQuery(dataSql);

        bindParameters(
                dataQuery,
                projectId,
                category,
                keyword,
                hasCategory,
                hasKeyword
        );

        dataQuery.setFirstResult(page * size);
        dataQuery.setMaxResults(size);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();

        List<ManagePostListItemDto> content = new ArrayList<>();

        for (Object[] row : rows) {
            content.add(new ManagePostListItemDto(
                    toLong(row[0]),
                    toStringValue(row[1]),
                    toStringValue(row[2]),
                    toStringValue(row[3]),
                    toLong(row[4]),
                    toLocalDateTime(row[5]),
                    "Y".equalsIgnoreCase(toStringValue(row[6]))
            ));
        }

        int totalPages =
                totalElements == 0
                        ? 0
                        : (int) Math.ceil((double) totalElements / size);

        return new ManagePostPageDto(
                content,
                page,
                size,
                totalElements,
                totalPages
        );
    }

    private void bindParameters(
            Query query,
            Long projectId,
            String category,
            String keyword,
            boolean hasCategory,
            boolean hasKeyword
    ) {
        query.setParameter("projectId", projectId);

        if (hasCategory) {
            query.setParameter("category", category);
        }

        if (hasKeyword) {
            query.setParameter("keyword", "%" + keyword.trim().toLowerCase() + "%");
        }
    }

    private long toLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        throw new IllegalArgumentException("지원하지 않는 날짜 형식입니다: " + value.getClass().getName());
    }
}
