package kz.smarthealth.userservice.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import kz.smarthealth.userservice.model.dto.UserRole;
import kz.smarthealth.userservice.model.dto.UserDTO;

/**
 * Validator class for user
 *
 * Created by Samat Abibulla 30/10/22
 */
public class UserValidator implements ConstraintValidator<User, UserDTO> {

    @Override
    public boolean isValid(UserDTO user, ConstraintValidatorContext context) {
        boolean isValid = true;

        if (user.getRoles() != null) {
            if ((user.getRoles().contains(UserRole.ROLE_DOCTOR) || user.getRoles().contains(UserRole.ROLE_PATIENT))
                    && user.getBirthDate() == null) {
                context.buildConstraintViolationWithTemplate("Invalid birth date")
                        .addPropertyNode("birthDate")
                        .addConstraintViolation();
                isValid = false;
            }
            if (user.getRoles().contains(UserRole.ROLE_DOCTOR) && user.getDoctorTypeId() == null) {
                context.buildConstraintViolationWithTemplate("Invalid doctor type")
                        .addPropertyNode("doctorTypeId")
                        .addConstraintViolation();
                isValid = false;
            }
        }

        return isValid;
    }
}
