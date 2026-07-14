package com.example.WorkTopus.chat.controller;

import com.example.WorkTopus.chat.dto.MeetingSummaryResponse;
import com.example.WorkTopus.chat.dto.MeetingSummarySaveResponse;
import com.example.WorkTopus.chat.service.MeetingSummaryService;
import com.example.WorkTopus.chat.service.MeetingSummaryStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.WorkTopus.chat.dto.MeetingSummaryListItem;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class MeetingSummaryController {

    private final MeetingSummaryService
            meetingSummaryService;


    private final MeetingSummaryStorageService
            meetingSummaryStorageService;


    /*
     * 프로젝트 단체채팅
     * Gemini AI 회의요약 생성
     *
     * POST
     * /api/ai/meeting-summary/2
     */
    @PostMapping(
            "/meeting-summary/{projectId}"
    )
    public ResponseEntity<MeetingSummaryResponse>
    summarizeMeeting(
            @PathVariable Long projectId
    ) {

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
     * 생성된 AI 회의요약 DB 저장
     *
     * POST
     * /api/ai/meeting-summary/save
     */
    @PostMapping(
            "/meeting-summary/save"
    )
    public ResponseEntity<MeetingSummarySaveResponse>
    saveMeetingSummary(
            @RequestBody
            MeetingSummaryResponse summary
    ) {

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
     * 특정 프로젝트의
     * 저장된 AI 회의록 목록 조회
     *
     * GET
     * /api/ai/meeting-summary/project/2
     */
    @GetMapping(
            "/meeting-summary/project/{projectId}"
    )
    public ResponseEntity<List<MeetingSummaryListItem>>
    getMeetingSummaryList(
            @PathVariable Long projectId
    ) {

        List<MeetingSummaryListItem> summaries =
                meetingSummaryStorageService
                        .getProjectSummaries(
                                projectId
                        );


        return ResponseEntity.ok(
                summaries
        );
    }

}