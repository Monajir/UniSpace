package com.techManiacs.UniSpace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UniSpaceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UniSpaceApplication.class, args);
	}

}
