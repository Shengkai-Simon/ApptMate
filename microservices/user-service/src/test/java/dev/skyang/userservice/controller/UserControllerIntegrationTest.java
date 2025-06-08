package dev.skyang.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.skyang.userservice.dto.RegisterUserRequest;
import dev.skyang.userservice.model.User;
import dev.skyang.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;


@SpringBootTest
@AutoConfigureMockMvc
// Potentially use @ActiveProfiles("test") if your main application.yml conflicts
// and you have a specific test application-{profile}.yml
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll(); // Clean database before each test
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("testuser@example.com");

        MvcResult result = mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("testuser@example.com")))
                .andExpect(jsonPath("$.password").doesNotExist()) // Password should not be returned
                .andReturn();

        // Verify user in database
        User savedUser = userRepository.findByUsername("testuser").orElse(null);
        assertNotNull(savedUser);
        assertEquals("testuser@example.com", savedUser.getEmail());
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));
    }

    @Test
    void testRegisterUser_UsernameAlreadyExists() throws Exception {
        // First, register a user
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        existingUser.setPassword(passwordEncoder.encode("password123"));
        existingUser.setEmail("existing@example.com");
        userRepository.save(existingUser);

        // Attempt to register with the same username
        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("existinguser");
        request.setPassword("newpassword456");
        request.setEmail("newemail@example.com");

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict()) // Expect 409 Conflict
                .andExpect(jsonPath("$.message", is("Username 'existinguser' is already taken.")));
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() throws Exception {
        // First, register a user
        User existingUser = new User();
        existingUser.setUsername("anotheruser");
        existingUser.setPassword(passwordEncoder.encode("password123"));
        existingUser.setEmail("common@example.com");
        userRepository.save(existingUser);

        // Attempt to register with the same email
        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("newuser");
        request.setPassword("newpassword456");
        request.setEmail("common@example.com");

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Email 'common@example.com' is already registered.")));
    }

    // TODO: Add tests for validation failures (e.g., blank username, invalid email, short password)
}
