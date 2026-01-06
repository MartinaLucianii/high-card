package it.sara.demo.web.user.request;

import it.sara.demo.web.request.GenericRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request payload used to create or update a user via the REST API.
 *
 * <p>This DTO is validated by Bean Validation (Jakarta Validation) when used in controller
 * methods annotated with {@code @Valid}. Validation errors typically raise
 * {@code MethodArgumentNotValidException}, which in this project is converted to a
 * {@code GenericResponse} by the centralized exception handler.
 *
 * <p>Fields represent basic user details and include constraints for:
 * <ul>
 *   <li>required values ({@link NotBlank})</li>
 *   <li>maximum length ({@link Size})</li>
 *   <li>format restrictions ({@link Pattern})</li>
 * </ul>
 */
@Getter
@Setter
public class AddUserRequest extends GenericRequest {

    /**
     * User first name.
     *
     * <p>Constraints:
     * <ul>
     *   <li>must be provided and not blank</li>
     *   <li>maximum length: 20 characters</li>
     *   <li>must start with a letter and may contain letters, spaces and common name punctuation
     *       (dot, apostrophe, dash)</li>
     * </ul>
     */
    @NotBlank(message = "First name is required")
    @Size(max = 20, message = "First name is too long")
    @Pattern(
            regexp = "^\\p{L}[\\p{L} .'-]*$",
            message = "First name contains invalid characters"
    )
    private String firstName;

    /**
     * User last name.
     *
     * <p>Constraints:
     * <ul>
     *   <li>must be provided and not blank</li>
     *   <li>maximum length: 20 characters</li>
     *   <li>must start with a letter and may contain letters, spaces and common name punctuation
     *       (dot, apostrophe, dash)</li>
     * </ul>
     */
    @NotBlank(message = "Last name is required")
    @Size(max = 20, message = "Last name is too long")
    @Pattern(
            regexp = "^\\p{L}[\\p{L} .'-]*$",
            message = "Last name contains invalid characters"
    )
    private String lastName;

    /**
     * User email address.
     *
     * <p>Constraints:
     * <ul>
     *   <li>must be provided and not blank</li>
     *   <li>maximum length: 254 characters (common email max length)</li>
     * </ul>
     *
     * <p><b>Note:</b> this class does not enforce a strict email format via {@code @Email};
     * format checks may be performed in the service layer depending on project rules.
     */
    @NotBlank(message = "Email is required")
    @Size(max = 254, message = "Email is too long")
    private String email;

    /**
     * User phone number.
     *
     * <p>Constraints:
     * <ul>
     *   <li>must be provided and not blank</li>
     *   <li>maximum length: 20 characters</li>
     *   <li>must match Italian format: {@code +39} followed by 8 to 11 digits</li>
     * </ul>
     */
    @NotBlank(message = "Phone is required")
    @Size(max = 20, message = "Phone is too long")
    @Pattern(
            regexp = "^\\+39\\d{8,11}$",
            message = "Phone is not valid"
    )
    private String phoneNumber;
}