package com.rsmnarts.valoo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class ValooApplication {

	public static void main(String[] args) {
		SpringApplication.run(ValooApplication.class, args);
	}

}
