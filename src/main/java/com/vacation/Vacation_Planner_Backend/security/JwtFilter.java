package com.vacation.Vacation_Planner_Backend.security;

import com.vacation.Vacation_Planner_Backend.model.entity.User;
import com.vacation.Vacation_Planner_Backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final TokenBlacklistService tokenBlacklistService;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Get Authorization header
        final String authHeader = request.getHeader("Authorization");

        // If no token — skip filter, Spring Security will return 401
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        System.out.println(">>> Request URI: " + request.getRequestURI());
        System.out.println(">>> Auth header: " + authHeader);
        final String token = authHeader.substring(7);
        // Check if token is blacklisted ← добавили
        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String email = jwtService.extractEmail(token);

        // If email exists and user is not yet authenticated
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = userRepository.findByEmail(email).orElse(null);
            // Validate token
            if (user != null && jwtService.isTokenValid(token, user)) {
                // Create authentication object
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                user.getAuthorities()
                        );

                // Add request details to authentication
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                // Set authentication in SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // Pass request to next filter
        filterChain.doFilter(request, response);
    }
}
