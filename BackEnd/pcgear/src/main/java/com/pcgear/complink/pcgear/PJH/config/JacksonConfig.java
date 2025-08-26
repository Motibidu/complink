package com.pcgear.complink.pcgear.PJH.config; // 패키지는 원하는 곳에 만드세요.

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module; // 새로운 import 추가
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

        @Bean
        public Module hibernate6Module() { // 메서드 이름도 바꿔주는 것이 좋습니다.
                // 클래스 이름을 Hibernate6Module로 변경
                return new Hibernate6Module();
        }
}