package kz.smarthealth.userservice.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PasswordValidator}
 */
@ExtendWith(MockitoExtension.class)
class PasswordValidatorTest {

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder;

    @InjectMocks
    private PasswordValidator underTest;

    @Test
    void isValid_returnsTrue_whenPasswordIsNull() {
        // when
        boolean isValid = underTest.isValid(null, constraintValidatorContext);
        // then
        assertTrue(isValid);
    }

    @Test
    void isValid_returnsTrue_whenValidPassword() {
        // given
        String password = "Aa123456!";
        // when
        boolean isValid = underTest.isValid(password, constraintValidatorContext);
        // then
        assertTrue(isValid);
    }

    @Test
    void isValid_returnsFalse_whenPasswordLessThenMinCharacters() {
        runTestWithNonNullInvalidCase("Aa1234!");
    }

    @Test
    void isValid_returnsFalse_whenPasswordMoreThenMaxCharacters() {
        runTestWithNonNullInvalidCase("Aa1234!1234567890");
    }

    @Test
    void isValid_returnsFalse_whenPasswordDoesNotContainUpperCaseCharacter() {
        runTestWithNonNullInvalidCase("aa12345!");
    }

    @Test
    void isValid_returnsFalse_whenPasswordDoesNotContainLowerCaseCharacter() {
        runTestWithNonNullInvalidCase("AA12345!");
    }

    @Test
    void isValid_returnsFalse_whenPasswordDoesNotContainDigit() {
        runTestWithNonNullInvalidCase("AAAAAAA!");
    }

    @Test
    void isValid_returnsFalse_whenPasswordDoesNotContainSpecialCharacter() {
        runTestWithNonNullInvalidCase("AA123456");
    }

    private void runTestWithNonNullInvalidCase(String invalidPassword) {
        // given
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);
        when(constraintViolationBuilder.addConstraintViolation()).thenReturn(constraintValidatorContext);
        // when
        boolean isValid = underTest.isValid(invalidPassword, constraintValidatorContext);
        // then
        assertFalse(isValid);
    }
}