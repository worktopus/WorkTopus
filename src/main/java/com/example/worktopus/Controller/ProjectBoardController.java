package com.example.worktopus.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/project")
public class ProjectBoardController {

    @RequestMapping("/dashboard")
    public String dashboard() {
        return "project/dashboard";
    }

    @RequestMapping("/board-list")
    public String boardList() {
        return "project/board-list";
    }

    @RequestMapping("/board-list1")
    public String boardList1() {
        return "project/board-list1";
    }

    @RequestMapping("/board-detail")
    public String boardDetail() {
        return "project/board-detail";
    }

    @RequestMapping("/board-detail1")
    public String boardDetail1() {
        return "project/board-detail1";
    }

    @RequestMapping("/board-write")
    public String boardWrite() {
        return "project/board-write";
    }
}