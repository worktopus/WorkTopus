package com.example.WorkTopus.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "AI_MEETING_SUMMARY",
        indexes = {

                /*
                 * 프로젝트별 회의록 조회용
                 */
                @Index(
                        name = "IDX_AI_MEETING_SUMMARY_PROJECT",
                        columnList = "PROJECT_ID"
                ),

                /*
                 * 프로젝트별 최신 회의록 조회용
                 */
                @Index(
                        name = "IDX_AI_MEETING_SUMMARY_PROJECT_TIME",
                        columnList = "PROJECT_ID, GENERATED_AT"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingSummaryEntity {

    /*
     * AI 회의록 PK
     */
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(
            name = "SUMMARY_ID"
    )
    private Long summaryId;


    /*
     * 프로젝트 PK
     *
     * 현재:
     * 임시 projectId 사용
     *
     * 추후 팀원 코드 병합 후:
     * Projects.id 값 사용
     */
    @Column(
            name = "PROJECT_ID",
            nullable = false
    )
    private Long projectId;


    /*
     * 요약한 채팅방 ID
     *
     * 예:
     * project_2_group
     */
    @Column(
            name = "ROOM_ID",
            nullable = false,
            length = 100
    )
    private String roomId;


    /*
     * AI가 생성한 전체 회의 요약
     */
    @Lob
    @Column(
            name = "SUMMARY_CONTENT",
            nullable = false
    )
    private String summaryContent;


    /*
     * 주요 결정 사항
     *
     * List<String>을 JSON 문자열로
     * 변환하여 저장합니다.
     *
     * 예:
     *
     * [
     *   "오후 3시에 통합 테스트 진행",
     *   "userNum을 사용자 식별자로 사용"
     * ]
     */
    @Lob
    @Column(
            name = "DECISIONS_JSON"
    )
    private String decisionsJson;


    /*
     * 해야 할 일
     *
     * List<String>을 JSON 문자열로 저장
     */
    @Lob
    @Column(
            name = "ACTION_ITEMS_JSON"
    )
    private String actionItemsJson;


    /*
     * 중요 키워드
     *
     * List<String>을 JSON 문자열로 저장
     */
    @Lob
    @Column(
            name = "KEYWORDS_JSON"
    )
    private String keywordsJson;


    /*
     * AI 분석에 사용된
     * 채팅 메시지 개수
     */
    @Column(
            name = "MESSAGE_COUNT",
            nullable = false
    )
    private Integer messageCount;


    /*
     * AI 회의록 생성 시간
     */
    @CreationTimestamp
    @Column(
            name = "GENERATED_AT",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime generatedAt;
}