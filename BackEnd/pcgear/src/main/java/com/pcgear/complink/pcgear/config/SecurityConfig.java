package com.pcgear.complink.pcgear.config;

//import com.pcgear.complink.pcgear.KJG.user.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.pcgear.complink.pcgear.User.security.CustomAuthFailureHandler;

import org.springframework.web.cors.CorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // 생성자 주입을 위한 어노테이션
public class SecurityConfig {

        // CustomOAuth2UserService를 주입받습니다.
        // private final CustomOAuth2UserService customOAuth2UserService;
        private final CustomAuthFailureHandler customAuthFailureHandler;

        @Bean
        public SecurityFilterChain httpFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .formLogin(form -> form.successHandler((request, response, authentication) -> {
                                        response.setStatus(HttpServletResponse.SC_OK);

                                })
                                                .failureHandler(customAuthFailureHandler))
                                .authorizeHttpRequests(auth -> auth
                                                // 관리자 관련 페이지 경로
                                                .requestMatchers("/admin/**")
                                                .hasAnyAuthority("ADMIN")

                                                // 회원가입 요청 페이지 경로
                                                .requestMatchers(HttpMethod.GET, "/users/signup-req")
                                                .hasAnyAuthority("ADMIN")

                                                // 회원가입 승인 경로
                                                .requestMatchers(HttpMethod.POST, "/users/signup-approve/**")
                                                .hasAnyAuthority("ADMIN")

                                                // 품목 등록, 수정, 삭제
                                                .requestMatchers(HttpMethod.POST, "/items/**").hasAnyAuthority("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/items/**").hasAnyAuthority("ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/items/**")
                                                .hasAnyAuthority("ADMIN")


                                                

                                                // 웹훅
                                                .requestMatchers("/payment/webhook/verify/paymentLink").permitAll()
                                                .requestMatchers("/payment/webhook-verify").permitAll()
                                                .requestMatchers("/delivery/webhook").permitAll()

                                                // 회원가입 여부
                                                .requestMatchers("/users/isLoggedIn").authenticated()

                                                // 회원가입, 로그인, 이메일 인증
                                                .requestMatchers(
                                                                "/users/register", "/users/login-process",
                                                                "/email-verifications/**")
                                                .permitAll()

                                                // 로그인, 로그아웃
                                                .requestMatchers(
                                                                "/login",
                                                                "/login-process",
                                                                "/logout")
                                                .permitAll()

                                                // swagger
                                                .requestMatchers("/swagger-ui/**",
                                                                "/v3/api-docs/**",
                                                                "/swagger-resources/**")
                                                .permitAll()

                                                .anyRequest().authenticated())

                                .exceptionHandling(exceptionHandling -> exceptionHandling
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                                }))
                                // .oauth2Login(oauth2 -> oauth2
                                // .loginPage("/login")
                                // .userInfoEndpoint(userInfo -> userInfo
                                // .userService(customOAuth2UserService))
                                // .successHandler((request, response, authentication) -> {
                                // response.sendRedirect("http://localhost:5173/"); // 소셜 로그인 성공 시 프론트엔드 메인 페이지로
                                // 이동
                                // })
                                // .failureHandler((request, response, exception) -> {
                                // response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                // response.getWriter().write("OAuth2 로그인 실패: " + exception.getMessage());
                                // }))
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessHandler((request, response, authentication) -> {
                                                        response.setStatus(HttpServletResponse.SC_OK);
                                                        response.getWriter().write("로그아웃 되었습니다.");
                                                        // response.sendRedirect("http://localhost:5173/login");
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