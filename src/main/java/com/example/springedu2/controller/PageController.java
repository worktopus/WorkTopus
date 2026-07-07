package com.example.springedu2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    // 로그인 페이지로 이동
    @GetMapping("/login")
    public  String login() {
        return "login";
    }

    // 로그인 처리할 주소가 필요 X
    // @PostMapping("/login")은 security filter가 처리하므로 코딩 X
    // db 처리로직을 별도의 클래스에 구현해서 security 가 자동으로 호출처리
    // UserDetailsService 에서는 loadUserByUsename() 실행조회 결과 반환
    // UserDetails 객체의 User로 저장해서 SpringSecurity에 보냄: 로그인 ok
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

}
