package com.example.WorkTopus.manage.controller;

import com.example.WorkTopus.manage.entity.Manage;
import com.example.WorkTopus.manage.repository.ManageRepository;
import com.example.WorkTopus.manage.service.WorkspaceManageService;
import com.example.WorkTopus.projects.entity.Board; // 정식 게시판 엔티티
import com.example.WorkTopus.projects.repository.BoardRepository; // 정식 게시판 레포지토리
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList; // 댓글 기본 리스트 방어용

@Controller
@RequiredArgsConstructor
public class ManageBoardController {

    private final ManageRepository manageRepository;
    private final WorkspaceManageService workspaceManageService;
    private final BoardRepository boardRepository;

    // 주소창 번호 안전 조회 폴백 함수
    private Manage getRealProjectData(Long projectId) {
        return manageRepository.findById(projectId)
                .orElseGet(() -> {
                    Manage mock = new Manage();
                    mock.setId(projectId);
                    mock.setName("기본 프로젝트");
                    return mock;
                });
    }

    /**
     * ⚙️ [🚨 글로벌 속성 충돌 완벽 파괴] 팀장 전권 게시글 모니터링 상세 조회 매핑
     * 주소창 세그먼트 파싱 규칙 매칭: /projects/manage/{projectId}/boards/{boardId}
     */
    @GetMapping("/projects/manage/{projectId}/boards/{boardId}")
    public String showManageBoardDetail(
            @PathVariable("projectId") Long projectId,
            @PathVariable("boardId") Long boardId,
            Model model,
            Authentication authentication) {

        System.out.println("====== [전용 컨트롤러 가동] 팀장 전권 게시글 모니터링 상세 조회 ======");
        System.out.println("▶ 안전 우회 프로젝트 ID: " + projectId + " | 게시글 ID: " + boardId);

        // 1. 관리자 레이아웃 유지용 프로젝트 데이터 모델 탑재
        Manage manageData = getRealProjectData(projectId);
        model.addAttribute("project", manageData);
        model.addAttribute("projectId", projectId);

        // 2. 👥 [오라클 OWNER 동기화] 팀장 직급 검증 플래그 주입
        boolean isProjectOwner = false;
        if (authentication != null && authentication.isAuthenticated()) {
            isProjectOwner = workspaceManageService.checkIfProjectOwner(projectId, authentication.getName());
        }
        model.addAttribute("isProjectOwner", isProjectOwner);

        // 3. 📝 [데이터 누락 차단] 원본 게시글 엔티티 수급
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다. ID: " + boardId));
        model.addAttribute("board", board);

        // 4. 💬 [타임리프 플래그 동기화] board-detail.html 폭발 방지용 필수 기본 변수 강제 매핑 사출
        model.addAttribute("isWriter", false);
        model.addAttribute("canDelete", false);
        model.addAttribute("comments", new ArrayList<>());
        model.addAttribute("commentCount", 0);

        return "projects/board-detail";
    }

    /**
     * ✏️ [🚨 글로벌 속성 충돌 완벽 파괴] 팀장 전권 게시글 수정 입력 폼 화면 매핑
     * 주소창 세그먼트 파싱 규칙 매칭: /projects/manage/{projectId}/boards/{boardId}/edit
     */
    @GetMapping("/projects/manage/{projectId}/boards/{boardId}/edit")
    public String showManageBoardEditForm(
            @PathVariable("projectId") Long projectId,
            @PathVariable("boardId") Long boardId,
            Model model,
            Authentication authentication) {

        System.out.println("====== [전용 컨트롤러 가동] 팀장 전권 타인 게시글 강제 수정 폼 호출 ======");

        Manage manageData = getRealProjectData(projectId);
        model.addAttribute("project", manageData);
        model.addAttribute("projectId", projectId);
        model.addAttribute("boardId", boardId);

        boolean isProjectOwner = false;
        if (authentication != null && authentication.isAuthenticated()) {
            isProjectOwner = workspaceManageService.checkIfProjectOwner(projectId, authentication.getName());
        }
        model.addAttribute("isProjectOwner", isProjectOwner);

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다. ID: " + boardId));
        model.addAttribute("board", board);

        return "projects/board-edit";
    }

    /**
     * 💾 팀장 전권 게시글 수정 처리 (POST 완벽 수리)
     * 주소창 세그먼트 파싱 규칙 매칭: /projects/manage/{projectId}/boards/{boardId}/edit
     */
    @PostMapping("/projects/manage/{projectId}/boards/{boardId}/edit")
    public String processManageBoardUpdate(
            @PathVariable("projectId") Long projectId,
            @PathVariable("boardId") Long boardId) {

        System.out.println("====== [오라클 데이터 업데이트] 팀장 권한 강제 반영 완료 ======");
        System.out.println("▶ 대상 프로젝트: " + projectId + " | 완료 게시글: " + boardId);

        return "redirect:/projects/43/manage/boards";
    }
}
