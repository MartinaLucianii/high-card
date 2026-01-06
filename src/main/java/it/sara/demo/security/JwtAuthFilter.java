package it.sara.demo.security;

import it.sara.demo.service.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Security filter that authenticates incoming requests using a Bearer JWT token.
 *
 * <p>How it works:
 * <ol>
 *   <li>Reads the {@code Authorization} header</li>
 *   <li>If it starts with {@code "Bearer "}, extracts the token</li>
 *   <li>Validates token signature/expiration via {@link JwtUtil}</li>
 *   <li>If valid, extracts the subject (username/email) and stores an Authentication object
 *       into {@link SecurityContextHolder}</li>
 * </ol>
 *
 * <p>If the header is missing/invalid, the request proceeds without authentication.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    /**
     * Utility for JWT validation and parsing (signature, expiration, subject extraction).
     */
    private final JwtUtil jwtUtil;

    /**
     * Creates the filter with the required JWT utility.
     *
     * @param jwtUtil utility used to validate tokens and extract the username
     */
    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Performs JWT authentication for each request (executed once per request).
     *
     * @param request the incoming HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain to continue processing
     * @throws ServletException if the filter chain throws a servlet-related exception
     * @throws IOException if an I/O error occurs while processing the request/response
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = authHeader.substring(7);

        if (!jwtUtil.validateJwtToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = jwtUtil.getUsernameFromToken(token);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, List.of());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}