package com.pcgear.complink.pcgear.KJG.user.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Bean
	PasswordEncoder  passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean 
    public SecurityFilterChain httpFilterChain(HttpSecurity http) throws Exception {  
        http  
                .httpBasic(AbstractHttpConfigurer::disable)  
                .cors(cors ->  
                        cors.configurationSource(corsConfigurationSource()))  
                .csrf(AbstractHttpConfigurer::disable)  
                .formLogin(AbstractHttpConfigurer::disable)  
                .sessionManagement(sessionManagement -> sessionManagement  
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))  
                .exceptionHandling(handling -> handling  
                        .authenticationEntryPoint((request, response, authException) -> {  
                            response.setStatus(401);  
                        })  
                );  
  
        return http.build();  
    }  

    @Bean  
    public CorsConfigurationSource corsConfigurationSource(){  
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
