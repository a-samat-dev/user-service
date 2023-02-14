package kz.smarthealth.userservice.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import kz.smarthealth.userservice.validator.Password;
import kz.smarthealth.userservice.validator.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;
import static kz.smarthealth.userservice.util.AppConstants.DEFAULT_DATE;
import static kz.smarthealth.userservice.util.AppConstants.DEFAULT_OFFSET_DATE_TIME_FORMAT;

/**
 * Data transfer object for user
 * <p>
 * Created by Samat Abibulla 2022-10-09
 */
@Data
@Builder
@User
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    @JsonProperty(access = READ_ONLY)
    @Id
    private UUID id;

    @Email(message = "Email is not valid")
    @NotEmpty(message = "Email must be provided")
    @Size(max = 155, message = "Email max length = 255 characters")
    private String email;

    @Password
    @JsonProperty(access = WRITE_ONLY)
    @Transient
    private String password;

    @NotEmpty(message = "Name must be provided")
    @Size(max = 155, message = "Name max length = 155 characters")
    private String name;

    @Size(max = 155, message = "Last name max length = 155 characters")
    private String lastName;

    @DateTimeFormat(pattern = DEFAULT_DATE)
    @JsonFormat(shape = STRING, pattern = DEFAULT_DATE)
    private LocalDate birthDate;

    private Short doctorTypeId;

    @Size(max = 255, message = "Max size is 255 characters")
    private String about;

    @Valid
    @NotNull(message = "Contact must be provided")
    private ContactDTO contact;

    @NotEmpty(message = "User roles must be provided")
    private Set<String> roles = new HashSet<>();

    @JsonProperty(access = READ_ONLY)
    @JsonFormat(shape = STRING, pattern = DEFAULT_OFFSET_DATE_TIME_FORMAT)
    private OffsetDateTime createdAt;

    @JsonFormat(shape = STRING, pattern = DEFAULT_OFFSET_DATE_TIME_FORMAT)
    @JsonProperty(access = READ_ONLY)
    private OffsetDateTime updatedAt;

    @JsonProperty(access = READ_ONLY)
    private String createdBy;

    @JsonProperty(access = READ_ONLY)
    private String updatedBy;
}
