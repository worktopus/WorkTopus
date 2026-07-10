package com.example.WorkTopus.service;

import com.example.WorkTopus.entity.Role;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = new DefaultOAuth2UserService().loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauthUser.getAttributes();

        System.out.println("provider = " + provider);
        System.out.println("attributes = " + attributes);

        String providerId;
        String email;
        String name;
        String picture;

        if ("google".equals(provider)) {
            providerId = String.valueOf(attributes.get("sub"));
            email = String.valueOf(attributes.get("email"));
            name = String.valueOf(attributes.get("name"));
            picture = String.valueOf(attributes.get("picture"));
        } else if ("kakao".equals(provider)) {
            providerId = String.valueOf(attributes.get("id"));

            Map<String, Object> kakaoAccount =
                    (Map<String, Object>) attributes.get("kakao_account");

            Map<String, Object> profile =
                    (Map<String, Object>) kakaoAccount.get("profile");

            email = providerId + "@kakao.local";
            name = String.valueOf(profile.get("nickname"));
            picture = String.valueOf(profile.get("profile_image_url"));
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다.");
        }

        final String finalProviderId = providerId;
        final String finalEmail = email;
        final String finalName = name;
        final String finalPicture = picture;

        userRepository.findByEmailIgnoreCase(finalEmail)
                .orElseGet(() -> {
                    Users user = new Users();
                    user.setUserId(finalEmail);
                    user.setPassword("SOCIAL_LOGIN_USER");
                    user.setName(finalName);
                    user.setEmail(finalEmail);
                    user.setRole(Role.USER);
                    user.setEnabled(true);
                    user.setProvider(provider);
                    user.setProviderId(finalProviderId);
                    user.setPicture(finalPicture);
                    return userRepository.save(user);
                });

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "kakao".equals(provider) ? "id" : "email"
        );
    }
}