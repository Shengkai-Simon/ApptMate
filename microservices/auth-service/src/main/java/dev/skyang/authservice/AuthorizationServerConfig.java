package dev.skyang.authservice;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.jwk.source.JWKSource;
import dev.skyang.authservice.jose.Jwks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// import org.springframework.jdbc.core.JdbcTemplate; // Stays commented out
// import org.springframework.security.oauth2.core.AuthorizationGrantType; // Stays commented out
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository; // Uncommented and used
// import org.springframework.security.oauth2.server.authorization.client.RegisteredClient; // Stays commented out
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository; // Uncommented and used
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
// import org.springframework.security.oauth2.server.authorization.settings.TokenSettings; // Stays commented out

// import java.time.Duration; // Stays commented out
// import java.util.UUID; // Stays commented out

@Configuration
public class AuthorizationServerConfig {

    @Bean // Uncommented and modified
    public RegisteredClientRepository registeredClientRepository() { // Removed JdbcTemplate parameter
        return new InMemoryRegisteredClientRepository(); // Simple instance
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        // generate an RSA key pair, expose JWK Set at /.well-known/jwks.json
        RSAKey rsaKey = Jwks.generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build(); // Issuer will be picked from properties
    }
}
