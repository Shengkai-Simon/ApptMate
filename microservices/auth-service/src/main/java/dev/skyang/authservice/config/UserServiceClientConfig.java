package dev.skyang.authservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class UserServiceClientConfig {

    @Value("${user.service.base.url}")
    private String userServiceBaseUrl;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // Getter for the base URL if needed by other components,
    // though direct injection of RestTemplate and the URL string
    // into the service class is also common.
    public String getUserServiceBaseUrl() {
        return userServiceBaseUrl;
    }
}
