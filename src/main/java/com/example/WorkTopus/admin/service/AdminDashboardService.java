package com.example.WorkTopus.admin.service;

import com.example.WorkTopus.entity.Projects;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.repository.ProjectRepository;
import com.example.WorkTopus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public long getTotalUserCount() {
        return userRepository.count();
    }

    public long getActiveUserCount() {
        return userRepository.countByEnabledTrue();
    }

    public long getTotalProjectCount() {
        return projectRepository.count();
    }

    public long getTodayUserCount() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return userRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                startOfDay,
                endOfDay
        );
    }

    public List<Users> getRecentUsers() {
        return userRepository.findAll(
                PageRequest.of(
                        0,
                        5,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        ).getContent();
    }

    public List<Projects> getRecentProjects() {
        return projectRepository.findAll(
                PageRequest.of(
                        0,
                        5,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        ).getContent();
    }
}