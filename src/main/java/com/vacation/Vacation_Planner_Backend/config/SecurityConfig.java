package com.vacation.Vacation_Planner_Backend.config;
import com.vacation.Vacation_Planner_Backend.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtFilter jwtFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — not needed for REST API
                .csrf(AbstractHttpConfigurer::disable)

                // Disable sessions — we use JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Configure endpoint access
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/logout").permitAll()
                        .requestMatchers("/api/auth/refresh").permitAll()

                        .requestMatchers("/api/vacations").authenticated()
                        .requestMatchers("/api/vacations/my").authenticated()
                        .requestMatchers("/api/vacations/balance").authenticated()
                        .requestMatchers("/api/vacations/team").authenticated()
                        .requestMatchers("/api/vacations/*/review").authenticated()
                        .requestMatchers("/api/vacations/my").authenticated()
                        .anyRequest().authenticated()
                )
                // Set authentication provider
                .authenticationProvider(authenticationProvider)
                // Add JwtFilter before Spring's default filter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}