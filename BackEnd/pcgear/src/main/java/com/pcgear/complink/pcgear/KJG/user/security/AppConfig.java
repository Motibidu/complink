package com.pcgear.complink.pcgear.KJG.user.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // PasswordEncoder를 여기서 독립적으로 생성하여 Bean으로 등록합니다.
        return new BCryptPasswordEncoder();
    }
}
