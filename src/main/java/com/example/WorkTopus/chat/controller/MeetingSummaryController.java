package com.example.WorkTopus.chat.controller;

import com.example.WorkTopus.chat.dto.MeetingSummaryListItem;
import com.example.WorkTopus.chat.dto.MeetingSummaryResponse;
import com.example.WorkTopus.chat.dto.MeetingSummarySaveResponse;
import com.example.WorkTopus.chat.service.MeetingSummaryService;
import com.example.WorkTopus.chat.service.MeetingSummaryStorageService;
import com.example.WorkTopus.chat.service.ProjectAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class MeetingSummaryController {

    private final MeetingSummaryService
            meetingSummaryService;

    private final MeetingSummaryStorageService
            meetingSummaryStorageService;

    private final ProjectAccessService
            projectAccessService;


    /*
     * 프로젝트 단체채팅 AI 회의요약 생성
     *
     * POST /api/ai/meeting-summary/{projectId}
     */
    @PostMapping("/meeting-summary/{projectId}")
    public ResponseEntity<MeetingSummaryResponse>
    summarizeMeeting(
            @PathVariable Long projectId,
            Principal principal
    ) {
        projectAccessService
                .requireProjectMember(
                        projectId,
                        principal
                );

        MeetingSummaryResponse response =
                meetingSummaryService
                        .summarizeProjectGroup(
                                projectId
                        );

        return ResponseEntity.ok(response);
    }


    /*
     * AI 회의요약 저장
     *
     * POST /api/ai/meeting-summary/save
     */
    @PostMapping("/meeting-summary/save")
    public ResponseEntity<MeetingSummarySaveResponse>
    saveMeetingSummary(
            @RequestBody MeetingSummaryResponse summary,
            Principal principal
    ) {
        if (summary == null) {
            throw new IllegalArgumentException(
                    "저장할 AI 회의요약 정보가 없습니다."
            );
        }

        /*
         * 사용자가 요청 본문의 projectId를
         * 임의로 바꾸는 것을 방지합니다.
         */
        projectAccessService
                .requireProjectMember(
                        summary.getProjectId(),
                        principal
                );

        /*
         * projectId와 roomId가 같은
         * 프로젝트인지 검사합니다.
         */
        validateSummaryRoomId(summary);

        MeetingSummarySaveResponse response =
                meetingSummaryStorageService
                        .save(summary);

        return ResponseEntity.ok(response);
    }


    /*
     * 프로젝트별 AI 회의록 목록
     *
     * GET /api/ai/meeting-summary/project/{projectId}
     */
    @GetMapping(
            "/meeting-summary/projects/{projectId}"
    )
    public ResponseEntity<List<MeetingSummaryListItem>>
    getMeetingSummaryList(
            @PathVariable Long projectId,
            Principal principal
    ) {
        projectAccessService
                .requireProjectMember(
                        projectId,
                        principal
                );

        List<MeetingSummaryListItem> summaries =
                meetingSummaryStorageService
                        .getProjectSummaries(
                                projectId
                        );

        return ResponseEntity.ok(summaries);
    }


    /*
     * AI 회의록 상세
     *
     * GET /api/ai/meeting-summary/detail/{summaryId}
     */
    @GetMapping(
            "/meeting-summary/detail/{summaryId}"
    )
    public ResponseEntity<MeetingSummaryResponse>
    getMeetingSummaryDetail(
            @PathVariable Long summaryId,
            Principal principal
    ) {
        MeetingSummaryResponse summary =
                meetingSummaryStorageService
                        .getSummaryDetail(
                                summaryId
                        );

        /*
         * 회의록에 저장된 실제 projectId를 기준으로
         * 프로젝트 참여 여부를 검사합니다.
         */
        projectAccessService
                .requireProjectMember(
                        summary.getProjectId(),
                        principal
                );

        return ResponseEntity.ok(summary);
    }


    /*
     * projectId와 roomId 일치 검사
     *
     * 예:
     * projectId = 21
     * roomId = project_21_group
     */
    private void validateSummaryRoomId(
            MeetingSummaryResponse summary
    ) {
        Long projectId =
                summary.getProjectId();

        String expectedRoomId =
                "project_"
                        + projectId
                        + "_group";

        if (
                summary.getRoomId() == null ||
                        !expectedRoomId.equals(
                                summary.getRoomId()
                        )
        ) {
            throw new IllegalArgumentException(
                    "프로젝트와 채팅방 정보가 일치하지 않습니다."
            );
        }
    }
}