package com.dogood.dogoodbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static com.dogood.dogoodbackend.utils.ValidateFields.isValidPhoneNumber;

@SpringBootApplication
public class DoGoodBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DoGoodBackendApplication.class, args);
		System.out.println(isValidPhoneNumber("0542026-033"));
	}

}
