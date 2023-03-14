package kz.smarthealth.userservice.service;

import kz.smarthealth.commonlogic.dto.RoleEnum;
import kz.smarthealth.commonlogic.exception.CustomException;
import kz.smarthealth.userservice.model.dto.SignUpInDTO;
import kz.smarthealth.userservice.model.dto.UserDTO;
import kz.smarthealth.userservice.model.entity.RoleEntity;
import kz.smarthealth.userservice.model.entity.UserEntity;
import kz.smarthealth.userservice.repository.RoleRepository;
import kz.smarthealth.userservice.repository.UserRepository;
import kz.smarthealth.userservice.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static kz.smarthealth.userservice.util.MessageSource.USER_BY_EMAIL_NOT_FOUND;
import static kz.smarthealth.userservice.util.MessageSource.USER_BY_ID_NOT_FOUND;
import static kz.smarthealth.userservice.util.TestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserService}
 * <p>
 * Created by Samat Abibulls on 2022-11-02
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Spy
    private ModelMapper modelMapper = new ModelMapper();
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtils jwtUtils;
    @InjectMocks
    private UserService underTest;

    @Test
    void signUp_throwsException_whenInvalidRoles() {
        // given
        SignUpInDTO signUpInDTO = SignUpInDTO.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.signUp(signUpInDTO));
        // then
        assertEquals("Invalid roles provided.", exception.getErrorMessage());
    }

    @Test
    void signUp_throwsException_whenEmailInUse() {
        // given
        SignUpInDTO signUpInDTO = SignUpInDTO.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .roles(Set.of(RoleEnum.ROLE_PATIENT))
                .build();
        when(userRepository.findByEmail(signUpInDTO.getEmail())).thenReturn(Optional.of(UserEntity.builder().build()));
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.signUp(signUpInDTO));
        // then
        assertEquals("test1@gmail.com is already in use, please provide another email address.",
                exception.getErrorMessage());
    }

    @Test
    void signUp_createsUser() {
        // given
        SignUpInDTO signUpInDTO = SignUpInDTO.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .roles(Set.of(RoleEnum.ROLE_PATIENT))
                .build();
        when(userRepository.findByEmail(signUpInDTO.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByName(signUpInDTO.getRoles().iterator().next().name()))
                .thenReturn(Optional.of(RoleEntity.builder()
                        .id((short) 1)
                        .name(signUpInDTO.getRoles().iterator().next().name())
                        .build()));
        // when
        SignUpInDTO actualSignUpInDTO = underTest.signUp(signUpInDTO);
        // then
        assertNotNull(actualSignUpInDTO);
        assertEquals(signUpInDTO.getEmail(), actualSignUpInDTO.getEmail());
        assertEquals(signUpInDTO.getPassword(), actualSignUpInDTO.getPassword());
    }

    @Test
    void signIn_throwsError_whenInvalidCredentialsProvided() {
        // given
        SignUpInDTO signInDTO = SignUpInDTO.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Unauthorized"));
        // when
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> underTest.signIn(signInDTO));
        // then
        assertEquals("Unauthorized", exception.getMessage());
    }

    @Test
    void signIn_throwsError_whenUserNotFound() {
        // given
        SignUpInDTO signInDTO = SignUpInDTO.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();
        when(authenticationManager.authenticate(any())).thenReturn(new TestingAuthenticationToken(null,
                null));
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> underTest.signIn(signInDTO));
        // then
        assertEquals(USER_BY_EMAIL_NOT_FOUND.getText(TEST_EMAIL), exception.getErrorMessage());
    }

    @Test
    void getUserById_throwsError_whenUserNotFound() {
        // given
        UUID invalidId = UUID.randomUUID();
        when(userRepository.findById(invalidId)).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> underTest.getUserById(invalidId));
        // then
        assertEquals(USER_BY_ID_NOT_FOUND.getText(invalidId.toString()), exception.getErrorMessage());
    }

    @Test
    void getUserById_returnsUser() {
        // given
        UserEntity userEntity = getUserEntity();
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        // when
        UserDTO userDTO = underTest.getUserById(userId);
        // then
        assertNotNull(userDTO);
        assertEquals(userEntity.getCreatedAt(), userDTO.getCreatedAt());
        assertEquals(userEntity.getCreatedBy(), userDTO.getCreatedBy());
        assertEquals(userEntity.getUpdatedAt(), userDTO.getUpdatedAt());
        assertEquals(userEntity.getUpdatedBy(), userDTO.getUpdatedBy());
        assertEquals(userEntity.getId(), userDTO.getId());
        assertEquals(userEntity.getEmail(), userDTO.getEmail());
        assertEquals(userEntity.getName(), userDTO.getName());
        assertEquals(userEntity.getLastName(), userDTO.getLastName());
        assertEquals(userEntity.getBirthDate(), userDTO.getBirthDate());
        assertEquals(userEntity.getAbout(), userDTO.getAbout());
        assertNotNull(userDTO.getContact());
        assertEquals(userEntity.getContact().getCreatedAt(), userDTO.getContact().getCreatedAt());
        assertEquals(userEntity.getContact().getCreatedBy(), userDTO.getContact().getCreatedBy());
        assertEquals(userEntity.getContact().getUpdatedAt(), userDTO.getContact().getUpdatedAt());
        assertEquals(userEntity.getContact().getUpdatedBy(), userDTO.getContact().getUpdatedBy());
        assertEquals(userEntity.getContact().getId(), userDTO.getContact().getId());
        assertEquals(userEntity.getContact().getCityId(), userDTO.getContact().getCityId());
        assertEquals(userEntity.getContact().getStreet(), userDTO.getContact().getStreet());
        assertEquals(userEntity.getContact().getBuildingNumber(), userDTO.getContact().getBuildingNumber());
        assertEquals(userEntity.getContact().getFlatNumber(), userDTO.getContact().getFlatNumber());
        assertEquals(userEntity.getContact().getPhoneNumber1(), userDTO.getContact().getPhoneNumber1());
        assertEquals(userEntity.getContact().getPhoneNumber2(), userDTO.getContact().getPhoneNumber2());
    }
}