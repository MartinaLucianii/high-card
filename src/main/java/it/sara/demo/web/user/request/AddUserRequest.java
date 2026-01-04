package it.sara.demo.web.user.request;

import it.sara.demo.web.request.GenericRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddUserRequest extends GenericRequest {
    @NotBlank(message = "First name is required")
    @Size(max = 20, message = "First name is too long")
    @Pattern(
            regexp = "^\\p{L}[\\p{L} .'-]*$",
            message = "First name contains invalid characters"
    )
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 20, message = "Last name is too long")
    @Pattern(
            regexp = "^\\p{L}[\\p{L} .'-]*$",
            message = "Last name contains invalid characters"
    )
    private String lastName;

    @NotBlank(message = "Email is required")
    @Size(max = 254, message = "Email is too long")
    private String email;

    @NotBlank(message = "Phone is required")
    @Size(max = 20, message = "Phone is too long")
    @Pattern(
            regexp = "^\\+39\\d{8,11}$",
            message = "Phone is not valid"
    )
    private String phoneNumber;
}
