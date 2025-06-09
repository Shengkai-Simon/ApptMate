package dev.skyang.authservice;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.jwk.source.JWKSource;
import dev.skyang.authservice.jose.Jwks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder; // Added
import org.springframework.security.oauth2.core.AuthorizationGrantType; // Added
import org.springframework.security.oauth2.core.ClientAuthenticationMethod; // Added
import org.springframework.security.oauth2.core.oidc.OidcScopes; // Added
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository; // Added
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient; // Added
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository; // Added
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings; // Added
// import org.springframework.security.oauth2.server.authorization.settings.TokenSettings; // Optional, stays commented for now
// import java.time.Duration; // Optional, stays commented for now
// import java.util.UUID; // Not strictly needed if client ID is hardcoded for this registration

@Configuration
public class AuthorizationServerConfig {

    @Bean
    public RegisteredClientRepository registeredClientRepository(PasswordEncoder passwordEncoder) { // Added PasswordEncoder for client secret
        RegisteredClient oidcClient = RegisteredClient.withId("oidc-client-id") // Unique ID for the registration
                .clientId("oidc-client")
                .clientSecret(passwordEncoder.encode("secret")) // Encode the secret
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .authorizationGrantType(AuthorizationGrantType.PASSWORD)
                .redirectUri("http://127.0.0.1:8080/login/oauth2/code/oidc-client")
                .redirectUri("http://appt-bff-gateway:3000/login/callback")
                .scope(OidcScopes.OPENID) // Using OidcScopes for standard scopes
                .scope(OidcScopes.PROFILE)
                .scope("api.read")
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(false) // As per config-repo
                        .requireAuthorizationConsent(true) // As per config-repo
                        .build())
                // TokenSettings can be added here if needed, e.g., access token TTL
                // .tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofHours(1)).build())
                .build();

        return new InMemoryRegisteredClientRepository(oidcClient);
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
