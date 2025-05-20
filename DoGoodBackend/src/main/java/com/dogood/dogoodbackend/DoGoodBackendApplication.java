package com.dogood.dogoodbackend;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Set;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class DoGoodBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DoGoodBackendApplication.class, args);
	}

}
