package com.govportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class GovPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(GovPortalApplication.class, args);
    }
}
