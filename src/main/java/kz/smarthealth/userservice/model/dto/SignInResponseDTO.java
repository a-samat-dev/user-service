package kz.smarthealth.userservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class SignInResponseDTO {

    private String accessToken;
    private String refreshToken;
    private UserDTO user;
}
