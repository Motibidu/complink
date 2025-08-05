package com.pcgear.complink.pcgear.KJG.user.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;
    // 동시성 문제를 해결하기 위해 ConcurrentHashMap 사용
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();
    private static final String senderEmail = "jungi77777@gmail.com";

    // 인증 메일 발송
    public void sendVerificationMail(String mail) {
        String number = createNumber();
        verificationCodes.put(mail, number); // 이메일을 Key로, 인증번호를 Value로 저장

        MimeMessage message = createMail(mail, number);
        javaMailSender.send(message);
    }

    // 인증번호 검증
    public boolean verifyCode(String mail, String userNumber) {
        String storedCode = verificationCodes.get(mail);
        if (storedCode != null && storedCode.equals(userNumber)) {
            verificationCodes.remove(mail); // 인증 성공 시, 사용된 코드는 즉시 제거
            return true;
        }
        return false;
    }

    // 인증번호 생성
    private String createNumber() {
        int num = (int) (Math.random() * 90000) + 100000; // 100000 ~ 189999
        return String.valueOf(num);
    }
    
    // 메일 내용 생성
    private MimeMessage createMail(String mail, String number) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            message.setFrom(senderEmail);
            message.setRecipients(MimeMessage.RecipientType.TO, mail);
            message.setSubject("PCGEAR 아이디 찾기 이메일 인증");
            String body = "";
            body += "<h3 style='color: #333;'>요청하신 인증 번호입니다.</h3>";
            body += "<h1 style='color: #007BFF;'>" + number + "</h1>";
            body += "<h3>정확하게 입력해주세요.</h3>";
            message.setText(body, "UTF-8", "html");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return message;
    }
}