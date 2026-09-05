package com.confi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ConfiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfiApplication.class, args);
    }
}