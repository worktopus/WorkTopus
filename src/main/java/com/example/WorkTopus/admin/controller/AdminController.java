package com.example.WorkTopus.admin.controller;

import com.example.WorkTopus.admin.dto.response.AdminProjectResponse;
import com.example.WorkTopus.admin.dto.response.AdminUserResponse;
import com.example.WorkTopus.admin.service.AdminDashboardService;
import com.example.WorkTopus.admin.service.AdminProjectService;
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
    private final AdminProjectService adminProjectService;
    private final AdminDashboardService adminDashboardService;

    @GetMapping("/dashboard")
    public ModelAndView dashboard() {

        ModelAndView mav = new ModelAndView("admin/dashboard");

        mav.addObject(
                "totalUserCount",
                adminDashboardService.getTotalUserCount()
        );

        mav.addObject(
                "activeUserCount",
                adminDashboardService.getActiveUserCount()
        );

        mav.addObject(
                "totalProjectCount",
                adminDashboardService.getTotalProjectCount()
        );

        mav.addObject(
                "todayUserCount",
                adminDashboardService.getTodayUserCount()
        );

        mav.addObject(
                "recentUsers",
                adminDashboardService.getRecentUsers()
        );

        mav.addObject(
                "recentProjects",
                adminDashboardService.getRecentProjects()
        );

        return mav;
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
    public ModelAndView projectList(
            @RequestParam(defaultValue = "name") String searchType,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page
    ) {

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                10,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<AdminProjectResponse> projectPage =
                adminProjectService.getProjects(
                        searchType,
                        keyword,
                        pageable
                );

        ModelAndView mv = new ModelAndView();

        mv.setViewName("admin/project-list");

        mv.addObject("searchType", searchType);
        mv.addObject("keyword", keyword);
        mv.addObject("projectPage", projectPage);
        mv.addObject("projects", projectPage.getContent());
        mv.addObject(
                "totalProjectCount",
                adminProjectService.getTotalProjectCount()
        );

        return mv;
    }

    @GetMapping("/reports")

    public ModelAndView reportList() {

        ModelAndView mv = new ModelAndView();

        mv.setViewName("admin/report-list");

        return mv;
    }
}