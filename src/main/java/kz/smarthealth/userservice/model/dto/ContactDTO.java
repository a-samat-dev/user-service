package kz.smarthealth.userservice.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import kz.smarthealth.commonlogic.util.AppConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

/**
 * Data transfer object for user contact
 *
 * Created by Samat Abibulla 2022-10-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactDTO {

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

    @JsonProperty(access = READ_ONLY)
    @JsonFormat(shape = STRING, pattern = AppConstants.DEFAULT_OFFSET_DATE_TIME_FORMAT)
    private OffsetDateTime createdAt;

    @JsonFormat(shape = STRING, pattern = AppConstants.DEFAULT_OFFSET_DATE_TIME_FORMAT)
    @JsonProperty(access = READ_ONLY)
    private OffsetDateTime updatedAt;

    @JsonProperty(access = READ_ONLY)
    private String createdBy;

    @JsonProperty(access = READ_ONLY)
    private String updatedBy;
}
