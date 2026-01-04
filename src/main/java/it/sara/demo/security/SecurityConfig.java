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

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

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