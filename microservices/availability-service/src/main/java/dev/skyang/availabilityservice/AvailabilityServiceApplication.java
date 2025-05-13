package dev.skyang.availabilityservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient    // enable Eureka client
public class AvailabilityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AvailabilityServiceApplication.class, args);
    }

}
