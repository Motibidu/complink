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
                                .authorizeHttpRequests(auth -> auth
                                                // 1. **가장 먼저 permitAll() 해야 할 정적 리소스 및 SPA 진입점 (index.html)**
                                                // AntPathRequestMatcher 사용으로 좀 더 명확하게 패턴 매칭
                                                .requestMatchers(
                                                                new AntPathRequestMatcher("/"), // 루트 경로
                                                                new AntPathRequestMatcher("/index.html"), // SPA의 기본 진입점
                                                                new AntPathRequestMatcher("/*.js"), // JS 파일
                                                                new AntPathRequestMatcher("/*.json"), // JSON 파일 (예:
                                                                                                      // manifest.json)
                                                                new AntPathRequestMatcher("/*.ico"), // favicon.ico
                                                                new AntPathRequestMatcher("/*.css"), // CSS 파일
                                                                new AntPathRequestMatcher("/static/**"), // 정적 리소스 (CRA
                                                                                                         // 스타일)
                                                                new AntPathRequestMatcher("/assets/**"), // **Vite 빌드
                                                                                                         // 결과물 (가장
                                                                                                         // 중요)**
                                                                new AntPathRequestMatcher("/manifest.json"),
                                                                new AntPathRequestMatcher("/logo*.*"),
                                                                new AntPathRequestMatcher("/favicon.ico"))
                                                .permitAll()

                                                // 2. **백엔드에서 처리하지만 인증이 필요 없는 API 경로**
                                                .requestMatchers(
                                                                new AntPathRequestMatcher("/users/**"), // 사용자 관련 비인증
                                                                                                        // API (ex:
                                                                                                        // 회원가입, 이메일 인증)
                                                                new AntPathRequestMatcher("/email-verifications/**"))
                                                .permitAll()

                                                // 3. **Spring Security의 인증/인가 로직에서 제외되어야 할 경로**
                                                // 실제 로그인 폼 제출 처리 URL: /login-process (POST)
                                                // OAuth2 로그인 시작 URL: /oauth2/authorization/{provider}
                                                // OAuth2 콜백 URL: /login/oauth2/code/{provider}
                                                .requestMatchers(
                                                                new AntPathRequestMatcher("/login-process"), // 폼 로그인
                                                                                                             // POST 처리
                                                                new AntPathRequestMatcher("/logout"), // 로그아웃 POST/GET
                                                                                                      // 처리
                                                                new AntPathRequestMatcher("/oauth2/**"), // OAuth2 로그인
                                                                                                         // 시작
                                                                new AntPathRequestMatcher("/login/oauth2/code/**") // OAuth2
                                                                                                                   // 콜백
                                                ).permitAll()
                                                .requestMatchers(
                                                                new AntPathRequestMatcher("/login"), // React 라우터의
                                                                                                     // /login 경로
                                                                new AntPathRequestMatcher("/signup"),
                                                                new AntPathRequestMatcher("/items"), // 이전에 에러가 났던
                                                                                                     // /items
                                                                // ... 여기에 React 라우터가 처리하는 모든 최상위 경로를 추가

                                                                // 더 일반적인 SPA 클라이언트 라우팅 처리:
                                                                // /api/로 시작하지 않는 모든 경로 (단일 경로 세그먼트)를 permitAll
                                                                new AntPathRequestMatcher("/{path:[^\\.]*}"), // 예:
                                                                                                              // /test,
                                                                                                              // /my-page
                                                                                                              // (파일 확장자
                                                                                                              // 없는 경로)
                                                                // /api/로 시작하지 않는 모든 경로 (다중 경로 세그먼트)를 permitAll
                                                                // 이 패턴은 `/api/`로 시작하는 실제 백엔드 API와 충돌하지 않도록 주의해야 합니다.
                                                                new AntPathRequestMatcher("/{path:^(?!api$).*$}/**") // 예:
                                                                                                                     // /my-page/sub-page
                                                ).permitAll()

                                                // 5. **기타 permitAll()이 필요한 경로**
                                                .requestMatchers(
                                                                new AntPathRequestMatcher("/swagger-ui/**"),
                                                                new AntPathRequestMatcher("/v3/api-docs/**"),
                                                                new AntPathRequestMatcher("/swagger-resources/**"))
                                                .permitAll() // Swagger
                                                .requestMatchers(
                                                                new AntPathRequestMatcher("/topic/**"),
                                                                new AntPathRequestMatcher("/ws/**"))
                                                .permitAll() // WebSocket
                                                .requestMatchers(
                                                                new AntPathRequestMatcher("/error"))
                                                .permitAll() // 에러 페이지는 인증 없이 접근 가능

                                                // 6. **나머지 모든 요청 (주로 백엔드 API)은 인증 필요**
                                                // 이 규칙은 항상 맨 마지막에 와야 합니다.
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login-process")
                                                .successHandler((request, response, authentication) -> {
                                                        response.setStatus(HttpServletResponse.SC_OK);
                                                        response.getWriter()
                                                                        .write(authentication.getName() + "님, 환영합니다.");
                                                        // 로그인 성공 시, 프론트엔드 URL로 리다이렉션
                                                        response.sendRedirect("http://localhost:5173/dashboard"); // 또는
                                                                                                                  // "/"
                                                })
                                                .failureHandler((request, response, exception) -> {
                                                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                                        response.getWriter().write("로그인 실패: " + exception.getMessage());
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