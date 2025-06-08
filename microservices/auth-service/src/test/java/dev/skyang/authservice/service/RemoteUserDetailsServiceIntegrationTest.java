package dev.skyang.authservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.skyang.authservice.model.RemoteUser; // Assuming RemoteUser is in this package
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import dev.skyang.authservice.config.UserServiceClientConfig; // Import the config that creates RestTemplate

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

// Using @SpringBootTest to load the full context including RemoteUserDetailsService and its dependencies
@SpringBootTest
// We need UserServiceClientConfig to get the RestTemplate bean that RemoteUserDetailsService uses
@Import(UserServiceClientConfig.class)
public class RemoteUserDetailsServiceIntegrationTest {

    @Autowired
    private RemoteUserDetailsService remoteUserDetailsService;

    @Autowired
    private RestTemplate restTemplate; // This is the bean used by RemoteUserDetailsService

    @Autowired
    private ObjectMapper objectMapper; // For creating JSON responses

    @Autowired
    private PasswordEncoder passwordEncoder; // To verify password (though it's hashed)

    @Value("${user.service.base.url}")
    private String userServiceBaseUrl;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void testLoadUserByUsername_Success() throws Exception {
        String username = "testuser";
        String expectedUrl = userServiceBaseUrl + "/api/users/internal/" + username;

        RemoteUser mockRemoteUser = new RemoteUser();
        mockRemoteUser.setId(1L);
        mockRemoteUser.setUsername(username);
        mockRemoteUser.setEmail(username + "@example.com");
        mockRemoteUser.setPassword(passwordEncoder.encode("password123")); // Hashed password

        mockServer.expect(ExpectedCount.once(), requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockRemoteUser), MediaType.APPLICATION_JSON));

        UserDetails userDetails = remoteUserDetailsService.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        // Password in UserDetails should be the same hashed password received from remote service
        assertEquals(mockRemoteUser.getPassword(), userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));

        mockServer.verify();
    }

    @Test
    void testLoadUserByUsername_UserNotFound() throws Exception {
        String username = "unknownuser";
        String expectedUrl = userServiceBaseUrl + "/api/users/internal/" + username;

        mockServer.expect(ExpectedCount.once(), requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(UsernameNotFoundException.class, () -> {
            remoteUserDetailsService.loadUserByUsername(username);
        });

        mockServer.verify();
    }

    @Test
    void testLoadUserByUsername_ServiceReturnsNullUser() throws Exception {
        String username = "nulluser";
        String expectedUrl = userServiceBaseUrl + "/api/users/internal/" + username;

        // Respond with success but an empty body or a body that Jackson maps to null
        mockServer.expect(ExpectedCount.once(), requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("null", MediaType.APPLICATION_JSON)); // Or use an empty string if appropriate

        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            remoteUserDetailsService.loadUserByUsername(username);
        });
        assertTrue(exception.getMessage().contains("remote service returned null"));
        mockServer.verify();
    }

    @Test
    void testLoadUserByUsername_ServiceReturnsUserWithNoPassword() throws Exception {
        String username = "nopassuser";
        String expectedUrl = userServiceBaseUrl + "/api/users/internal/" + username;

        RemoteUser mockRemoteUser = new RemoteUser();
        mockRemoteUser.setId(1L);
        mockRemoteUser.setUsername(username);
        mockRemoteUser.setEmail(username + "@example.com");
        mockRemoteUser.setPassword(null); // Password is null

        mockServer.expect(ExpectedCount.once(), requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockRemoteUser), MediaType.APPLICATION_JSON));

        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            remoteUserDetailsService.loadUserByUsername(username);
        });
        assertTrue(exception.getMessage().contains("Password for user " + username + " is missing"));
        mockServer.verify();
    }
}
