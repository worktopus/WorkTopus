package com.example.WorkTopus.controller;

import com.example.WorkTopus.dto.UserUpdateForm;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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

    // 내 정보
    @GetMapping("/user/me")
    public String myPage(Authentication authentication, Model model) {

        Users user = userService.findByUserName(authentication.getName());
        model.addAttribute("user", user);
        model.addAttribute("userForm", userService.toUpdateForm(user));

        return "userMyPage";
    }

    // 내 정보 수정
    @PostMapping("/user/me")
    public String updateMyPage(
            Authentication authentication,
            @Valid @ModelAttribute("userForm") UserUpdateForm userForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        Users user = userService.findByUserName(authentication.getName());

        if (bindingResult.hasErrors()){
            model.addAttribute("user", user);
            return "userMyPage";
        }

        try {
            userService.update(user.getUserNum(), userForm, false);
        } catch (IllegalArgumentException e) {
            bindingResult.reject("updateFail", e.getMessage());
            model.addAttribute("user", user);
            return "userMyPage";
        }

        // 반드시 redirect 할때만 사용가능하다. redirect:/login
        redirectAttributes.addFlashAttribute("msg",
                "재 정보가 수정되었습니다");

        return "redirect:/user/me";
    }

}
