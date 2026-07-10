package com.example.WorkTopus.controller;

import com.example.WorkTopus.dto.UserUpdateForm;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication; // 💡 임포트 추가 확인
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 내 정보 보기
    @GetMapping("/user/mypage")
    public String myPage(Authentication authentication, Model model) {

        Users user = userService.findByUserId(authentication.getName());
        model.addAttribute("user", user);

        UserUpdateForm userForm = new UserUpdateForm();
        userForm.setName(user.getName());
        userForm.setEmail(user.getEmail());
        model.addAttribute("userForm", userForm);

        return "user/mypage";
    }

    // 내 정보 수정 처리
    @PostMapping("/user/mypage")
    public String updateMyPage(
            Authentication authentication,
            @Valid @ModelAttribute("userForm") UserUpdateForm userForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        String userId = authentication.getName();
        Users user = userService.findByUserId(userId);

        // 에러 발생 시 원래 유저 정보 조회 후 폼으로 복귀
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            return "user/mypage";
        }

        try {
            userService.updateName(userId, userForm.getName());
        } catch (IllegalArgumentException e) {
            bindingResult.reject("updateFail", e.getMessage());
            model.addAttribute("user", user);
            return "user/mypage";
        }

        // 리디렉션 시 메시지 전달
        redirectAttributes.addFlashAttribute("msg",
                "내 정보가 수정되었습니다");

        return "redirect:/user/mypage";
    }
}