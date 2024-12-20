package com.example.apiGateway.controller;

import com.example.apiGateway.exception.UserNotFoundException;
import com.example.apiGateway.model.AuthRequest;
import com.example.apiGateway.model.AuthResponse;
import com.example.apiGateway.model.User;
import com.example.apiGateway.model.UserDTO;
import com.example.apiGateway.service.GatewayService;
import com.example.apiGateway.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



@RestController
@RequestMapping("/api")
public class APIController {

    private final GatewayService gatewayService;

    @Autowired
    public APIController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    // POST /api/auth/login - User Login
    @PostMapping("/auth/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody AuthRequest authRequest, ServerHttpResponse response) {
        return gatewayService.login(authRequest)
                .flatMap(authResponse -> {
                    if (authResponse.getBody() != null) {
                        String token = authResponse.getBody().getToken();
                        if (token == null || !isValidToken(token)) {
                            return Mono.error(new UserNotFoundException(token));
                        }
                            ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", token)
                                .httpOnly(true)
                                .path("/")
                                .maxAge(10 * 60 * 60)  // Token expiration (10 hours)
                                .build();

                        response.addCookie(jwtCookie);

                    }
                    return Mono.just(authResponse);
                });
    }

    private boolean isValidToken(String token) {
        return !token.contains(" ");
    }

    // POST /api/auth/signup - User Signup
    @PostMapping("/auth/signup")
    public Mono<ResponseEntity<String>> signup(@RequestBody User user) {
        return gatewayService.signup(user);
    }

    // POST /api/refresh-token - Refresh JWT Token
    @PostMapping("/refresh-token")
    public Mono<ResponseEntity<AuthResponse>> refreshToken(@CookieValue("jwtToken") String token) {
        return gatewayService.refreshToken(token);
    }

    // GET /api/users - Get all users
    @GetMapping("/users")
    public Mono<ResponseEntity<Flux<UserDTO>>> getAllUsers() {
        return gatewayService.getAllUsers();
    }

    @DeleteMapping("/user/{username}")
    public Mono<ResponseEntity<String>> deleteUser(@PathVariable String username) {
        return gatewayService.deleteUser(username);
    }

    @GetMapping("/profile/{username}")
    public Mono<ResponseEntity<String>> getUserProfile(@PathVariable String username, ServerWebExchange exchange) {
        return gatewayService.userProfile(username, exchange);

    }

}
