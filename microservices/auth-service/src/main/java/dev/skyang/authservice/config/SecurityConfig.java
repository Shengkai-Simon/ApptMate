package dev.skyang.authservice.config;

import dev.skyang.authservice.service.RemoteUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import dev.skyang.authservice.service.RemoteUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
// Removed Nimbus JOSE and SecurityContext imports as jwkSource is removed
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
// Removed AuthorizationServerSettings import as its bean is removed
import org.springframework.security.web.SecurityFilterChain;
// Removed RequestMatcher import as it's not used directly here
// Removed java.security.* and java.util.UUID imports as they were for jwkSource and generateRsaKey

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final RemoteUserDetailsService remoteUserDetailsService;

    public SecurityConfig(RemoteUserDetailsService remoteUserDetailsService) {
        this.remoteUserDetailsService = remoteUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(remoteUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder passwordEncoder, RemoteUserDetailsService userDetailsService) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
        authenticationManagerBuilder.authenticationProvider(authenticationProvider());
        return authenticationManagerBuilder.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults()); // Enable OpenID Connect 1.0
        http
            // Unauthenticated access to redirect_uri for client registration (if any)
            // and OIDC discovery endpoints
            .exceptionHandling(exceptions ->
                exceptions.authenticationEntryPoint(
                    new org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint("/login"))
            )
            .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/actuator/**", "/login").permitAll()
                    .anyRequest().authenticated()
            )
            .formLogin(Customizer.withDefaults()); // Default form login
        return http.build();
    }

    // Commented-out RegisteredClientRepository removed for cleanliness.
    // JWKSource bean, generateRsaKey method, and AuthorizationServerSettings bean removed as they are duplicates or auto-configured.
}
