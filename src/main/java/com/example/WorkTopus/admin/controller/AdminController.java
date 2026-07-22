package com.example.WorkTopus.admin.controller;

import com.example.WorkTopus.admin.dto.response.AdminUserResponse;
import com.example.WorkTopus.admin.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminUserService adminUserService;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/users")

    public ModelAndView userList(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "") String status,
            @RequestParam(defaultValue = "0") int page
    ) {

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                10,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<AdminUserResponse> userPage =
                adminUserService.getUsers(keyword, status, pageable);

        ModelAndView mv = new ModelAndView();

        mv.setViewName("admin/user-list");

        mv.addObject("userPage", userPage);
        mv.addObject("users", userPage.getContent());

        mv.addObject(
                "totalUserCount",
                adminUserService.getTotalUserCount()
        );

        mv.addObject(
                "activeUserCount",
                adminUserService.getActiveUserCount()
        );

        mv.addObject(
                "inactiveUserCount",
                adminUserService.getInactiveUserCount()
        );

        mv.addObject(
                "withdrawnUserCount",
                adminUserService.getWithdrawnUserCount()
        );

        mv.addObject("keyword", keyword);
        mv.addObject("status", status);

        return mv;
    }

    @PostMapping("/users/{userNum}/toggle-enabled")
    public String toggleUserEnabled(
            @PathVariable Long userNum,
            RedirectAttributes redirectAttributes
    ) {

        adminUserService.toggleEnabled(userNum);

        redirectAttributes.addFlashAttribute(
                "message",
                "회원 상태가 변경되었습니다."
        );

        return "redirect:/admin/users";
    }


    @GetMapping("/projects")
    public ModelAndView projectList() {

        ModelAndView mv = new ModelAndView();

        mv.setViewName("admin/project-list");

        return mv;
    }

    @GetMapping("/reports")

    public ModelAndView reportList() {

        ModelAndView mv = new ModelAndView();

        mv.setViewName("admin/report-list");

        return mv;
    }
}