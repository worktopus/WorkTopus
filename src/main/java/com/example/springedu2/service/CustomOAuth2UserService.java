package com.example.springedu2.service;

import com.example.springedu2.entity.Member;
import com.example.springedu2.entity.Role;
import com.example.springedu2.repository.MemberRepository;
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

    private final MemberRepository memberRepository;

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

        memberRepository.findByEmailIgnoreCase(finalEmail)
                .orElseGet(() -> {
                    Member member = new Member();
                    member.setUsername(finalEmail);
                    member.setPassword("SOCIAL_LOGIN_USER");
                    member.setName(finalName);
                    member.setEmail(finalEmail);
                    member.setRole(Role.USER);
                    member.setEnabled(true);
                    member.setProvider(provider);
                    member.setProviderId(finalProviderId);
                    member.setPicture(finalPicture);
                    return memberRepository.save(member);
                });

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "kakao".equals(provider) ? "id" : "email"
        );
    }
}