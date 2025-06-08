package dev.skyang.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .authorizeExchange(exchanges -> exchanges
                // Allow requests to auth-service for token generation.
                // The /oauth2/token endpoint on auth-service handles its own client basic auth.
                // Requests to /auth-service/** are routed to auth-service.
                // The gateway itself doesn't need to apply additional auth for this specific path if auth-service secures it.
                // However, if the token endpoint is exposed through the gateway, it should be reachable.
                .pathMatchers("/auth/oauth2/token").permitAll() // Updated path
                .pathMatchers("/auth/**").permitAll() // Updated path, temporarily permit all to auth-service for simplicity

                // Allow user registration endpoint if it's routed via gateway and intended to be public.
                // Gateway route /users/** maps to /api/users/** on user-service.
                // User registration endpoint on user-service is /api/users/register.
                // So, the gateway path is /users/register.
                .pathMatchers("/users/register").permitAll() // Updated path

                // Allow actuator endpoints on the gateway itself
                .pathMatchers("/actuator/**").permitAll()

                .anyExchange().authenticated() // All other requests require a validated JWT
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())); // Enable JWT validation

        http.csrf(ServerHttpSecurity.CsrfSpec::disable); // Typically disable CSRF for stateless API gateway
        return http.build();
    }
}
