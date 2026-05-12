package com.tusas.vitrin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Tusas Vitrin Flight Tracker application.
 */
@SpringBootApplication
public class TusasVitrinApplication {

    /**
     * Bootstraps the Spring Boot application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(TusasVitrinApplication.class, args);
    }
}
