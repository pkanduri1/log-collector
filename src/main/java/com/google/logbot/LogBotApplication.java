package com.google.logbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Entry Point for the Log Bot Application.
 * <p>
 * Bootstraps the Spring Boot application, initializing all components,
 * including the Web Server, H2 Database, and AI Services.
 * </p>
 */
@SpringBootApplication
public class LogBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(LogBotApplication.class, args);
	}

}
