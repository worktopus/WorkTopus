package com.example.WorkTopus.service;

import com.example.WorkTopus.dto.Post;
import com.example.WorkTopus.dto.UserCreateForm;
import com.example.WorkTopus.entity.Role;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.projects.entity.Board;
import com.example.WorkTopus.projects.entity.BoardComment;
import com.example.WorkTopus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.WorkTopus.dto.Comment;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /*
     * 일반 로그인 시 Spring Security가 자동 호출한다.
     * USER_ID로 회원을 찾고 로그인 검증에 필요한 정보를 반환한다.
     */
    @Override
    public UserDetails loadUserByUsername(String userId)
            throws UsernameNotFoundException {

        Users user = userRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "존재하지 않는 사용자입니다."
                        )
                );

        return User.builder()
                .username(user.getUserId())
                .password(user.getPassword())
                .disabled(!user.isEnabled())
                .roles(user.getRole().name())
                .build();
    }

    /*
     * USER_ID로 사용자 조회
     * 로그인한 사용자의 실제 Users 엔티티가 필요할 때 사용한다.
     */
    public Users findByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "회원을 찾을 수 없습니다."
                        )
                );
    }

    /*
     * 일반 회원가입
     */
    @Transactional
    public Users register(UserCreateForm userForm) {
        validateNewUser(
                userForm.getUserId(),
                userForm.getEmail()
        );

        Users user = new Users();
        user.setUserId(userForm.getUserId());
        user.setPassword(
                passwordEncoder.encode(userForm.getPassword())
        );
        user.setName(userForm.getName());
        user.setEmail(userForm.getEmail());
        user.setRole(Role.USER);
        user.setEnabled(true);

        return userRepository.save(user);
    }

    /*
     * 아이디·이메일 중복 검사
     */
    private void validateNewUser(
            String userId,
            String email
    ) {
        if (userRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 아이디입니다."
            );
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 이메일입니다."
            );
        }
    }

    @Transactional(readOnly = true)
    public boolean isUserIdAvailable(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException(
                    "아이디를 입력하세요."
            );
        }

        String normalizedUserId = userId.trim();

        if (normalizedUserId.length() < 4 ||
                normalizedUserId.length() > 30) {

            throw new IllegalArgumentException(
                    "아이디는 4~30자로 입력하세요."
            );
        }

        return !userRepository.existsByUserId(
                normalizedUserId
        );
    }

    // -------------------------------------------------------------------------------------
    // 마이페이지에서 정보 수정 //
    // 이름 변경
    @Transactional
    public void updateName(String userId, String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException(
                    "이름을 입력해주세요."
            );
        }

        Users user = findByUserId(userId);
        user.setName(newName.trim());
    }

    // 비밀번호 변경
    @Transactional
    public void updatePasswordWithoutCurrent(String userId, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty() || newPassword.length() < 4) {
            throw new IllegalArgumentException("새 비밀번호는 4자 이상이어야 합니다.");
        }

        Users user = findByUserId(userId);
        user.setPassword(passwordEncoder.encode(newPassword.trim()));
    }

    // 프로필 변경
    @Transactional
    public void updatePicture(String userId, String picture) {
        Users user = findByUserId(userId);
        user.setPicture(picture);
    }

    // 회원 탈퇴
    @Transactional
    public void deleteUser(String userId) {

        Users user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + userId));

        userRepository.delete(user);
    }

    // 마이페이지에서 글 조회 //
    public List<Post> findPostsByWriterName(String writerName) {
        List<Object[]> results = userRepository.findActiveBoardsWithProjectNameByWriterName(writerName);

        // 💡 댓글 조회처럼 단 한 줄로 깔끔하게 처리!
        return results.stream()
                .map(res -> new Post((Board) res[0], (String) res[1], (Long) res[2]))
                .collect(Collectors.toList());
    }

    // 마이페이지에서 댓글 조회 //
    public List<Comment> getMyComments(Long userNum) {
        // 1. DB에서 엔티티 리스트 조회
        List<BoardComment> comments = userRepository.findByWriterIdWithBoard(userNum);

        // 2. 엔티티(BoardComment) -> DTO(Comment)로 변환하여 리턴
        return comments.stream()
                .map(Comment::new)
                .collect(Collectors.toList());
    }

}