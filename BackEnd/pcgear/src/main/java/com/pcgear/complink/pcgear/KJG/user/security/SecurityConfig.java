package com.pcgear.complink.pcgear.KJG.user.security;

//import com.pcgear.complink.pcgear.KJG.user.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // 생성자 주입을 위한 어노테이션
public class SecurityConfig {

        // CustomOAuth2UserService를 주입받습니다.
        // private final CustomOAuth2UserService customOAuth2UserService;

        @Bean
        public SecurityFilterChain httpFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .formLogin(form -> form.successHandler((request, response, authentication) -> {
                                        response.setStatus(HttpServletResponse.SC_OK);
                                }))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/").permitAll()

                                                .requestMatchers(
                                                                "/users/**",
                                                                "/email-verifications/**")
                                                .permitAll()

                                                .requestMatchers(
                                                                "/login",
                                                                "/login-process",
                                                                "/logout")
                                                .permitAll()

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
                                                        response.sendRedirect("http://localhost:5173/login");
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