package com.pcgear.complink.pcgear.User.service;

//RecaptchaService.java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.pcgear.complink.pcgear.properties.RecaptchaProperties;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecaptchaService {

    private final RestTemplate restTemplate;
    private final RecaptchaProperties recaptchaProperties;

    public boolean verifyRecaptcha(String recaptchaToken) {
        if (recaptchaToken == null || recaptchaToken.isEmpty()) {
            return false;
        }

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("secret", recaptchaProperties.getSecretKey());
        body.add("response", recaptchaToken);

        try {
            // Google 서버에 POST 요청을 보냄
            Map<String, Object> response = restTemplate.postForObject(recaptchaProperties.getVerifyUrl(), body,
                    Map.class);

            if (response == null || !response.containsKey("success")) {
                return false;
            }

            return (Boolean) response.get("success");

        } catch (Exception e) {
            // 로깅 추가 권장
            System.out.println("reCAPTCHA verification failed: " + e.getMessage());
            return false;
        }
    }
}
