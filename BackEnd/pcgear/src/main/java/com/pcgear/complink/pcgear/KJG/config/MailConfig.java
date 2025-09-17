package com.pcgear.complink.pcgear.KJG.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {
        @Bean
        public JavaMailSender javaMailService() {
                JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

                return javaMailSender;
        }
}
