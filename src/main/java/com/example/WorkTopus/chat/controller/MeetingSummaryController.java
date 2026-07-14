package com.example.WorkTopus.chat.controller;

import com.example.WorkTopus.chat.dto.MeetingSummaryResponse;
import com.example.WorkTopus.chat.service.MeetingSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class MeetingSummaryController {

    private final MeetingSummaryService
            meetingSummaryService;


    /*
     * 프로젝트 단체채팅 AI 회의요약
     *
     * 예:
     *
     * POST
     * /api/ai/meeting-summary/2
     *
     * projectId = 2
     *
     * 요약 대상:
     * project_2_group
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
}