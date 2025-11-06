package com.pcgear.complink.pcgear;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.client.RestTemplate;

import com.pcgear.complink.pcgear.properties.DeliveryTrackerProperties;
import com.pcgear.complink.pcgear.properties.PortoneProperties;
import com.pcgear.complink.pcgear.properties.RecaptchaProperties;

@SpringBootApplication
@EnableConfigurationProperties({ DeliveryTrackerProperties.class, PortoneProperties.class, RecaptchaProperties.class })
@EnableJpaAuditing
public class PcgearApplication {

	public static void main(String[] args) {
		SpringApplication.run(PcgearApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
