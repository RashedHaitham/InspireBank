package com.example.apiGateway.service;

import com.example.apiGateway.util.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JWTFilter implements WebFilter {

    private final JWTUtil jwtUtil;
    private static final Logger log = LoggerFactory.getLogger(JWTFilter.class);

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Optional<HttpCookie> jwtCookieOpt = getJwtTokenFromRequest(exchange.getRequest());

        String path = exchange.getRequest().getPath().toString();

        return Mono.justOrEmpty(jwtCookieOpt)
                .map(HttpCookie::getValue)
                .flatMap(token -> {
                    String username = jwtUtil.extractUsername(token);
                    if (jwtUtil.validateToken(token, username)) {

                        if (path.matches("/profile/\\w+")) {
                            String pathUsername = path.split("/")[2];
                            if (!username.equals(pathUsername)) {
                                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                                return exchange.getResponse().setComplete();
                            }
                        }

                        List<String> roles = jwtUtil.extractRoles(token);
                        List<GrantedAuthority> authorities = roles.stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());

                        Authentication auth =
                                new UsernamePasswordAuthenticationToken(username, null, authorities);

                        SecurityContext securityContext = new SecurityContextImpl(auth);

                        return chain.filter(exchange)
                                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                                .doOnEach(signal -> log.debug("Reactive context during filter: {}", signal.getContextView()));
                    }

                    return chain.filter(exchange);
                })
                .switchIfEmpty(chain.filter(exchange))
                .doOnEach(signal -> log.debug("Signal context at end of filter: {}", signal.getContextView()));
    }

    private Optional<HttpCookie> getJwtTokenFromRequest(ServerHttpRequest request) {
        return Optional.ofNullable(request.getCookies().getFirst("jwtToken"));
    }
}
