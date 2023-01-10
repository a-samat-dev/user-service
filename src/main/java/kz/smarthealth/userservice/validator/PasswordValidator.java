package kz.smarthealth.userservice.validator;

import org.apache.commons.lang3.StringUtils;
import org.passay.*;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

/**
 * Validator class for user passwords
 *
 * Created by Samat Abibulla 5/10/22
 */
public class PasswordValidator implements ConstraintValidator<Password, String> {

    /**
     * Validates user password. Password must have:
     * - Min 8 characters
     * - Max 16 characters
     * - At least 1 upper case character
     * - At least 1 lower case character
     * - At least 1 digit
     * - At least 1 special character
     *
     * @param value   password entered by user
     * @param context {@link ConstraintValidatorContext}
     * @return result of validation
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value))
            return true;

        org.passay.PasswordValidator validator = getPasswordValidator();
        RuleResult result = validator.validate(new PasswordData(value));

        if (result.isValid()) {
            return true;
        }

        List<String> messages = validator.getMessages(result);
        String messageTemplate = String.join(",", messages);
        context.buildConstraintViolationWithTemplate(messageTemplate)
                .addConstraintViolation()
                .disableDefaultConstraintViolation();

        return false;
    }

    /**
     * Creates password validator with required rules
     *
     * @return password validator object
     */
    private org.passay.PasswordValidator getPasswordValidator() {
        return new org.passay.PasswordValidator(Arrays.asList(
                // at least 8 characters
                new LengthRule(8, 16),
                // at least one upper-case character
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                // at least one lower-case character
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                // at least one digit character
                new CharacterRule(EnglishCharacterData.Digit, 1),
                // at least one symbol (special character)
                new CharacterRule(EnglishCharacterData.Special, 1),
                // no whitespace
                new WhitespaceRule()
        ));
    }
}
