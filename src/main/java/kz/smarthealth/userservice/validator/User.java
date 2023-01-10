package kz.smarthealth.userservice.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = UserValidator.class)
@Target({TYPE})
@Retention(RUNTIME)
public @interface User {

    String message() default "Invalid User";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
