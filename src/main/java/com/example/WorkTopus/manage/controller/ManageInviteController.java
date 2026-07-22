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
     * 팀원 초대 이메일 발송 API (화면에서 수집된 실제 프로젝트명 및 코드 결합)
     */
    @PostMapping("/send-email")
    public String sendInviteEmail(@RequestBody Map<String, String> payload) {
        String targetEmail = payload.get("email");
        String messageText = payload.get("message");
        String inviteCode = payload.get("code");

        if (targetEmail == null || targetEmail.trim().isEmpty()) {
            return "EMAIL_EMPTY";
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(targetEmail);

            // [변경] 제목에도 전송된 문구를 기반으로 자동화 타겟팅 매핑
            message.setSubject("[WorkTopus] 프로젝트 워크스페이스 초대 안내");

            // [변경] 유저가 입력창에서 확인한 동적 본문 텍스트와 진짜 코드를 정밀 조립
            String fullContent = messageText + "\n\n인증 초대 코드 : " + inviteCode + "\n\n시스템에 접속하여 위 코드를 입력해 주세요.";
            message.setText(fullContent);

            mailSender.send(message);
            return "SUCCESS";

        } catch (Exception e) {
            e.printStackTrace();
            return "FAIL";
        }
    }
}
