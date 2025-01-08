package com.dogood.dogoodbackend;

import com.dogood.dogoodbackend.domain.volunteerings.MemoryVolunteeringRepository;
import com.dogood.dogoodbackend.service.FacadeManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DoGoodBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DoGoodBackendApplication.class, args);
	}

}
