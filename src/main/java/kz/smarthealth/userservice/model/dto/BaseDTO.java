package kz.smarthealth.userservice.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import kz.smarthealth.userservice.util.AppConstants;
import lombok.Data;

import java.time.OffsetDateTime;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Data
public class BaseDTO {

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
