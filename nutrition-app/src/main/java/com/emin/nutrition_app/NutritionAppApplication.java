package com.emin.nutrition_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.emin.controller", "com.emin.entities", "com.emin.repository", "com.emin.services", "com.emin.security"})
@EntityScan(basePackages = {"com.emin.entities"})
@EnableJpaRepositories(basePackages = {"com.emin.repository"})
public class NutritionAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(NutritionAppApplication.class, args);
	}

}
