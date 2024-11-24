package com.example.apiGateway.config;

import com.example.apiGateway.service.JWTFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JWTFilter jwtFilter;

    public SecurityConfig(JWTFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }


    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/api/auth/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/user/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/user/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/user/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/employee/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/employee/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/employee/**").hasRole("ADMIN")
                        .anyExchange().authenticated()
                )
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .addFilterBefore(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> null;
    }

}
