package com.example.WorkTopus.controller;

import com.example.WorkTopus.dto.Comment;
import com.example.WorkTopus.dto.Post;
import com.example.WorkTopus.dto.UserUpdateForm;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 1. 내 정보 조회/수정폼 이동
    @GetMapping("/mypage")
    public ModelAndView myPage(Authentication authentication) {
        ModelAndView mv = new ModelAndView();

        // 로그인된 실제 유저 ID 가져오기
        String userId = authentication.getName();

        // 서비스에서 실제 로그인한 유저 정보 조회
        Users user = userService.findByUserId(userId);

        // 정보 가져오기
        UserUpdateForm userForm = new UserUpdateForm();
        userForm.setName(user.getName());
        userForm.setEmail(user.getEmail());

        // 정보 담기
        mv.addObject("user", user);
        mv.addObject("userForm", userForm);

        mv.setViewName("user/mypage");
        return mv;
    }

    // 2. 내 정보 수정 처리
    @PostMapping("/mypage") // 💡 /user/mypage 로 매핑됩니다.
    public ModelAndView updateMyPage(
            Authentication authentication,
            @Valid @ModelAttribute("userForm") UserUpdateForm userForm,
            BindingResult bindingResult,
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture,
            RedirectAttributes redirectAttributes) {

        ModelAndView mv = new ModelAndView();
        String userId = authentication.getName();

        if (userForm.getPassword() == null || userForm.getPassword().trim().isEmpty()) {
            bindingResult.rejectValue("password", null);
        }

        // 유효성 검증 실패시 다시 폼 화면으로 돌려보내기
        if (bindingResult.hasErrors() && bindingResult.hasFieldErrors("name")) {
            Users user = userService.findByUserId(userId);
            mv.addObject("user", user);
            mv.setViewName("user/mypage");
            return mv;
        }

        try {
            // 이름 변경
            userService.updateName(userId, userForm.getName());

            // 비밀번호 변경
            if (userForm.getPassword() != null && !userForm.getPassword().trim().isEmpty()) {
                userService.updatePasswordWithoutCurrent(userId, userForm.getPassword());
            }

            // 프사 변경
            if (profilePicture != null && !profilePicture.isEmpty()) {
                String originalFilename = profilePicture.getOriginalFilename();
                String saveFileName = System.currentTimeMillis() + "_" + originalFilename;

                // DB에 웹 접근
                String savedPath = "/images/" + saveFileName;

                String rootPath = System.getProperty("user.dir");

                // 경로를 각각 정의
                String srcPath = rootPath + "/src/main/resources/static/images/";
                String buildPath = rootPath + "/build/resources/main/static/images/";

                // 폴더 자동 생성
                java.io.File srcFolder = new java.io.File(srcPath);
                if (!srcFolder.exists()) srcFolder.mkdirs();

                java.io.File buildFolder = new java.io.File(buildPath);
                if (!buildFolder.exists()) buildFolder.mkdirs();

                // 3. 파일 저장
                profilePicture.transferTo(new java.io.File(srcPath + saveFileName));

                java.io.File srcFile = new java.io.File(srcPath + saveFileName);
                java.io.File buildFile = new java.io.File(buildPath + saveFileName);
                java.nio.file.Files.copy(srcFile.toPath(), buildFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                userService.updatePicture(userId, savedPath);
            }
        } catch (Exception e) {
            bindingResult.reject("updateFail", e.getMessage());
            Users user = userService.findByUserId(userId);
            mv.addObject("user", user);
            mv.setViewName("user/mypage");
            return mv;
        }

        // 성공 알림
        redirectAttributes.addFlashAttribute("msg", "내 정보가 수정되었습니다");

        mv.setViewName("redirect:/user/mypage");
        return mv;
    }

    // 회원탈퇴 처리
    @PostMapping("/delete")
    public String deleteUser(
            Authentication authentication,
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            RedirectAttributes redirectAttributes) {

        if (authentication != null) {
            String userId = authentication.getName();

            try {
                userService.deleteUser(userId);

                new org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler()
                        .logout(request, response, authentication);

                redirectAttributes.addFlashAttribute("msg", "회원탈퇴가 성공적으로 처리되었습니다.");
                return "redirect:/";

            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("msg", "회원탈퇴 중 오류가 발생했습니다: " + e.getMessage());
                return "redirect:/user/mypage";
            }
        }
        return "redirect:/";
    }

// ------------------------------------------------------------------------------------------
    // 내가 쓴 글 목록 조회 (비동기 JSON 응답)
    @GetMapping("/mypage/post")
    @ResponseBody
    public java.util.List<Post> getMyPosts(Authentication authentication) {

        // 스프링 시큐리티 인증 객체에서 계정 ID(예: admin) 추출
        String userId = authentication.getName();

        // 유저 엔티티에서 데이터베이스에 매핑된 실제 실명(예: 김여린, 관리자) 조회
        Users user = userService.findByUserId(userId);
        String writerName = user.getName();

        // 실제 이름으로 작성된 글 리스트를 PostDto 형태로 받아서 반환
        return userService.findPostsByWriterName(writerName);
    }

    // ------------------------------------------------------------------------------------------
    // 내가 쓴 댓글 목록 조회
    @GetMapping("/mypage/comments")
    @ResponseBody
    public ResponseEntity<List<Comment>> getMyComments(Authentication authentication) {
        String userId = authentication.getName();
        Users user = userService.findByUserId(userId);

        List<com.example.WorkTopus.dto.Comment> comments = userService.getMyComments(user.getUserNum());
        return ResponseEntity.ok(comments);
    }
}