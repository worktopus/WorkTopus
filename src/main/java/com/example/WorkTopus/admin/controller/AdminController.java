package com.example.WorkTopus.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public ModelAndView memberList() {

        ModelAndView mv = new ModelAndView();

        mv.setViewName("admin/user-list");

        return mv;
    }

    @GetMapping("/projects")
    public ModelAndView projectList() {

        ModelAndView mv = new ModelAndView();

        mv.setViewName("admin/project-list");

        return mv;
    }

    @GetMapping("/reports")
    public ModelAndView repostList() {

        ModelAndView mv = new ModelAndView();

        mv.setViewName("admin/report-list");

        return mv;
    }
}