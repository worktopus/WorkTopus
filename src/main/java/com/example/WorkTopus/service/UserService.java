package com.example.WorkTopus.service;

import com.example.WorkTopus.dto.UserCreateForm;
import com.example.WorkTopus.dto.UserUpdateForm;
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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = false)
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String user_id) throws UsernameNotFoundException {
        Users loginUser = userRepository.findByUserId(user_id)
                .orElseThrow( () -> new UsernameNotFoundException(
                        "존재하지 않는 사용자입니다"
                ));

        return User.builder()
                .username(loginUser.getUserId())  // 롬복 표준 카멜케이스 게터 호출
                .password(loginUser.getPassword())
                .disabled(!loginUser.isEnabled())
                .roles(loginUser.getRole().toString())
                .build();
    }

    public List<Users> findAll() {
        return userRepository.findAll();
    }

    public Users findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow( () -> new IllegalArgumentException(
                        "회원을 찾을 수 없습니다."
                ));
    }

    public Users findByUserName(String username) {
        return userRepository.findByUserId(username)
                .orElseThrow( () -> new IllegalArgumentException(
                        "회원을 찾을 수 없습니다"
                ));
    }

    public Users register(@Valid UserCreateForm userForm) throws IllegalAccessException {
        userForm.setRole(Role.USER.name());
        return create(userForm);
    }

    @Transactional
    public Users create(@Valid UserCreateForm userForm) throws IllegalAccessException {
        validNewMember(userForm.getUser_id(), userForm.getEmail());

        Users user = new Users();
        user.setUserId(userForm.getUser_id()); // 카멜케이스 세터로 변경
        user.setPassword(passwordEncoder.encode(userForm.getPassword()));
        user.setName(userForm.getName());
        user.setEmail(userForm.getEmail());
        user.setRole(parseRole(userForm.getRole()));
        user.setEnabled(true);

        return userRepository.save(user);
    }

    private void validNewMember(String username, String email) throws IllegalAccessException {
        if (userRepository.existsByUserId(username)) {
            throw new IllegalAccessException("이미 사용중인 아이디 입니다");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalAccessException("이미 사용중인 이메일 입니다");
        }
    }

    private Role parseRole(String role) {
        if (role == null || role.isBlank()) {
            return Role.USER;
        }
        return Role.valueOf(role.toUpperCase());
    }

    public UserUpdateForm toUpdateForm(Users user) {
        UserUpdateForm form = new UserUpdateForm();
        form.setName(user.getName());
        form.setEmail(user.getEmail());
        form.setRole(user.getRole().toString());
        form.setEnabled(user.isEnabled());
        return form;
    }

    @Transactional
    public Users update(Long id, @Valid UserUpdateForm userForm, boolean adminMode) {
        Users user = findById(id);

        if (userRepository.existsByEmailAndUserNumNot(userForm.getEmail(), id)) {
            throw new IllegalArgumentException("이미 사용중인 이메일 입니다.");
        }
        user.setName(userForm.getName());
        user.setEmail(userForm.getEmail());

        if (userForm.getPassword() != null && !userForm.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userForm.getPassword()));
        }

        if (adminMode) {
            user.setRole(parseRole(userForm.getRole()));
            user.setEnabled(userForm.isEnabled());
        }

        return user;
    }

    @Transactional
    public void delete(Long user_num, String name) {
        Users user = findById(user_num);
        if (user.getUserId().equals(name)) { // 카멜케이스 게터로 변경
            throw new IllegalArgumentException("현재 로그인한 자신은 삭제할 수 없습니다");
        }
        userRepository.delete(user);
    }
}