package com.citrus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LoanPocApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoanPocApplication.class, args);
		System.out.println("==============================");
		System.out.println("========== STARTED ===========");
		System.out.println("==============================");
	}

}
