package com.kjl.servicejava;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServiceJavaApplication {
    public static void main(String[] args) {
        // Workaround for Spring Boot 4.0.1 Java version detection bug in native images
        // Set system properties to override Java version detection before Spring Boot starts
        System.setProperty("java.version", "25.0.1");
        System.setProperty("java.specification.version", "25");
        
        SpringApplication.run(ServiceJavaApplication.class, args);
    }
}
