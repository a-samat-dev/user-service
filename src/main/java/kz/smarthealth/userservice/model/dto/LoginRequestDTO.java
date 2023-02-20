package kz.smarthealth.userservice.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRequestDTO {

    @Email
    @NotEmpty(message = "Email must be provided")
    private String email;
    @NotEmpty(message = "Password must be provided")
    private String password;
}
