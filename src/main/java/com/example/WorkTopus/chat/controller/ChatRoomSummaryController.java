package com.example.WorkTopus.chat.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import com.example.WorkTopus.chat.dto.ChatRoomSummary;
import com.example.WorkTopus.chat.service.ChatReadService;
import com.example.WorkTopus.chat.service.ChatRoomAccessService;
import com.example.WorkTopus.chat.service.ChatRoomSummaryService;
import com.example.WorkTopus.chat.service.ProjectAccessService;
import com.example.WorkTopus.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;


@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRoomSummaryController {

    /*
     * 로그인 사용자의 단체방과 개인방 목록을
     * 조회하는 서비스입니다.
     */
    private final ChatRoomSummaryService
            chatRoomSummaryService;


    /*
     * 채팅방의 읽음 정보를
     * DB에 저장하는 서비스입니다.
     */
    private final ChatReadService
            chatReadService;


    /*
     * 로그인 사용자 정보를 조회하는
     * 공통 권한 서비스입니다.
     */
    private final ProjectAccessService
            projectAccessService;


    /*
     * 단체방과 개인방 접근 권한을
     * 공통으로 검사하는 서비스입니다.
     */
    private final ChatRoomAccessService
            chatRoomAccessService;


    /*
     * =====================================================
     * 로그인 사용자의 전체 채팅방 목록 조회
     * =====================================================
     *
     * GET
     * /api/chat/rooms
     *
     * 반환 내용:
     *
     * - 참여 중인 프로젝트 단체방
     * - 프로젝트 팀원과의 개인방
     * - 마지막 메시지
     * - 안 읽은 메시지 수
     */
    @GetMapping("/rooms")
    public List<ChatRoomSummary> getRooms(
            Principal principal
    ) {

        /*
         * Spring Security의 Principal을 기준으로
         * 실제 로그인 사용자 정보를 조회합니다.
         *
         * 브라우저에서 전달한 userNum은 사용하지 않습니다.
         */
        Users loginUser =
                projectAccessService
                        .getLoginUser(
                                principal
                        );


        /*
         * 로그인 사용자가 참여 중인 프로젝트를 기준으로
         * 전체 단체방과 개인방 목록을 반환합니다.
         */
        return chatRoomSummaryService
                .getRoomSummaries(
                        loginUser
                );
    }


    /*
     * =====================================================
     * 채팅방 읽음 처리
     * =====================================================
     *
     * POST
     * /api/chat/rooms/{roomId}/read
     *
     * 단체방 예:
     *
     * POST
     * /api/chat/rooms/project_22_group/read
     *
     * 개인방 예:
     *
     * POST
     * /api/chat/rooms/project_22_private_3_8/read
     */
    @PostMapping("/rooms/{roomId}/read")
    public ResponseEntity<Void> markRoomAsRead(
            @PathVariable String roomId,
            Principal principal
    ) {

        /*
         * 현재 로그인 사용자가 실제로 해당 채팅방에
         * 접근할 수 있는지 검사합니다.
         *
         * 단체방:
         * - 프로젝트 참여자인지 검사
         *
         * 개인방:
         * - 프로젝트 참여자인지 검사
         * - 개인방 당사자인지 검사
         * - 두 사용자 모두 프로젝트 참여자인지 검사
         *
         * 검사를 통과하면 실제 로그인 Users를 반환합니다.
         */
        Users loginUser =
                chatRoomAccessService
                        .requireRoomAccess(
                                roomId,
                                principal
                        );


        /*
         * 채팅방 ID에 포함된 실제 프로젝트 번호를
         * 서버에서 추출합니다.
         *
         * project_22_group
         * → 22
         *
         * project_22_private_3_8
         * → 22
         */
        Long projectId =
                chatRoomAccessService
                        .extractProjectId(
                                roomId
                        );


        /*
         * 모든 권한 검사를 통과한 경우에만
         * 현재 채팅방의 마지막 메시지까지 읽은 것으로 저장합니다.
         *
         * 프런트에서 userNum을 전달받지 않고
         * 실제 로그인 사용자의 userNum을 사용합니다.
         */
        chatReadService.markRoomAsRead(
                projectId,
                roomId.trim(),
                loginUser.getUserNum()
        );


        /*
         * 읽음 처리 정상 완료
         */
        return ResponseEntity
                .noContent()
                .build();
    }
}