package it.sara.demo.web.auth;

import it.sara.demo.dto.LoginRequestDTO;
import it.sara.demo.exception.GenericException;
import it.sara.demo.service.database.FakeDatabase;
import it.sara.demo.service.util.JwtUtil;
import it.sara.demo.web.response.GenericResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication REST controller.
 *
 * <p>This controller exposes a minimal login endpoint that issues a JWT token for a known user.
 * The user existence check is performed against the in-memory {@link FakeDatabase} used by this project.
 *
 * <p><b>Important:</b> This is a simplified authentication flow used for the assignment/demo:
 * it validates only the user's email (no password verification).
 *
 * <p>On success, the controller returns HTTP 200 with a {@link GenericResponse} containing
 * a {@link it.sara.demo.dto.StatusDTO} where:
 * <ul>
 *   <li>{@code code = 200}</li>
 *   <li>{@code message = <generated JWT token>}</li>
 *   <li>{@code traceId = <random UUID>}</li>
 * </ul>
 *
 * <p>On failure, a {@link GenericException} is thrown and is expected to be converted into
 * a {@link GenericResponse} by the centralized exception handler (see {@code GlobalExceptionHandler}).
 */
@RestController
@RequestMapping("/auth")
public class AuthController {


    private final JwtUtil jwtUtil;

    /**
     * Creates a new {@link AuthController}.
     *
     * @param jwtUtil JWT utility used to generate authentication tokens
     */
    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * <p>Flow:
     * <ol>
     *  <li>Validates that the email field in {@link it.sara.demo.dto.LoginRequestDTO} is provided and not blank</li>
     *   <li>Checks whether a user with that email exists in {@link FakeDatabase#TABLE_USER}</li>
     *   <li>If found, generates a JWT token with the email as the subject</li>
     * </ol>
     *
     * <p>Error cases:
     * <ul>
     *   <li>If email is missing/blank: throws {@link GenericException} with code 400</li>
     *   <li>If the user does not exist: throws {@link GenericException} with code 401</li>
     * </ul>
     *
     * <p>Success response:
     * returns HTTP 200 with {@link GenericResponse#success(String)} where the message contains the token.
     *
     * @param request login request payload containing the user email
     * @return a {@link ResponseEntity} wrapping the {@link GenericResponse} with the generated token
     * @throws GenericException if validation fails or user is not authorized
     */
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