package dev.skyang.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable) // Moved CSRF disable to the top for conventional ordering
            .authorizeExchange(exchanges -> exchanges
                // Allow requests to auth-service for token generation.
                .pathMatchers("/auth/oauth2/token").permitAll() // Updated path
                .pathMatchers("/auth/**").permitAll() // Updated path, temporarily permit all to auth-service for simplicity

                // Allow user registration endpoint
                .pathMatchers("/users/register").permitAll() // Updated path

                // Allow actuator endpoints on the gateway itself
                .pathMatchers("/actuator/**").permitAll()

                .anyExchange().authenticated() // All other requests require a validated JWT
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtConverter()) // Use the custom converter
                )
            );
        return http.build();
    }

    private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtConverter() {
        JwtAuthenticationConverter delegate = new JwtAuthenticationConverter();
        // Configure delegate if needed (e.g., to extract authorities from custom claims)
        // delegate.setJwtGrantedAuthoritiesConverter(new GrantedAuthoritiesExtractor());
        return new ReactiveJwtAuthenticationConverterAdapter(delegate);
    }

    // Example GrantedAuthoritiesExtractor if you need to customize authority extraction
    // static class GrantedAuthoritiesExtractor implements Converter<Jwt, Collection<GrantedAuthority>> {
    //     public Collection<GrantedAuthority> convert(Jwt jwt) {
    //         List<String> roles = jwt.getClaimAsStringList("roles"); // Example: "roles": ["ROLE_USER", "ROLE_ADMIN"]
    //         if (roles == null) {
    //             return Collections.emptyList();
    //         }
    //         return roles.stream()
    //                 .map(SimpleGrantedAuthority::new)
    //                 .collect(Collectors.toList());
    //     }
    // }
}
