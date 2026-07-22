package com.example.WorkTopus.chat.controller;

import org.springframework.security.access.AccessDeniedException;
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

    /*
     * 프로젝트 단체채팅 내용을 조회하고
     * Gemini를 이용해 회의요약을 생성하는 서비스입니다.
     */
    private final MeetingSummaryService
            meetingSummaryService;


    /*
     * 생성된 AI 회의요약을 DB에 저장하고,
     * 저장된 회의록 목록과 상세 내용을 조회하는 서비스입니다.
     */
    private final MeetingSummaryStorageService
            meetingSummaryStorageService;


    /*
     * 로그인 사용자 조회와
     * 프로젝트 참여자 권한 검사를 공통으로 처리합니다.
     */
    private final ProjectAccessService
            projectAccessService;

    /*
     * =====================================================
     * 프로젝트 단체채팅 AI 회의요약 생성
     * =====================================================
     *
     * POST
     * /api/ai/meeting-summary/{projectId}
     *
     * 예:
     *
     * POST
     * /api/ai/meeting-summary/22
     */
    @PostMapping(
            "/meeting-summary/{projectId}"
    )
    public ResponseEntity<MeetingSummaryResponse>
    summarizeMeeting(
            @PathVariable Long projectId,
            Principal principal
    ) {

        /*
         * AI가 채팅 내용을 조회하기 전에
         * 현재 로그인 사용자가 해당 프로젝트 참여자인지 확인합니다.
         *
         * 비참여자는 프로젝트 단체채팅을
         * AI 요약에 사용할 수 없습니다.
         */
        projectAccessService
                .requireProjectMember(
                        projectId,
                        principal
                );


        /*
         * 해당 프로젝트의 단체 채팅방:
         *
         * project_{projectId}_group
         *
         * 메시지를 조회한 뒤 AI 회의요약을 생성합니다.
         */
        MeetingSummaryResponse response =
                meetingSummaryService
                        .summarizeProjectGroup(
                                projectId
                        );


        return ResponseEntity.ok(
                response
        );
    }


    /*
     * =====================================================
     * AI 회의요약 저장
     * =====================================================
     *
     * POST
     * /api/ai/meeting-summary/save
     */
    @PostMapping(
            "/meeting-summary/save"
    )
    public ResponseEntity<MeetingSummarySaveResponse>
    saveMeetingSummary(
            @RequestBody MeetingSummaryResponse summary,
            Principal principal
    ) {

        /*
         * 저장 요청 데이터가 존재하는지 확인합니다.
         */
        if (summary == null) {
            throw new IllegalArgumentException(
                    "저장할 AI 회의요약 정보가 없습니다."
            );
        }


        /*
         * 요청 본문에 projectId가 존재하는지 확인합니다.
         */
        if (
                summary.getProjectId() == null
                        || summary.getProjectId() <= 0
        ) {
            throw new IllegalArgumentException(
                    "올바른 프로젝트 번호가 필요합니다."
            );
        }


        /*
         * 브라우저에서 요청 본문의 projectId를
         * 다른 프로젝트 번호로 조작할 수 있으므로,
         * 해당 프로젝트의 실제 참여자인지 검사합니다.
         */
        projectAccessService
                .requireProjectMember(
                        summary.getProjectId(),
                        principal
                );


        /*
         * projectId와 roomId가 같은 프로젝트인지 검사합니다.
         *
         * 예:
         *
         * projectId = 22
         * roomId = project_22_group
         */
        validateSummaryRoomId(
                summary
        );


        /*
         * 프로젝트 참여자 검사와
         * 채팅방 번호 검사를 모두 통과한 경우에만
         * AI_MEETING_SUMMARY 테이블에 저장합니다.
         */
        MeetingSummarySaveResponse response =
                meetingSummaryStorageService
                        .save(
                                summary
                        );


        return ResponseEntity.ok(
                response
        );
    }


    /*
     * =====================================================
     * 프로젝트별 AI 회의록 목록 조회
     * =====================================================
     *
     * GET
     * /api/ai/meeting-summary/project/{projectId}
     *
     * 예:
     *
     * GET
     * /api/ai/meeting-summary/project/22
     */
    @GetMapping(
            "/meeting-summary/project/{projectId}"
    )
    public ResponseEntity<List<MeetingSummaryListItem>>
    getMeetingSummaryList(
            @PathVariable Long projectId,
            Principal principal
    ) {

        /*
         * 다른 프로젝트 번호를 직접 입력해
         * 저장된 회의록 목록을 조회하지 못하도록
         * 프로젝트 참여 여부를 검사합니다.
         */
        projectAccessService
                .requireProjectMember(
                        projectId,
                        principal
                );


        /*
         * 프로젝트 참여자인 경우에만
         * 해당 프로젝트의 저장된 회의록을 조회합니다.
         */
        List<MeetingSummaryListItem> summaries =
                meetingSummaryStorageService
                        .getProjectSummaries(
                                projectId
                        );


        return ResponseEntity.ok(
                summaries
        );
    }


    /*
     * =====================================================
     * 저장된 AI 회의록 상세 조회
     * =====================================================
     *
     * GET
     * /api/ai/meeting-summary/detail/{summaryId}
     *
     * 예:
     *
     * GET
     * /api/ai/meeting-summary/detail/15
     */
    @GetMapping(
            "/meeting-summary/detail/{summaryId}"
    )
    public ResponseEntity<MeetingSummaryResponse>
    getMeetingSummaryDetail(
            @PathVariable Long summaryId,
            Principal principal
    ) {

        /*
         * 올바른 회의록 PK인지 확인합니다.
         */
        if (
                summaryId == null
                        || summaryId <= 0
        ) {
            throw new IllegalArgumentException(
                    "올바른 회의록 번호가 필요합니다."
            );
        }


        /*
         * summaryId만 보고 사용자의 projectId를 신뢰하지 않습니다.
         *
         * 먼저 DB에 저장된 실제 회의록을 조회하여
         * 그 회의록이 어느 프로젝트에 속하는지 확인합니다.
         */
        MeetingSummaryResponse summary =
                meetingSummaryStorageService
                        .getSummaryDetail(
                                summaryId
                        );


        /*
         * DB에서 조회된 실제 projectId를 기준으로
         * 현재 사용자의 프로젝트 참여 여부를 검사합니다.
         *
         * 따라서 다른 프로젝트의 summaryId를
         * 직접 입력해도 상세 내용을 받을 수 없습니다.
         */
        projectAccessService
                .requireProjectMember(
                        summary.getProjectId(),
                        principal
                );


        return ResponseEntity.ok(
                summary
        );
    }

    /*
     * =====================================================
     * 게시글 작성용 저장 회의록 상세 조회
     * =====================================================
     *
     * GET
     * /api/ai/meeting-summary/project/{projectId}/detail/{summaryId}
     *
     * 현재 게시판의 projectId와
     * 저장된 회의록의 실제 projectId가 같은지 검사합니다.
     */
    @GetMapping(
            "/meeting-summary/project/{projectId}/detail/{summaryId}"
    )
    public ResponseEntity<MeetingSummaryResponse>
    getMeetingSummaryForBoardWrite(
            @PathVariable Long projectId,
            @PathVariable Long summaryId,
            Principal principal
    ) {

        /*
         * 올바른 프로젝트 번호인지 확인합니다.
         */
        if (
                projectId == null
                        || projectId <= 0
        ) {
            throw new IllegalArgumentException(
                    "올바른 프로젝트 번호가 필요합니다."
            );
        }


        /*
         * 올바른 회의록 번호인지 확인합니다.
         */
        if (
                summaryId == null
                        || summaryId <= 0
        ) {
            throw new IllegalArgumentException(
                    "올바른 회의록 번호가 필요합니다."
            );
        }


        /*
         * 현재 로그인 사용자가
         * 게시글을 작성하려는 프로젝트의 참여자인지 검사합니다.
         */
        projectAccessService
                .requireProjectMember(
                        projectId,
                        principal
                );


        /*
         * summaryId에 해당하는 저장 회의록을
         * DB에서 조회합니다.
         */
        MeetingSummaryResponse summary =
                meetingSummaryStorageService
                        .getSummaryDetail(
                                summaryId
                        );


        /*
         * URL의 프로젝트와
         * 회의록이 실제로 저장된 프로젝트가 같은지 검사합니다.
         *
         * 예:
         *
         * 게시글 작성 프로젝트: 22
         * 회의록 실제 프로젝트: 21
         *
         * 위와 같은 경우 접근을 차단합니다.
         */
        if (
                summary.getProjectId() == null
                        || !projectId.equals(
                        summary.getProjectId()
                )
        ) {
            throw new AccessDeniedException(
                    "현재 프로젝트의 회의록이 아닙니다."
            );
        }


        return ResponseEntity.ok(
                summary
        );
    }

    /*
     * =====================================================
     * projectId와 roomId 일치 검사
     * =====================================================
     *
     * AI 회의요약은 프로젝트 단체방만 저장할 수 있습니다.
     *
     * 예:
     *
     * projectId = 21
     * roomId = project_21_group
     *
     * 위 두 값이 정확히 일치해야 합니다.
     */
    private void validateSummaryRoomId(
            MeetingSummaryResponse summary
    ) {

        Long projectId =
                summary.getProjectId();


        /*
         * 현재 프로젝트의 정상 단체방 번호를 생성합니다.
         */
        String expectedRoomId =
                "project_"
                        + projectId
                        + "_group";


        /*
         * 다음과 같은 요청은 차단됩니다.
         *
         * projectId = 22
         * roomId = project_23_group
         *
         * projectId = 22
         * roomId = project_22_private_3_8
         */
        if (
                summary.getRoomId() == null
                        || !expectedRoomId.equals(
                        summary.getRoomId().trim()
                )
        ) {
            throw new IllegalArgumentException(
                    "프로젝트와 채팅방 정보가 일치하지 않습니다."
            );
        }
    }
}