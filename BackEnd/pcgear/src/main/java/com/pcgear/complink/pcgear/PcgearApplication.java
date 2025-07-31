package com.pcgear.complink.pcgear;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PcgearApplication {

	public static void main(String[] args) {
		SpringApplication.run(PcgearApplication.class, args);
	}

}
