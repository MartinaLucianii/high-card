package it.sara.demo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.sara.demo.dto.StatusDTO;
import it.sara.demo.web.response.GenericResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.UUID;

/**
 * Spring Security configuration for JWT-based authentication.
 *
 * <p>Main settings:
 * <ul>
 *   <li>CSRF disabled (typical for stateless REST APIs)</li>
 *   <li>Stateless session (no HTTP session is created/used)</li>
 *   <li>JWT filter ({@link JwtAuthFilter}) is executed before Spring's username/password filter</li>
 *   <li>Public endpoints:
 *     <ul>
 *       <li>{@code /auth/**}</li>
 *       <li>{@code POST /user/v1/user} (user registration/creation)</li>
 *     </ul>
 *   </li>
 *   <li>All other requests require authentication</li>
 * </ul>
 *
 * <p>Project requirement: even authentication/authorization errors must return HTTP 200.
 * The real status code is mapped into {@link StatusDTO#getCode()} inside {@link GenericResponse}.
 */
@Configuration
public class SecurityConfig {

    /**
     * Custom JWT filter that reads the Bearer token and sets the SecurityContext when valid.
     */
    private final JwtAuthFilter jwtAuthFilter;

    /**
     * JSON serializer used to write error responses directly to the {@link jakarta.servlet.http.HttpServletResponse}.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates the security configuration with the required JWT filter.
     *
     * @param jwtAuthFilter filter responsible for JWT authentication
     */
    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * Configures the Spring Security filter chain.
     *
     * <p>Notes:
     * <ul>
     *   <li>Requests matching allowed patterns are not authenticated</li>
     *   <li>All other requests must contain a valid Bearer token</li>
     *   <li>Authentication and authorization errors are returned with HTTP 200 and an error {@link StatusDTO}</li>
     * </ul>
     *
     * @param http Spring Security HTTP configuration builder
     * @return configured {@link SecurityFilterChain}
     * @throws Exception if a configuration error occurs
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/user/v1/user").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> write200(res, 401, "Unauthorized"))
                        .accessDeniedHandler((req, res, e) -> write200(res, 403, "Forbidden"))
                );

        return http.build();
    }

    /**
     * Writes a JSON error response with HTTP status code 200 (project standard),
     * embedding the real error code and message inside {@link StatusDTO}.
     *
     * @param response HTTP response to write to
     * @param code application error code (e.g. 401, 403)
     * @param message error message (e.g. "Unauthorized", "Forbidden")
     * @throws java.io.IOException if the response cannot be written
     */
    private void write200(jakarta.servlet.http.HttpServletResponse response, int code, String message) throws java.io.IOException {
        response.setStatus(200);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        GenericResponse body = new GenericResponse();
        StatusDTO status = new StatusDTO();
        status.setCode(code);
        status.setMessage(message);
        status.setTraceId(UUID.randomUUID().toString());
        body.setStatus(status);

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}