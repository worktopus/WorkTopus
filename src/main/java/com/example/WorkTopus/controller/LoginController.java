package com.example.WorkTopus.controller;

import com.example.WorkTopus.dto.UserCreateForm;
import com.example.WorkTopus.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;

    // 회원가입페이지 이동
    @GetMapping("/user/register")
    public String registerForm(Model model) {
        model.addAttribute("userForm", new UserCreateForm());
        return "userRegister";
    }

    // 회원가입
    @PostMapping("/user/register")
    public String registerUser(
            @Valid @ModelAttribute("userForm") UserCreateForm userForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) throws IllegalAccessException {

        // 입력에 오류가 있다면 다시 입력화면으로 돌아가기
        if(bindingResult.hasErrors()) {
            return "userRegister";
        }

        // 회원가입 : DB에 저장
        try {
            userService.register(userForm);
        } catch (IllegalArgumentException e){
            redirectAttributes.addFlashAttribute("msg",
                    "회원가입이 실패했습니다." + e.getMessage() );
            bindingResult.reject("가입실패", e.getMessage());
            return "userRegister";
        }

        redirectAttributes.addFlashAttribute("msg", "회원가입이 완료되었습니다. 로그인하세요");
        return "redirect:/login";
    }

}
