package com.example.WorkTopus.service;

import com.example.WorkTopus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    // 이메일별 인증번호와 만료시간을 서버 메모리에 저장
    private final Map<String, VerificationData> verificationStore =
            new ConcurrentHashMap<>();

    // 인증번호 발송
    public void sendVerificationCode(String email) {
        String normalizedEmail = normalizeEmail(email);

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        String verificationCode = createVerificationCode();

        verificationStore.put(
                normalizedEmail,
                new VerificationData(
                        verificationCode,
                        LocalDateTime.now().plusMinutes(5)
                )
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(normalizedEmail);
        message.setSubject("[WorkTopus] 이메일 인증번호");
        message.setText(
                "WorkTopus 회원가입 인증번호는 "
                        + verificationCode
                        + "입니다.\n\n"
                        + "인증번호는 5분 동안 유효합니다."
        );

        mailSender.send(message);
    }

    // 인증번호 확인
    public void verifyCode(String email, String verificationCode) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedCode = verificationCode == null
                ? ""
                : verificationCode.trim();

        VerificationData verificationData =
                verificationStore.get(normalizedEmail);

        if (verificationData == null) {
            throw new IllegalArgumentException(
                    "인증번호를 먼저 발송해주세요."
            );
        }

        if (verificationData.expiresAt()
                .isBefore(LocalDateTime.now())) {

            verificationStore.remove(normalizedEmail);

            throw new IllegalArgumentException(
                    "인증번호가 만료되었습니다. 다시 발송해주세요."
            );
        }

        if (!verificationData.code().equals(normalizedCode)) {
            throw new IllegalArgumentException(
                    "인증번호가 올바르지 않습니다."
            );
        }
    }

    // 가입 완료 후 인증번호 삭제
    public void removeVerificationCode(String email) {
        verificationStore.remove(normalizeEmail(email));
    }

    private String createVerificationCode() {
        int number = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(number);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "이메일을 입력해주세요."
            );
        }

        return email.trim().toLowerCase();
    }

    private record VerificationData(
            String code,
            LocalDateTime expiresAt
    ) {
    }
}