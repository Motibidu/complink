package com.pcgear.complink.pcgear.KJG.user.service;

//RecaptchaService.java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class RecaptchaService {

    private final RestTemplate restTemplate;

    // @Value("${recaptcha.secret-key}")
    private String secretKey = "6LfEFNIrAAAAAPC_kAnhZ1heNPqKSexPsoFFxUg7";

    // @Value("${recaptcha.verify-url}")
    private String verifyUrl = "https://www.google.com/recaptcha/api/siteverify";

    public RecaptchaService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean verifyRecaptcha(String recaptchaToken) {
        if (recaptchaToken == null || recaptchaToken.isEmpty()) {
            return false;
        }

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("secret", secretKey);
        body.add("response", recaptchaToken);

        try {
            // Google 서버에 POST 요청을 보냄
            Map<String, Object> response = restTemplate.postForObject(verifyUrl, body, Map.class);

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
