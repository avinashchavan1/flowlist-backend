package com.flowlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FlowlistApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlowlistApplication.class, args);
    }
}
