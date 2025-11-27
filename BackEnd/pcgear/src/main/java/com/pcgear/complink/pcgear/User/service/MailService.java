package com.pcgear.complink.pcgear.User.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Slf4j import 추가
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j // Log 기능을 사용하기 위한 어노테이션 추가
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();
    private static final String senderEmail = "jack981109@gmail.com";

    // 인증 메일 발송
    // public void sendVerificationMail(String mail) {
    // log.info("인증 메일 발송 요청 시작. 수신자: {}", mail); // 1. 로그 추가
    // String number = createNumber();
    // verificationCodes.put(mail, number);

    // MimeMessage message = createMail(mail, number);

    // try {
    // log.info("▶▶▶ Gmail SMTP 서버로 메일 발송을 시도합니다..."); // 2. 로그 추가
    // javaMailSender.send(message);
    // log.info("▶▶▶ Gmail SMTP 서버로 메일 발송 요청 완료. (에러 없음)"); // 3. 로그 추가
    // } catch (Exception e) {
    // log.error("메일 발송 중 심각한 오류 발생", e); // 4. 예외 발생 시 로그 추가
    // throw e; // 예외를 다시 던져서 컨트롤러가 알 수 있도록 함
    // }
    // }

    // 임시 비밀번호 메일 발송
    // public void sendTemporaryPasswordMail(String email, String tempPassword) {
    // log.info("임시 비밀번호 메일 발송 요청 시작. 수신자: {}", email);
    // MimeMessage message = createPasswordMail(email, tempPassword);

    // try {
    // log.info("▶▶▶ Gmail SMTP 서버로 임시 비밀번호 메일 발송을 시도합니다...");
    // javaMailSender.send(message);
    // log.info("▶▶▶ Gmail SMTP 서버로 임시 비밀번호 메일 발송 요청 완료. (에러 없음)");
    // } catch (Exception e) {
    // log.error("임시 비밀번호 메일 발송 중 심각한 오류 발생", e);
    // throw e;
    // }
    // }

    // 인증번호 검증
    // public boolean verifyCode(String mail, String userNumber) {
    // String storedCode = verificationCodes.get(mail);
    // if (storedCode != null && storedCode.equals(userNumber)) {
    // verificationCodes.remove(mail); // 인증 성공 시, 사용된 코드는 즉시 제거
    // return true;
    // }
    // return false;
    // }

    // 인증번호 생성
    // private String createNumber() {
    // int num = (int) (Math.random() * 90000) + 100000; // 100000 ~ 189999
    // return String.valueOf(num);
    // }

    // 인증번호 메일 내용 생성
    // private MimeMessage createMail(String mail, String number) {
    // MimeMessage message = javaMailSender.createMimeMessage();
    // try {
    // message.setFrom(senderEmail);
    // message.setRecipients(MimeMessage.RecipientType.TO, mail);
    // message.setSubject("PCGEAR 이메일 인증");
    // String body = "";
    // body += "<h3 style='color: #333;'>요청하신 인증 번호입니다.</h3>";
    // body += "<h1 style='color: #007BFF;'>" + number + "</h1>";
    // body += "<h3>정확하게 입력해주세요.</h3>";
    // message.setText(body, "UTF-8", "html");
    // } catch (MessagingException e) {
    // e.printStackTrace();
    // }
    // return message;
    // }

    // 임시 비밀번호 메일 내용 생성
    // private MimeMessage createPasswordMail(String mail, String tempPassword) {
    // MimeMessage message = javaMailSender.createMimeMessage();
    // try {
    // message.setFrom(senderEmail);
    // message.setRecipients(MimeMessage.RecipientType.TO, mail);
    // message.setSubject("[PCGEAR] 임시 비밀번호가 발급되었습니다.");
    // String body = "";
    // body += "<h1>[PCGEAR] 임시 비밀번호 안내</h1>";
    // body += "<p>요청하신 임시 비밀번호는 아래와 같습니다.</p>";
    // body += "<p>로그인 후, 반드시 [마이페이지 > 회원정보 수정]에서 새로운 비밀번호로 변경해주세요.</p>";
    // body += "<div style='border: 2px solid #007bff; padding: 20px; text-align:
    // center; margin: 30px 0;'>";
    // body += "<h3 style='color: #dc3545;'>임시 비밀번호</h3>";
    // body += "<p style='font-size: 24px; font-weight: bold; letter-spacing:
    // 2px;'>" + tempPassword + "</p>";
    // body += "</div>";
    // message.setText(body, "UTF-8", "html");
    // } catch (MessagingException e) {
    // e.printStackTrace();
    // }
    // return message;
    // }

    public MimeMessage createDbErrorMail(String adminMail, Integer orderId, String errorMessage) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            message.setFrom(senderEmail);
            message.setRecipients(MimeMessage.RecipientType.TO, adminMail);

            // [제목] 긴급함을 강조
            message.setSubject("🚨 [긴급] PCGEAR 결제/DB 데이터 불일치 발생 (Order #" + orderId + ")");

            StringBuilder body = new StringBuilder();
            body.append(
                    "<div style='font-family: 'Apple SD Gothic Neo', sans-serif; padding: 20px; border: 1px solid #ddd; border-radius: 5px;'>");

            // [헤더] 경고 문구
            body.append("<h2 style='color: #dc3545; margin-bottom: 10px;'>⚠️ 치명적인 DB 오류가 감지되었습니다.</h2>");
            body.append(
                    "<p style='font-size: 14px; color: #555;'>외부 결제(환불) 처리는 성공했으나, <strong>내부 DB 반영 중 예외가 발생</strong>하여 데이터가 일치하지 않습니다.</p>");
            body.append(
                    "<p style='font-size: 14px; color: #555;'>관리자 페이지에서 <strong>[강제 취소]</strong> 기능을 통해 데이터를 보정해주세요.</p>");

            body.append("<hr style='border: 0; border-top: 1px solid #eee; margin: 20px 0;'>");

            // [상세 정보] 주문 번호 및 에러 내용
            body.append("<h3 style='color: #333;'>📋 상세 정보</h3>");
            body.append("<ul style='list-style: none; padding: 0;'>");
            body.append("<li style='margin-bottom: 10px;'><strong>주문 번호 (Order ID):</strong> " + orderId + "</li>");
            body.append("<li style='margin-bottom: 10px;'><strong>발생 시각:</strong> " + java.time.LocalDateTime.now()
                    + "</li>");
            body.append("</ul>");

            // [에러 로그 박스]
            body.append(
                    "<div style='background-color: #f8d7da; color: #721c24; padding: 15px; border-radius: 5px; margin-top: 20px; font-family: monospace;'>");
            body.append("<strong>Error Message:</strong><br>");
            body.append(errorMessage);
            body.append("</div>");

            body.append("</div>");

            message.setText(body.toString(), "UTF-8", "html");
        } catch (MessagingException e) {
            log.error("메일 생성 실패", e);
        }
        return message;
    }
}