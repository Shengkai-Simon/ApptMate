package dev.skyang.userservice.controller;

import dev.skyang.userservice.dto.RegisterUserRequest;
import dev.skyang.userservice.dto.UserResponse;
import dev.skyang.userservice.exception.UserAlreadyExistsException;
import dev.skyang.userservice.exception.UserNotFoundException;
import dev.skyang.userservice.model.User;
import dev.skyang.userservice.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterUserRequest registerUserRequest) {
        if (userRepository.findByUsername(registerUserRequest.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username '" + registerUserRequest.getUsername() + "' is already taken.");
        }

        if (userRepository.findByEmail(registerUserRequest.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email '" + registerUserRequest.getEmail() + "' is already registered.");
        }

        User user = new User();
        user.setUsername(registerUserRequest.getUsername());
        user.setEmail(registerUserRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerUserRequest.getPassword()));

        User savedUser = userRepository.save(user);

        // Ensure password is NOT returned in the registration response
        UserResponse response = new UserResponse();
        response.setId(savedUser.getId());
        response.setUsername(savedUser.getUsername());
        response.setEmail(savedUser.getEmail());
        response.setCreatedAt(savedUser.getCreatedAt());
        response.setUpdatedAt(savedUser.getUpdatedAt());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/internal/users/{username}")
    public ResponseEntity<UserResponse> getUserByUsernameForInternalAuth(@PathVariable String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getPassword() // Include hashed password for internal use
        );
        return ResponseEntity.ok(userResponse);
    }
}
