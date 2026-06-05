package com.atm.backend.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security — Session-Based Authentication.
 *
 * Design:
 * - Session-based (HTTPSession) over JWT to keep scope simple for the project.
 * - In-memory users seeded to match the 10 manually inserted accounts.
 * - Swagger UI, /login, /logout and account lookup permitted without authentication.
 * - All /api/transactions/** endpoints require an authenticated session.
 * - CORS allows the React dev server (localhost:5173) to call this API.
 *
 * Production Note: Replace InMemoryUserDetailsManager with a DB-backed UserDetailsService.
 */
@Configuration
@EnableWebSecurity
@SecurityScheme(
    name = "basicAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "basic"
)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/api/accounts/**"   // Account lookup is public
                ).permitAll()
                .requestMatchers("/api/transactions/**").authenticated() 
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> basic.authenticationEntryPoint((request, response, authException) -> {
                // Prevent the browser popup by returning 401 without the WWW-Authenticate header
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
            }))
            .logout(logout -> logout
                .logoutUrl("/logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        return new InMemoryUserDetailsManager(
            User.withUsername("alice").password(passwordEncoder.encode("password123")).roles("USER").build(),
            User.withUsername("bob").password(passwordEncoder.encode("password123")).roles("USER").build(),
            User.withUsername("carol").password(passwordEncoder.encode("password123")).roles("USER").build(),
            User.withUsername("david").password(passwordEncoder.encode("password123")).roles("USER").build(),
            User.withUsername("eva").password(passwordEncoder.encode("password123")).roles("USER").build(),
            User.withUsername("frank").password(passwordEncoder.encode("password123")).roles("USER").build(),
            User.withUsername("grace").password(passwordEncoder.encode("password123")).roles("USER").build(),
            User.withUsername("henry").password(passwordEncoder.encode("password123")).roles("USER").build(),
            User.withUsername("isabella").password(passwordEncoder.encode("password123")).roles("USER").build(),
            User.withUsername("james").password(passwordEncoder.encode("password123")).roles("USER").build()
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",
            "http://localhost:5500",
            "http://localhost:3000",
            "http://127.0.0.1:5500",
            "http://127.0.0.1:5173"
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
