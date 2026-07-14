package com.example.WorkTopus.chat.service;

import com.example.WorkTopus.chat.dto.MeetingSummaryListItem;
import com.example.WorkTopus.chat.dto.MeetingSummaryResponse;
import com.example.WorkTopus.chat.dto.MeetingSummarySaveResponse;
import com.example.WorkTopus.chat.entity.MeetingSummaryEntity;
import com.example.WorkTopus.chat.repository.MeetingSummaryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingSummaryStorageService {

    private final MeetingSummaryJpaRepository
            meetingSummaryJpaRepository;


    private final ObjectMapper objectMapper =
            new ObjectMapper();


    /*
     * =====================================================
     * AI 회의록 저장
     * =====================================================
     */
    @Transactional
    public MeetingSummarySaveResponse save(
            MeetingSummaryResponse summary
    ) {

        /*
         * 저장할 데이터 검증
         */
        validateSummary(
                summary
        );


        /*
         * List<String>
         * →
         * JSON 문자열
         */
        String decisionsJson =
                toJson(
                        summary.getDecisions()
                );


        String actionItemsJson =
                toJson(
                        summary.getActionItems()
                );


        String keywordsJson =
                toJson(
                        summary.getKeywords()
                );


        /*
         * DB Entity 생성
         */
        MeetingSummaryEntity entity =
                MeetingSummaryEntity
                        .builder()
                        .projectId(
                                summary.getProjectId()
                        )
                        .roomId(
                                summary.getRoomId()
                        )
                        .summaryContent(
                                summary.getSummary()
                                        .trim()
                        )
                        .decisionsJson(
                                decisionsJson
                        )
                        .actionItemsJson(
                                actionItemsJson
                        )
                        .keywordsJson(
                                keywordsJson
                        )
                        .messageCount(
                                summary.getMessageCount()
                        )
                        .build();


        /*
         * AI_MEETING_SUMMARY 저장
         */
        MeetingSummaryEntity saved =
                meetingSummaryJpaRepository
                        .save(
                                entity
                        );


        /*
         * 저장 결과 반환
         */
        return MeetingSummarySaveResponse
                .builder()
                .summaryId(
                        saved.getSummaryId()
                )
                .projectId(
                        saved.getProjectId()
                )
                .roomId(
                        saved.getRoomId()
                )
                .savedAt(
                        saved.getGeneratedAt()
                )
                .message(
                        "AI 회의록이 저장되었습니다."
                )
                .build();
    }


    /*
     * =====================================================
     * 프로젝트별 AI 회의록 목록 조회
     * =====================================================
     */
    public List<MeetingSummaryListItem>
    getProjectSummaries(
            Long projectId
    ) {

        validateProjectId(
                projectId
        );


        return meetingSummaryJpaRepository
                .findByProjectIdOrderByGeneratedAtDesc(
                        projectId
                )
                .stream()
                .map(
                        this::toListItem
                )
                .toList();
    }


    /*
     * =====================================================
     * 저장된 AI 회의록 상세 조회
     * =====================================================
     */
    public MeetingSummaryResponse
    getSummaryDetail(
            Long summaryId
    ) {

        /*
         * 회의록 PK 검증
         */
        validateSummaryId(
                summaryId
        );


        /*
         * SUMMARY_ID 기준 DB 조회
         */
        MeetingSummaryEntity entity =
                meetingSummaryJpaRepository
                        .findById(
                                summaryId
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "해당 AI 회의록을 찾을 수 없습니다."
                                        )
                        );


        /*
         * JSON 문자열
         * →
         * List<String>
         */
        List<String> decisions =
                fromJson(
                        entity.getDecisionsJson()
                );


        List<String> actionItems =
                fromJson(
                        entity.getActionItemsJson()
                );


        List<String> keywords =
                fromJson(
                        entity.getKeywordsJson()
                );


        /*
         * 기존 AI 회의요약 팝업에서 사용하는
         * MeetingSummaryResponse 형태로 반환
         */
        return MeetingSummaryResponse
                .builder()
                .projectId(
                        entity.getProjectId()
                )
                .roomId(
                        entity.getRoomId()
                )
                .summary(
                        entity.getSummaryContent()
                )
                .decisions(
                        decisions
                )
                .actionItems(
                        actionItems
                )
                .keywords(
                        keywords
                )
                .messageCount(
                        entity.getMessageCount()
                )
                .generatedAt(
                        entity.getGeneratedAt()
                )
                .build();
    }


    /*
     * =====================================================
     * Entity
     * →
     * 회의록 목록 DTO
     * =====================================================
     */
    private MeetingSummaryListItem toListItem(
            MeetingSummaryEntity entity
    ) {

        return MeetingSummaryListItem
                .builder()
                .summaryId(
                        entity.getSummaryId()
                )
                .projectId(
                        entity.getProjectId()
                )
                .roomId(
                        entity.getRoomId()
                )
                .summary(
                        entity.getSummaryContent()
                )
                .messageCount(
                        entity.getMessageCount()
                )
                .generatedAt(
                        entity.getGeneratedAt()
                )
                .build();
    }


    /*
     * =====================================================
     * List<String>
     * →
     * JSON 문자열
     * =====================================================
     */
    private String toJson(
            List<String> values
    ) {

        try {

            List<String> safeValues =
                    values == null
                            ? Collections.emptyList()
                            : values;


            return objectMapper
                    .writeValueAsString(
                            safeValues
                    );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "회의요약 데이터를 JSON으로 변환하지 못했습니다.",
                    exception
            );
        }
    }


    /*
     * =====================================================
     * JSON 문자열
     * →
     * List<String>
     * =====================================================
     */
    private List<String> fromJson(
            String json
    ) {

        /*
         * DB 값이 없으면
         * 빈 목록 반환
         */
        if (
                json == null
                        ||
                        json.isBlank()
        ) {

            return Collections.emptyList();
        }


        try {

            var root =
                    objectMapper
                            .readTree(
                                    json
                            );


            /*
             * JSON 배열이 아니면
             * 빈 목록 반환
             */
            if (
                    root == null
                            ||
                            !root.isArray()
            ) {

                return Collections.emptyList();
            }


            List<String> result =
                    new ArrayList<>();


            for (
                    var item :
                    root
            ) {

                if (
                        item == null
                                ||
                                item.isNull()
                ) {

                    continue;
                }


                String value =
                        item
                                .asText()
                                .trim();


                if (
                        !value.isBlank()
                ) {

                    result.add(
                            value
                    );
                }
            }


            return result;

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "저장된 회의록 JSON 데이터를 읽지 못했습니다.",
                    exception
            );
        }
    }


    /*
     * =====================================================
     * 프로젝트 ID 검증
     * =====================================================
     */
    private void validateProjectId(
            Long projectId
    ) {

        if (
                projectId == null
                        ||
                        projectId <= 0
        ) {

            throw new IllegalArgumentException(
                    "올바른 projectId가 필요합니다."
            );
        }
    }


    /*
     * =====================================================
     * 회의록 PK 검증
     * =====================================================
     */
    private void validateSummaryId(
            Long summaryId
    ) {

        if (
                summaryId == null
                        ||
                        summaryId <= 0
        ) {

            throw new IllegalArgumentException(
                    "올바른 summaryId가 필요합니다."
            );
        }
    }


    /*
     * =====================================================
     * 저장할 AI 회의요약 데이터 검증
     * =====================================================
     */
    private void validateSummary(
            MeetingSummaryResponse summary
    ) {

        /*
         * 회의요약 자체가 없는 경우
         */
        if (
                summary == null
        ) {

            throw new IllegalArgumentException(
                    "저장할 AI 회의요약 정보가 없습니다."
            );
        }


        /*
         * 프로젝트 ID 검증
         */
        if (
                summary.getProjectId() == null
                        ||
                        summary.getProjectId() <= 0
        ) {

            throw new IllegalArgumentException(
                    "올바른 projectId가 필요합니다."
            );
        }


        /*
         * 채팅방 ID 검증
         */
        if (
                summary.getRoomId() == null
                        ||
                        summary.getRoomId().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "채팅방 정보가 없습니다."
            );
        }


        /*
         * 현재는 프로젝트 단체채팅의
         * AI 회의록만 저장
         */
        String expectedRoomId =
                "project_"
                        + summary.getProjectId()
                        + "_group";


        if (
                !expectedRoomId.equals(
                        summary.getRoomId()
                )
        ) {

            throw new IllegalArgumentException(
                    "프로젝트 단체채팅의 AI 회의요약만 저장할 수 있습니다."
            );
        }


        /*
         * AI 전체 요약 내용 검증
         */
        if (
                summary.getSummary() == null
                        ||
                        summary.getSummary().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "저장할 회의요약 내용이 없습니다."
            );
        }


        /*
         * 분석 메시지 개수 검증
         */
        if (
                summary.getMessageCount() < 0
        ) {

            throw new IllegalArgumentException(
                    "메시지 개수가 올바르지 않습니다."
            );
        }
    }
}