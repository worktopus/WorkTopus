package com.example.WorkTopus.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // 메인 로그인 페이지
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // 프로젝트 목록
    @GetMapping("/projects")
    public String projects() {
        return "projects";
    }

    // 프로젝트 생성
    @GetMapping("/projects/new")
    public String projectCreate() {
        return "project-create";
    }

    // 권한 없음
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

}