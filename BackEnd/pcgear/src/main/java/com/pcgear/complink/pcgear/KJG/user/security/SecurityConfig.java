package com.pcgear.complink.pcgear.KJG.user.security;

import com.pcgear.complink.pcgear.KJG.user.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // 생성자 주입을 위한 어노테이션
public class SecurityConfig {

    // CustomOAuth2UserService를 주입받습니다.
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain httpFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 모든 경로를 여기에 등록합니다.
                        .requestMatchers(
                                "/signup",
                                "/find/**",
                                "/login/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**", // Swagger UI의 리소스(CSS, JS)
                                "/v3/api-docs/**", // OpenAPI 3.0 명세서 JSON
                                "/swagger-resources/**", // Swagger 리소스
                                "/find-password/**",
                                "/error",
                                "/orders/**",
                                "/registers/**",
                                "/topic/**", "/managers/**", "/customers/**", "/items/**")
                        .permitAll()
                        .anyRequest().authenticated() // 그 외 모든 요청은 로그인 필요
                )
                // 1. 기존 아이디/비밀번호 방식 로그인 설정
                .formLogin(form -> form
                        .loginProcessingUrl("/login")
                        .loginPage("/login") // 로그인 페이지 경로
                        .successHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().write(authentication.getName() + "님, 환영합니다.");
                        })
                        .failureHandler((request, response, exception) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("로그인 실패: " + exception.getMessage());
                        }))
                // 2. 새로운 OAuth2 소셜 로그인 설정 추가
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login") // 로그인 페이지는 기존 폼과 공유
                        .userInfoEndpoint(userInfo -> userInfo
                                // ★★★ 로그인 성공 후, 사용자 정보를 처리할 서비스를 여기서 지정합니다. ★★★
                                .userService(customOAuth2UserService))
                        .successHandler((request, response, authentication) -> {
                            // 소셜 로그인 성공 후 처리 로직 (메인 페이지로 리디렉션 등)
                            response.sendRedirect("http://localhost:5173/"); // 성공 시 프론트엔드 메인 페이지로 이동
                        }))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                        }));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOrigin("http://localhost:5173");
        corsConfig.setAllowCredentials(true);
        corsConfig.addAllowedHeader("*");
        corsConfig.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }
}