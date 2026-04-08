package com.example.DisasterRelief.config;

import com.example.DisasterRelief.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration.
 *
 * <p>Authentication is JWT-based and stateless.  Role-based access control rules:
 * <ul>
 *   <li>Public: {@code GET /}, {@code GET /request}, {@code POST /api/auth/login}, {@code POST /api/users} (self-registration)</li>
 *   <li>Authenticated (USER or ADMIN): {@code POST /api/send-request}</li>
 *   <li>ADMIN only: {@code GET /api/users}, {@code GET /api/users/{id}}, {@code PUT /api/users/{id}}, {@code DELETE /api/users/{id}}</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF – this REST API uses stateless JWT Bearer tokens.
                // CSRF attacks require that the browser automatically attach credentials
                // (e.g. cookies). Because authentication here relies solely on the
                // Authorization header, which browsers never send automatically,
                // CSRF protection is not applicable and can be safely disabled.
                .csrf(AbstractHttpConfigurer::disable)
                // Use stateless sessions (JWT carries all auth state)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Static assets and Thymeleaf pages
                        .requestMatchers("/", "/request", "/static/**", "/css/**", "/js/**", "/images/**").permitAll()
                        // Auth endpoint
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        // Self-registration
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        // Disaster relief request – any authenticated user
                        .requestMatchers(HttpMethod.POST, "/api/send-request").authenticated()
                        // User management – ADMIN only
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                // Place JWT filter before the standard username/password filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // Return 401 (not 403) when the request is unauthenticated
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        return http.build();
    }
}
