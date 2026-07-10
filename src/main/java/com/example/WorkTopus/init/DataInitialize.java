package com.example.WorkTopus.init;

import com.example.WorkTopus.entity.Role;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitialize implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        createIfNotExists("admin", "admin1234", "관리자", "admin@example.com", Role.ADMIN);
    }

    private void createIfNotExists(String user_id, String password,
                                   String name, String email, Role role) {
        // 계정이 존재한다면 return
        if(userRepository.existsByUserId(user_id)) {
            return;
        }

        // 계정이 없다면 저장
        Users member = new Users();
        member.setUserId(user_id);
        member.setPassword(passwordEncoder.encode(password));
        member.setName(name);
        member.setEmail(email);
        member.setRole(role);
        member.setEnabled(true);

        userRepository.save(member);

    }

}


