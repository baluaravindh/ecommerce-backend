package com.balu.ecommerce.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // To handle 403 accessDenied Exception
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response,
                                                   authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\\\"status\\\":401,\\\"message\\\":\\\"Unauthorized: Please login first\\\"}");
                        })
                        .accessDeniedHandler((request, response,
                                              accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\\\"status\\\":403,\\\"message\\\":\\\"Access Denied: You don't have permission\\\"}"
                            );
                        })
                )
                .authorizeHttpRequests(auth -> auth

                                // PUBLIC endpoints — no token needed
                                .requestMatchers("/api/users/register").permitAll()
                                .requestMatchers("/api/users/login").permitAll()
                                .requestMatchers("api/users/refresh").permitAll()
                                .requestMatchers("/api/users/logout").permitAll()

                                // ADMIN only
//                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")

                                // AUTHENTICATED users (any role)
//                        .requestMatchers("/api/orders/**").authenticated()
//                        .requestMatchers("/api/users/**").authenticated()

                                // Everything else — authenticated
                                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
