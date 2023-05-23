package kz.smarthealth.userservice.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import kz.smarthealth.userservice.util.AppConstants;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

/**
 * Data transfer object for user contact
 *
 * Created by Samat Abibulla 2022-10-30
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactDTO extends BaseDTO {

    @JsonProperty(access = READ_ONLY)
    private UUID id;

    @NotNull(message = "City must be provided")
    private Short cityId;

    private String street;

    private String buildingNumber;

    private String flatNumber;

    @NotEmpty(message = "Phone number must be provided")
    private String phoneNumber1;

    private String phoneNumber2;
}
