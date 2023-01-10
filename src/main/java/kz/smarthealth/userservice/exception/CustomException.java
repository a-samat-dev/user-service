package kz.smarthealth.userservice.exception;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@Builder
public class CustomException extends RuntimeException {

    private HttpStatus status;
    private String error;
    private String message;
}
