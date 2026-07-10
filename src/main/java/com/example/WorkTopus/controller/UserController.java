package com.example.WorkTopus.controller;

import com.example.WorkTopus.dto.UserUpdateForm;
import com.example.WorkTopus.entity.Users;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
public class UserController {

    // 1. 내 정보 조회/수정폼 이동
    @GetMapping("/mypage")
    public ModelAndView myPage(Authentication authentication) {
        ModelAndView mv = new ModelAndView();

        // [임시] 서비스를 쓰지 않으므로 가짜 데이터를 생성해서 HTML에 넘겨줍니다.
        Users dummyUser = new Users();
        dummyUser.setUserId(authentication != null ? authentication.getName() : "tester");
        dummyUser.setName("홍길동");
        dummyUser.setEmail("hong@example.com");

        UserUpdateForm userForm = new UserUpdateForm();
        userForm.setName(dummyUser.getName());
        userForm.setEmail(dummyUser.getEmail());

        // ModelAndView에 데이터 바인딩 (model.addAttribute 역할)
        mv.addObject("user", dummyUser);
        mv.addObject("userForm", userForm);

        // 뷰 네임 설정 (Thymeleaf 파일 경로)
        mv.setViewName("user/mypage");
        return mv;
    }

    // 2. 내 정보 수정 처리
    @PostMapping("/mypage") // 💡 /user/mypage 로 매핑됩니다.
    public ModelAndView updateMyPage(
            Authentication authentication,
            @Valid @ModelAttribute("userForm") UserUpdateForm userForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        ModelAndView mv = new ModelAndView();

        // 유효성 검증 실패(에러) 시 다시 폼 화면으로 돌려보내기
        if (bindingResult.hasErrors()) {
            // 화면을 다시 그리기 위해 가짜 유저 데이터 바인딩
            Users dummyUser = new Users();
            dummyUser.setUserId(authentication != null ? authentication.getName() : "tester");

            mv.addObject("user", dummyUser);
            mv.setViewName("user/mypage");
            return mv;
        }

        // [임시] 성공 메시지를 리디렉션 직전 세션에 보관
        redirectAttributes.addFlashAttribute("msg", "내 정보가 수정되었습니다");

        // 수정 완료 후 새로고침 시 데이터 중복 제출 방지를 위한 redirect 설정
        mv.setViewName("redirect:/user/mypage");
        return mv;
    }
}