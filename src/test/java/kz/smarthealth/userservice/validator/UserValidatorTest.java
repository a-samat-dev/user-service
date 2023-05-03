package kz.smarthealth.userservice.validator;

import jakarta.validation.ConstraintValidatorContext;
import kz.smarthealth.userservice.model.dto.RoleEnum;
import kz.smarthealth.userservice.model.dto.UserDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserValidator}
 *
 * Created by Samat Abibulla on 2023-05-03
 */
@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilderCustomizableContext;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder;

    @InjectMocks
    private UserValidator underTest;

    @Test
    void isValid_returnsFalse_whenPatientOrDoctorMandatoryFieldsNotPassed() {
        // given
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);
        when(constraintViolationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilderCustomizableContext);
        UserDTO userDTO = UserDTO.builder()
                .roles(Set.of(RoleEnum.ROLE_PATIENT))
                .build();
        // when
        boolean isValid = underTest.isValid(userDTO, constraintValidatorContext);
        // then
        assertFalse(isValid);
    }

    @Test
    void isValid_returnsFalse_whenDoctorTypeIdNotPassed() {
        // given
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);
        when(constraintViolationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilderCustomizableContext);
        UserDTO userDTO = UserDTO.builder()
                .roles(Set.of(RoleEnum.ROLE_DOCTOR))
                .birthDate(LocalDate.of(2000, 1, 1))
                .build();
        // when
        boolean isValid = underTest.isValid(userDTO, constraintValidatorContext);
        // then
        assertFalse(isValid);
    }

    @Test
    void isValid_returnsTrue() {
        // given
        UserDTO userDTO = UserDTO.builder()
                .roles(Set.of(RoleEnum.ROLE_DOCTOR))
                .birthDate(LocalDate.of(2000, 1, 1))
                .doctorTypeId((short) 1)
                .build();
        // when
        boolean isValid = underTest.isValid(userDTO, constraintValidatorContext);
        // then
        assertTrue(isValid);
    }
}