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
                .pathMatchers("/auth-service/oauth2/token").permitAll()
                .pathMatchers("/auth-service/**").permitAll() // Temporarily permit all to auth-service for simplicity, can be tightened

                // Allow user registration endpoint if it's routed via gateway and intended to be public.
                // This path is on user-service, accessed via /user-service/** on gateway.
                .pathMatchers("/user-service/api/users/register").permitAll()

                // Allow actuator endpoints on the gateway itself
                .pathMatchers("/actuator/**").permitAll()

                .anyExchange().authenticated() // All other requests require a validated JWT
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())); // Enable JWT validation

        http.csrf(ServerHttpSecurity.CsrfSpec::disable); // Typically disable CSRF for stateless API gateway
        return http.build();
    }
}
