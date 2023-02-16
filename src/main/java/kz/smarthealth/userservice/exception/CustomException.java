package kz.smarthealth.userservice.exception;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class CustomException extends RuntimeException {

    private HttpStatus httpStatus;

    private String message;
}
