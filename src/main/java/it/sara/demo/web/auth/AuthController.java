package it.sara.demo.web.auth;

import it.sara.demo.dto.LoginRequestDTO;
import it.sara.demo.exception.GenericException;
import it.sara.demo.service.util.JwtUtil;
import it.sara.demo.service.database.FakeDatabase;
import it.sara.demo.web.response.GenericResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<GenericResponse> login(@RequestBody LoginRequestDTO request) throws GenericException {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new GenericException(400, "Email is required");
        }

        boolean exists = FakeDatabase.TABLE_USER.stream()
                .anyMatch(u -> request.getEmail().equalsIgnoreCase(u.getEmail()));

        if (!exists) {
            throw new GenericException(401, "Unauthorized");
        }

        String token = jwtUtil.generateToken(request.getEmail());
        return ResponseEntity.ok(GenericResponse.success(token));
    }
}