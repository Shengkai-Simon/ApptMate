package dev.skyang.authservice.service;

import dev.skyang.authservice.model.RemoteUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;

@Service
public class RemoteUserDetailsService implements UserDetailsService {

    private final RestTemplate restTemplate;
    private final String userServiceBaseUrl;

    @Autowired
    public RemoteUserDetailsService(RestTemplate restTemplate, @Value("${user.service.base.url}") String userServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.userServiceBaseUrl = userServiceBaseUrl;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String url = UriComponentsBuilder.fromHttpUrl(userServiceBaseUrl)
                .path("/api/users/internal/{username}")
                .buildAndExpand(username)
                .toUriString();

        try {
            RemoteUser remoteUser = restTemplate.getForObject(url, RemoteUser.class);

            if (remoteUser == null) {
                throw new UsernameNotFoundException("User not found: " + username + ", remote service returned null");
            }

            // Ensure password from remoteUser is not null or empty
            if (remoteUser.getPassword() == null || remoteUser.getPassword().isEmpty()) {
                throw new UsernameNotFoundException("Password for user " + username + " is missing from remote service response.");
            }


            return new User(
                    remoteUser.getUsername(),
                    remoteUser.getPassword(), // This is the hashed password from user-service
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // Default role
            );
        } catch (HttpClientErrorException.NotFound ex) {
            throw new UsernameNotFoundException("User not found: " + username, ex);
        } catch (Exception ex) {
            // Log the exception for debugging
            // For example: log.error("Error fetching user details for {}", username, ex);
            throw new UsernameNotFoundException("Error fetching user details for " + username + ": " + ex.getMessage(), ex);
        }
    }
}
