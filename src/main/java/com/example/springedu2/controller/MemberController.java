package com.example.springedu2.controller;

import com.example.springedu2.dto.MemberCreateForm;
import com.example.springedu2.dto.MemberUpdateForm;
import com.example.springedu2.entity.Member;
import com.example.springedu2.service.memberService;
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
public class MemberController {

    private final memberService memberService;

    // 회원가입페이지 이동
    @GetMapping("/members/register")
    public String registerForm(Model model) {
        model.addAttribute("memberForm", new MemberCreateForm());
        return "memberRegister";  // memberReigster.html
    }

    // 회원가입
    @PostMapping("/members/register")
    public String registerMember(
            @Valid @ModelAttribute("memberForm") MemberCreateForm memberForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) throws IllegalAccessException {

        // 입력에 오류가 있다면 다시 입력화면으로 돌아가기
        if(bindingResult.hasErrors()) {
            return "memberRegister";  // memberReigster.html
        }

        // 회원가입 : DB에 저장
        try {
            memberService.register(memberForm);
        } catch (IllegalArgumentException e){
            redirectAttributes.addFlashAttribute("msg",
                    "회원가입이 실패했습니다." + e.getMessage() );
            bindingResult.reject("가입실패", e.getMessage());
            return "memberRegister";
        }

        redirectAttributes.addFlashAttribute("msg", "회원가입이 완료되었습니다. 로그인하세요");
        return "redirect:/login";
    }

    // 내 정보
    @GetMapping("/members/me")
    public String myPage(Authentication authentication, Model model) {
        System.out.println(("Authentication:" + authentication));

        Member member = memberService.findByUserName(authentication.getName());
        model.addAttribute("member", member);
        model.addAttribute("memberForm", memberService.toUpdateFrom(member));

        return "memberMyPage";
    }

    // 내 정보 수정
    @PostMapping("/members/me")
    public String updateMyPage(
            Authentication authentication,
            @Valid @ModelAttribute("memberForm") MemberUpdateForm memberForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        Member member = memberService.findByUserName(authentication.getName());

        if (bindingResult.hasErrors()){
            model.addAttribute("member", member);
            return "memberMyPage";
        }

        try {
            memberService.update(member.getId(), memberForm, false);
        } catch (IllegalArgumentException e) {
            bindingResult.reject("updateFail", e.getMessage());
            model.addAttribute("member", member);
            return "memberMyPage";
        }

        // 반드시 redirect 할때만 사용가능하다. redirect:/login
        redirectAttributes.addFlashAttribute("msg",
                "재 정보가 수정되었습니다");

        return "redirect:/members/me";
    }

}
