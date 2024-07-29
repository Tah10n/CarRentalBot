package com.github.tah10n.carrentalbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CarRentalBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarRentalBotApplication.class, args);
    }
}
