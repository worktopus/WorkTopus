package com.example.WorkTopus;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.WorkTopus.dto.UserCreateForm;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.service.UserService;

@SpringBootTest
@Transactional 
class Springedu2ApplicationTests {

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("회원가입 성공 테스트")
    void registerUserTest() {
        // 1. Given (준비: 테스트할 회원 가입 데이터 세팅)
        UserCreateForm form = new UserCreateForm();
        form.setUserId("user123");
        form.setPassword("password123!");
        form.setName("테스터");
        form.setEmail("test@worktopus.com");

        // 2. When (실행: 회원가입 서비스 실행)
        Users registeredUser = userService.register(form);

        // 3. Then (검증: 저장된 데이터가 가입한 데이터와 일치하는지 확인)
        assertThat(registeredUser).isNotNull();
        assertThat(registeredUser.getUserId()).isEqualTo("testuser123");
        assertThat(registeredUser.getEmail()).isEqualTo("test@worktopus.com");
    }
}