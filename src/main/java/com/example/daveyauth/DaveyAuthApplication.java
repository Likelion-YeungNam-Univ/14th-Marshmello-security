package com.example.daveyauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DaveyAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(DaveyAuthApplication.class, args);
    }
}
