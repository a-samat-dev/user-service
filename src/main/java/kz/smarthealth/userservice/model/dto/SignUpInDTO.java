package kz.smarthealth.userservice.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import kz.smarthealth.userservice.validator.Password;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO class used to sign up new user
 *
 * Created by Samat Abibulla on 2023-03-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpInDTO {

    @Email(message = "Email is not valid")
    @NotEmpty(message = "Email must be provided")
    @Size(max = 155, message = "Email max length = 155 characters")
    private String email;

    @Password
    private String password;

    private Set<UserRole> roles;
}
