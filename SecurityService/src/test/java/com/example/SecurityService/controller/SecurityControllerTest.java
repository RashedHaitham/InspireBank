package com.example.SecurityService.controller;

import com.example.SecurityService.config.TestSecurityConfig;
import com.example.SecurityService.model.AuthRequest;
import com.example.SecurityService.model.AuthResponse;
import com.example.SecurityService.model.User;
import com.example.SecurityService.service.UserService;
import com.example.SecurityService.util.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebFluxTest(SecurityController.class)
@Import(TestSecurityConfig.class)
class SecurityControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ReactiveAuthenticationManager reactiveAuthenticationManager;

    @MockBean
    private JWTUtil jwtUtil;

    @MockBean
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void authenticate_shouldReturnAuthTokenWhenCredentialsAreValid() {
        // Arrange
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("rashed");
        authRequest.setPassword("password");

        User user = new User();
        user.setUsername("rashed");
        user.setFullName("rashed alqatarneh");

        Authentication authentication = new UsernamePasswordAuthenticationToken("rashed", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        String token = "mocked.jwt.token";

        when(reactiveAuthenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(Mono.just(authentication));
        when(userService.findByUsername(authRequest.getUsername()))
                .thenReturn(Mono.just(user));
        when(jwtUtil.generateToken(any(), any()))
                .thenReturn(Mono.just(token));

        // Act & Assert
        webTestClient.post()
                .uri("/security/authenticate")
                .contentType(APPLICATION_JSON)
                .bodyValue(authRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .value(authResponse -> {
                    assertNotNull(authResponse);
                    assertEquals(token, authResponse.getToken());
                });
    }

    @Test
    void signup_shouldReturnCreatedUserWhenValidDataIsProvided() {
        // Arrange
        User user = new User();
        user.setUsername("rashed");
        user.setPassword("password");
        user.setFullName("rashed alqatarneh");

        when(userService.savePerson(any(User.class)))
                .thenReturn(Mono.just(user));

        // Act & Assert
        webTestClient.post()
                .uri("/security/signup")
                .contentType(APPLICATION_JSON)
                .bodyValue(user)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .value(response -> assertEquals("User signed up successfully: rashed alqatarneh", response));
    }

    @Test
    void refreshToken_shouldReturnNewAuthToken() {
        // Arrange
        String refreshToken = "mocked.refresh.token";
        String username = "rashed";
        User user = new User();
        user.setUsername(username);
        user.setFullName("rashed alqatarneh");

        List<String> roles = List.of("ROLE_USER");
        String newToken = "mocked.new.jwt.token";

        when(jwtUtil.extractUsername(refreshToken)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(Mono.just(user));
        when(jwtUtil.extractRoles(refreshToken)).thenReturn(roles);
        when(jwtUtil.generateToken(any(), any())).thenReturn(Mono.just(newToken));

        // Act & Assert
        webTestClient.post()
                .uri("/security/refresh-token")
                .cookie("jwtToken", refreshToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .value(authResponse -> {
                    assertNotNull(authResponse);
                    assertEquals(newToken, authResponse.getToken());
                });
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        // Arrange
        User user1 = new User();
        user1.setUsername("rashed");
        user1.setFullName("rashed alqatarneh");

        User user2 = new User();
        user2.setUsername("ahmad");
        user2.setFullName("ahmad bali");

        when(userService.findAll()).thenReturn(Flux.just(user1, user2));

        // Act & Assert
        webTestClient.get()
                .uri("/security/users")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(User.class)
                .hasSize(2)
                .value(users -> {
                    assertEquals("rashed", users.get(0).getUsername());
                    assertEquals("ahmad", users.get(1).getUsername());
                });
    }

    @Test
    void deleteUser_shouldReturnDeletedMessageWhenUserExists() {
        // Arrange
        String username = "rashed";
        User user = new User();
        user.setUsername(username);
        user.setFullName("rashed alqatarneh");

        when(userService.findByUsername(username)).thenReturn(Mono.just(user));
        when(userService.deletePerson(user)).thenReturn(Mono.empty());

        // Act & Assert
        webTestClient.delete()
                .uri("/security/users/{username}", username)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(response -> assertEquals("User deleted successfully: " + username, response));
    }
}
