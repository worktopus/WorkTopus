package com.example.WorkTopus.service;

import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.entity.Role;
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

    // 로그인을 위해 db에서 회원정보를 조회해서 UserDetails를 생성
    @Override
    public UserDetails loadUserByUsername(String user_id) throws UsernameNotFoundException {
        // 회원정보 db에서 회원이름으로 조회
        Users user = userRepository.findByUsername(user_id)
                .orElseThrow( () -> new UsernameNotFoundException(
                        "존재하지 않는 사용자입니다"
                ));
        // 조회한 결과를 Member -> UserDetails 로 변환
        UserDetails user = User.builder()
                .username(user.getUsername())     // 아이디
                .password(user.getPassword())     // 비번
                .disabled(!user.isEnabled())      // 계정 사용가능
                .roles(user.getRole().toString()) // 사용자 권한 "ADMIN" -> Role_ADMIN 권한
                .build();
        return user;
    }

    // ---------------------------------------------------------------------------------------
    // 회원조회
    // 전체조회
    public List<Users> findAll() {
        return userRepository.findAll();
    }

    // Id로 조회
    public Users findById(Long id) {
        return  userRepository.findById(id)
                .orElseThrow( () -> new IllegalArgumentException(
                        "회원을 찾을 수 없습니다."
                ));
    }

    // username으로 조회
    public Users findByUserName(String username) {
        return userRepository.findByUsername(user_id)
                .orElseThrow( () -> new IllegalArgumentException(
                        "회원을 찾을 수 없습니다"
                ));
    }

    // 일반유저 회원가입
    public Users register(@Valid UserCreateForm userForm) throws IllegalAccessException {
        userForm.setRole(Role.USER.name() );
        return create(userForm);
    }

    // 회원가입
    @Transactional
    public Users create(@Valid UserCreateForm userForm) throws IllegalAccessException {
        // 기존회원인지 조회
        validNewMember(userForm.getUser_id(), userForm.getEmail());

        Users user = new Users();

        user.setUser_id( userForm.getUser_id() );
        user.setPassword( passwordEncoder.encode( userForm.getPassword() ) );
        user.setName( userForm.getName() );
        user.setEmail( userForm.getEmail() );
        user.setRole( parseRole( userForm.getRole() ) );
        user.setEnabled(true);

        return userRepository.save(user);
    }

    // 기존회원 체크
    private void validNewMember(String username, String email) throws IllegalAccessException {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalAccessException("이미 사용중인 아이디 입니다");
        }
        if( userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalAccessException("이미 사용중인 이메일 입니다");
        }
    }

    // 권한 문자열 변환 "ADMIN" -> Role.ADMIN
    private Role parseRole(String role) {
        if( role == null || role.isBlank() ) {
            return Role.USER;
        }
        return Role.valueOf( role.toUpperCase() );
    }

    public UserUpdateForm toUpdateFrom(Users user) {
        UserUpdateForm form = new userUpdateForm();
        form.setName(user.getName());
        form.setEmail(user.getEmail());
        form.setRole(user.getRole().toString());
        form.setEnabled(user.isEnabled());
        return form;
    }

    // 회원정보 수정
    @Transactional
    public Users update(Long id, @Valid UserUpdateForm userForm, boolean adminMode) {
        Users user = findById(id);

        if (userRepository.existsByEmailAndIdNot(userForm.getEmail(), id)) {
            throw new IllegalArgumentException("이미 사용중인 이메일 입니다.");
        }
        user.setName(userForm.getName());
        user.setEmail(userForm.getEmail());

        if (userForm.getPassword() != null && !userForm.getPassword().isBlank()) {
            user.setPassword(
                passwordEncoder.encode( userForm.getPassword()) );
        }

        if (adminMode) {
            user.setRole(parseRole( userForm.getRole() ));
            user.setEnabled(userForm.isEnabled());
        }

        return user;
    }

    // 회원 삭제
    @Transactional
    public void delete(Long user_num, String name) {
        Users user = findById(user_num);
        if (user.getUser_id().equals(name)) {
            throw new IllegalArgumentException("현재 로그인한 자신은 삭제할 수 없습니다");
        }
        userRepository.delete(user);
    }

}
