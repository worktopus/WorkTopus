package com.example.WorkTopus.service;

import com.example.WorkTopus.dto.UserCreateForm;
import com.example.WorkTopus.entity.Role;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Users loginUser = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자입니다."));

        return User.builder()
                .username(loginUser.getUserId())
                .password(loginUser.getPassword())
                .disabled(!loginUser.isEnabled())
                .roles(loginUser.getRole().name())
                .build();
    }

    @Transactional(readOnly = true)
    public Users findByUserNum(Long userNum) {
        return userRepository.findById(userNum)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public Users findByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
    }

    public Users register(@Valid UserCreateForm userForm) throws IllegalAccessException {
        validateNewUser(userForm.getUserId(), userForm.getEmail());

        Users user = new Users();
        user.setUserId(userForm.getUserId());
        user.setPassword(passwordEncoder.encode(userForm.getPassword()));
        user.setName(userForm.getName());
        user.setEmail(userForm.getEmail());
        user.setRole(Role.USER);
        user.setEnabled(true);

        return userRepository.save(user);
    }

    private void validateNewUser(String userId, String email) throws IllegalAccessException {
        if (userRepository.existsByUserId(userId)) {
            throw new IllegalAccessException("이미 사용 중인 아이디입니다.");
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalAccessException("이미 사용 중인 이메일입니다.");
        }
    }
}