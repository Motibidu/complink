package com.pcgear.complink.pcgear.KJG.config;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {
        @Bean
        public JavaMailSender javaMailService() {
                JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

                // 주입받은 값으로 JavaMailSenderImpl 설정
                javaMailSender.setHost("smtp.gmail.com");
                javaMailSender.setPort(587);
                javaMailSender.setUsername("jack981109@gmail.com");
                javaMailSender.setPassword("wmuskpxglwikoutn");

                // 추가적인 JavaMail 속성 설정
                Properties props = javaMailSender.getJavaMailProperties();
                props.put("mail.smtp.auth", true);
                props.put("mail.smtp.starttls.enable", true);
                // props.put("mail.smtp.starttls.required", starttlsRequired); // 필요시 주석 해제
                props.put("mail.smtp.timeout", 5000);
                props.put("mail.smtp.connectiontimeout", 5000); // connectiontimeout도 timeout과 동일하게 설정하는 경우가 많습니다.
                props.put("mail.smtp.writetimeout", 5000); // writetimeout도 timeout과 동일하게 설정하는 경우가 많습니다.

                // 디버그 모드를 활성화하여 메일 전송 과정을 자세히 볼 수 있습니다.
                // 개발 단계에서 유용하며, 운영 시에는 꺼두는 것이 좋습니다.
                // props.put("mail.debug", "true");

                return javaMailSender;
        }
}
