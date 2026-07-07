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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final memberService memberService;

    // 회원목록
    @GetMapping("/admin/members")
    public String memberList(Model model) {
        List<Member> memberList = memberService.findAll();
        model.addAttribute("memberList", memberList);
        return "memberList";  // memberList.html
    }

    // 회원추가
    @PostMapping("/admin/members")
    public String adminCreate(
            @Valid @ModelAttribute("memberForm") MemberCreateForm memberCreateForm,
            BindingResult bindingResult) throws IllegalAccessException {

        if (bindingResult.hasErrors()) {
            return "/memberAdminForm"; //  다시 입력받기
        }

        // 새 회원을 추가
        try {
            memberService.create(memberCreateForm);
        } catch (IllegalArgumentException e) {
            bindingResult.reject("createFail", e.getMessage());
            return "memberAdmiForm";      // 회원추가실패 -> 다시 추가화면으로 이동
        }

        return "redirect:/admin/members"; // 목록조회
    }

    // 회원추가
    @GetMapping("/admin/members/new")
    public String adminCreateForm(Model model) {
        model.addAttribute("memberForm", new MemberCreateForm());
        return "memberAdminForm";
    }

    // 회원수정 화면 /admin/members/1/edit
    @GetMapping("/admin/members/{id}/edit")
    public String adminEditForm(@PathVariable Long id, Model model) {

        // 엔티티 안의 DB 데이터조회
        Member           member     = memberService.findById(id);

        // DTO 안의 memberAdminEditForm 에서 사용할 객체인 MemberUpdateForm 구조로 변겅
        MemberUpdateForm memberForm = memberService.toUpdateFrom(member);

        model.addAttribute("memberForm", memberForm);
        model.addAttribute("member", member);

        return "memberAdminEditForm";
    }

    // 넘어온 수정정보로 수정 진행
    @PostMapping("/admin/members/{id}/edit")
    public String adminEdit(
            @PathVariable Long id,
            @Valid @ModelAttribute("memberForm") MemberUpdateForm form,
            BindingResult bindingResult,
            Model model) {

        // 수정
        Member member = memberService.findById(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("memberForm", member);
            return "memberAdminEditForm";
        }

        try {
            memberService.update(id, form, true);
        } catch (IllegalArgumentException e) {
            bindingResult.reject("updateFail", e.getMessage());
            model.addAttribute("member", member);
            return "memberAdminEditForm";
        }

        return "redirect:/admin/members";
    }

    // 회원 삭제
    @PostMapping("/admin/members/{id}/delete")
    public String adminDelete(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            memberService.delete(id, authentication.getName());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("msg", e.getMessage());
        }

        return "redirect:/admin/members";
    }

}
