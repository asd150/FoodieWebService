package com.cs417.chadha.desai.patiala.FoodieWebService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.cs417.chadha.desai.patiala.FoodieWebService"})
public class FoodieWebServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FoodieWebServiceApplication.class, args);
	}
}
