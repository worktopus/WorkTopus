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
    public void run(ApplicationArguments args) {
        createUserIfNotExists(
                "admin",
                "admin1234",
                "관리자",
                "admin@example.com",
                Role.ADMIN
        );
    }

    private void createUserIfNotExists(
            String userId,
            String password,
            String name,
            String email,
            Role role
    ) {

        // 이미 존재하면 생성하지 않음
        if (userRepository.existsByUserId(userId)) {
            return;
        }

        Users user = new Users();
        user.setUserId(userId);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        user.setEnabled(true);

        userRepository.save(user);
    }
}