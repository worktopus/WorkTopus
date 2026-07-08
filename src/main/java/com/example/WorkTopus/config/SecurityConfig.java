package com.example.WorkTopus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// SpringSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/","/index.html",
                            "/css/**", "/img/**", "/js/**",
                            "/login", "/user/register"
                    ).permitAll()       // 로그인 없이 사용가능
                    // .requestMatchers(
                    //        "/admin/**", "/vupdate", "/vdelete"
                    // ).hasRole("ADMIN")  // 추후 관리자페이지
                    .requestMatchers(
                            "/user/me"
                    ).authenticated()   // 로그인이 필요
                    .anyRequest().authenticated() // 설정하지 않은 다른 요청도 로그인 필요
            )
            .formLogin(form->form
                    .loginPage("/login")
                    .loginProcessingUrl("/login")  // 생략가능
                    .defaultSuccessUrl("/user/me", true)
                    .permitAll() // 로그인 페이지는 누구나 접근가능하다
                    // 로그인화면, 로그인처리 url, 로그인 실패 url 은 실패없이 접근가능 해야함
            )
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID"))
            .exceptionHandling(
                    exception ->
                            exception.accessDeniedPage("/access-denied")
            ); // 접근 거부 페이지 처리

        return http.build();
    }

    // 비밀번호 암호화
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
