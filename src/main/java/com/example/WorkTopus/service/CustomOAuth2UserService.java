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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService
        implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User oauthUser =
                new DefaultOAuth2UserService().loadUser(userRequest);

        String provider =
                userRequest.getClientRegistration().getRegistrationId();

        Map<String, Object> originalAttributes =
                oauthUser.getAttributes();

        String providerId;
        String email;
        String name;
        String picture;

        if ("google".equals(provider)) {
            providerId = String.valueOf(
                    originalAttributes.get("sub")
            );

            email = String.valueOf(
                    originalAttributes.get("email")
            );

            name = String.valueOf(
                    originalAttributes.get("name")
            );

            picture = String.valueOf(
                    originalAttributes.get("picture")
            );

        } else if ("kakao".equals(provider)) {
            providerId = String.valueOf(
                    originalAttributes.get("id")
            );

            Map<String, Object> kakaoAccount =
                    (Map<String, Object>)
                            originalAttributes.get("kakao_account");

            Map<String, Object> profile =
                    (Map<String, Object>)
                            kakaoAccount.get("profile");

            email = providerId + "@kakao.local";
            name = String.valueOf(profile.get("nickname"));
            picture = String.valueOf(
                    profile.get("profile_image_url")
            );

        } else {
            throw new OAuth2AuthenticationException(
                    "지원하지 않는 소셜 로그인입니다."
            );
        }

        String userId = email;

        Users user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseGet(() -> {
                    Users newUser = new Users();

                    newUser.setUserId(userId);
                    newUser.setPassword("SOCIAL_LOGIN_USER");
                    newUser.setName(name);
                    newUser.setEmail(email);
                    newUser.setRole(Role.USER);
                    newUser.setEnabled(true);
                    newUser.setProvider(provider);
                    newUser.setProviderId(providerId);
                    newUser.setPicture(picture);

                    return userRepository.save(newUser);
                });

        Map<String, Object> customAttributes =
                new HashMap<>(originalAttributes);

        customAttributes.put("userId", user.getUserId());

        return new DefaultOAuth2User(
                List.of(
                        new SimpleGrantedAuthority("ROLE_USER")
                ),
                customAttributes,
                "userId"
        );
    }
}