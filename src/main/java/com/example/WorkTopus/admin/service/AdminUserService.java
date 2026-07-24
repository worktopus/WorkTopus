package com.example.WorkTopus.admin.service;

import com.example.WorkTopus.admin.dto.response.AdminUserResponse;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;

    public Page<AdminUserResponse> getUsers(
            String keyword,
            String status,
            Pageable pageable
    ) {
        return userRepository
                .searchAdminUsers(normalize(keyword), normalize(status), pageable)
                .map(AdminUserResponse::from);
    }

    public long getTotalUserCount() {
        return userRepository.count();
    }

    public long getActiveUserCount() {
        return userRepository.countByEnabledTrue();
    }

    public long getInactiveUserCount() {
        return userRepository.countByEnabledFalse();
    }

    public long getWithdrawnUserCount() {
        return userRepository.countByDeleteAtIsNotNull();
    }

    @Transactional
    public void toggleEnabled(Long userNum) {
        Users user = userRepository.findById(userNum)
                .orElseThrow(() ->
                        new IllegalArgumentException("회원을 찾을 수 없습니다. userNum=" + userNum)
                );

        user.setEnabled(!user.isEnabled());
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}