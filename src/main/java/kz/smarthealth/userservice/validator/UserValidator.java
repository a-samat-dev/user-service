package kz.smarthealth.userservice.validator;

import kz.smarthealth.userservice.model.dto.UserDTO;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator class for user
 *
 * Created by Samat Abibulla 30/10/22
 */
public class UserValidator implements ConstraintValidator<User, UserDTO> {

    @Override
    public boolean isValid(UserDTO user, ConstraintValidatorContext context) {
        boolean isValid = true;

        if ((user.getRoles().contains("DOCTOR") || user.getRoles().contains("PATIENT")) && user.getBirthDate() == null) {
            context.buildConstraintViolationWithTemplate("Invalid birth date")
                    .addPropertyNode("birthDate")
                    .addConstraintViolation();
            isValid = false;
        }
        if (user.getRoles().contains("DOCTOR") && user.getDoctorTypeId() == null) {
            context.buildConstraintViolationWithTemplate("Invalid doctor type")
                    .addPropertyNode("doctorTypeId")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}
