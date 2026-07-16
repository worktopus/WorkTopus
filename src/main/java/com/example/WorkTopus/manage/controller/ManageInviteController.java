package com.example.WorkTopus.manage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/manage")
public class ManageInviteController {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * 팀원 초대 이메일 발송 API (임시 데이터 및 고정 코드 처리)
     */
    @PostMapping("/send-email")
    public String sendInviteEmail(@RequestBody Map<String, String> payload) {
        // 프론트엔드 JS(project-manage.js)에서 보낸 데이터 추출
        String targetEmail = payload.get("email");
        String messageText = payload.get("message");
        String inviteCode = payload.get("code");

        // 이메일 유효성 간단 검증
        if (targetEmail == null || targetEmail.trim().isEmpty()) {
            return "EMAIL_EMPTY";
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(targetEmail);
            message.setSubject("[WorkTopus] 프로젝트 초대 코드 안내");

            // 메일 본문 구성 (임시 문구 + CWEXN8)
            String fullContent = messageText + "\n\n인증 코드 : " + inviteCode;
            message.setText(fullContent);

            // 실제 Gmail 서버를 통해 발송
            mailSender.send(message);
            return "SUCCESS";

        } catch (Exception e) {
            // 콘솔창에 구체적인 에러 확인용
            e.printStackTrace();
            return "FAIL";
        }
    }
}
