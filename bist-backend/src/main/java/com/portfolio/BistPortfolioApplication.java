package com.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BistPortfolioApplication {

    public static void main(String[] args) {
        SpringApplication.run(BistPortfolioApplication.class, args);
    }
}
