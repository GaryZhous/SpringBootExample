package com.example.DisasterRelief;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class DisasterReliefApplication {

	public static void main(String[] args) {
		SpringApplication.run(DisasterReliefApplication.class, args);
	}

}
