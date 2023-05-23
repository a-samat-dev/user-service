package kz.smarthealth.userservice.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import kz.smarthealth.userservice.util.AppConstants;
import kz.smarthealth.userservice.validator.User;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

/**
 * Data transfer object for user
 * <p>
 * Created by Samat Abibulla 2022-10-09
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@User
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO extends BaseDTO {

    @JsonProperty(access = READ_ONLY)
    private UUID id;

    @Email(message = "Email is not valid")
    @NotEmpty(message = "Email must be provided")
    @Size(max = 155, message = "Email max length = 255 characters")
    private String email;

    @NotEmpty(message = "Name must be provided")
    @Size(max = 155, message = "Name max length = 155 characters")
    private String name;

    @Size(max = 155, message = "Last name max length = 155 characters")
    private String lastName;

    @DateTimeFormat(pattern = AppConstants.DEFAULT_DATE)
    @JsonFormat(shape = STRING, pattern = AppConstants.DEFAULT_DATE)
    private LocalDate birthDate;

    private Short doctorTypeId;

    @Size(max = 255, message = "Max size is 255 characters")
    private String about;

    @Valid
    private ContactDTO contact;

    @NotEmpty(message = "User roles must be provided")
    private Set<UserRole> roles = new HashSet<>();

    private String profilePicturePreSignedUrl;
}
