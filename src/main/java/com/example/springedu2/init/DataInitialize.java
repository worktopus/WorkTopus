package com.example.springedu2.init;

import com.example.springedu2.entity.Member;
import com.example.springedu2.entity.Role;
import com.example.springedu2.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// implements ApplicationRunner 비슷한 명령 : commandLineRunner(파라미터가 다름)
// 애플리케이션 실행되면 바로 실행할 명령을 기술
@Component
@RequiredArgsConstructor
public class DataInitialize implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        // 기존 계정이 없다면 admin, user 계정을 생성 -> mysql에 저장
        createIfNotExists("admin", "admin1234", "관리자", "admin@example.com", Role.ADMIN);
        createIfNotExists("user",  "user1234",  "일반",   "user@example.com",  Role.USER);
    }

    private void createIfNotExists(String username, String password,
                                   String name, String email, Role role) {
        // 계정이 존재한다면 return
        if(memberRepository.existsByUsername(username)) {
            return;
        }

        // 계정이 없다면 저장
        Member member = new Member();
        member.setUsername(username);
        member.setPassword(passwordEncoder.encode(password));
        member.setName(name);
        member.setEmail(email);
        member.setRole(role);
        member.setEnabled(true);

        memberRepository.save(member);

    }

}


