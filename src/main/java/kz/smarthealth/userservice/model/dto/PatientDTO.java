package kz.smarthealth.userservice.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import kz.smarthealth.userservice.util.AppConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientDTO {

    private UUID userId;
    private String firstName;
    private String lastName;
    @JsonFormat(shape = STRING, pattern = AppConstants.DEFAULT_DATE)
    private LocalDate birthDate;
    private String phoneNumber;
}
