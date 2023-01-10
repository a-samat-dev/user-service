package kz.smarthealth.userservice.model.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
@Builder
public class LoginRequestDTO {

    @Email
    @NotEmpty(message = "Email must be provided")
    private String email;
    @NotEmpty(message = "Password must be provided")
    private String password;
}
