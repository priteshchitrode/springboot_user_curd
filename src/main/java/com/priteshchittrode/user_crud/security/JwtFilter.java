package com.priteshchittrode.user_crud.security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PublicEndpoints publicEndpoints;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        if (publicEndpoints.isPublic(requestUri)) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            sendError(response, "Missing token");
            return;
        }

        String token = header.substring(7);
        if (jwtUtil.isTokenExpired(token)) {
            sendError(response, "Token expired or invalid");
            return;
        }

        Long userId;
        try {
            userId = jwtUtil.extractUserId(token);
        } catch (Exception e) {
            sendError(response, "Invalid token");
            return;
        }

        // Set userId in request attribute
        request.setAttribute("userId", userId);

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":\"Failed\",\"message\":\"" + message + "\"}");
    }
}
