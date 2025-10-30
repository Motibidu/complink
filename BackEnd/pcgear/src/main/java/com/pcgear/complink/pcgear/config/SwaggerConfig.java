package com.pcgear.complink.pcgear.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
        @Bean
        public OpenAPI openAPI() {

                // 1. API 문서의 '머리말'에 해당하는 정보
                Info info = new Info()
                                .title("PCGear ERP API") // API 문서 제목
                                .version("v1.0.0") // API 버전
                                .description("PCGear ERP 프로젝트의 API 명세서입니다."); // API 상세 설명

                // 2. JWT 인증 방식을 API 문서에 적용하기 위한 설정
                // (만약 JWT를 사용하지 않는다면 이 부분은 생략 가능)

                // SecurityScheme 이름 (임의 지정)
                String jwtSchemeName = "jwtAuth";

                // API 요청 헤더에 인증 정보를 담는 방식 정의
                SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

                // SecuritySchemes 등록
                Components components = new Components()
                                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                                                .name(jwtSchemeName)
                                                .type(SecurityScheme.Type.HTTP) // HTTP 방식
                                                .scheme("bearer") // bearer 토큰 방식을 사용
                                                .bearerFormat("JWT")); // 토큰 형식은 JWT

                return new OpenAPI()
                                .info(info)
                                .addSecurityItem(securityRequirement) // 3. API 문서 전체에 보안 요구사항 적용
                                .components(components);
        }
}
