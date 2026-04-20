package com.springbootapp.fyp.smartrent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartrentApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartrentApplication.class, args);
	}

}
