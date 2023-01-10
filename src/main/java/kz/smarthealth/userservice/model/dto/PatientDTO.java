package kz.smarthealth.userservice.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class PatientDTO {

    private String name;
    private String lastName;
    private LocalDate birthDate;
    private UUID userId;
}
