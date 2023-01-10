package kz.smarthealth.userservice.service;

import kz.smarthealth.userservice.exception.CustomException;
import kz.smarthealth.userservice.model.dto.ContactDTO;
import kz.smarthealth.userservice.model.dto.LoginRequestDTO;
import kz.smarthealth.userservice.model.dto.PatientDTO;
import kz.smarthealth.userservice.model.dto.UserDTO;
import kz.smarthealth.userservice.model.entity.RoleEntity;
import kz.smarthealth.userservice.model.entity.UserEntity;
import kz.smarthealth.userservice.repository.RoleRepository;
import kz.smarthealth.userservice.repository.UserRepository;
import kz.smarthealth.userservice.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
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
import static kz.smarthealth.userservice.util.TestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserService}
 *
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

    @Captor
    private ArgumentCaptor<Set<PatientDTO>> patientDTOArgumentCaptor;

    @Test
    void isEmailAvailable_returnsTrue_whenEmailIsNotInUse() {
        // given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        // when
        boolean isEmailAvailable = underTest.isEmailAvailable(TEST_EMAIL);
        // then
        assertTrue(isEmailAvailable);
    }

    @Test
    void isEmailAvailable_returnsFalse_whenEmailIsInUse() {
        // given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(UserEntity.builder().build()));
        // when
        boolean isEmailAvailable = underTest.isEmailAvailable(TEST_EMAIL);
        // then
        assertFalse(isEmailAvailable);
    }

    @Test
    void createUser_throwsError_whenDuplicatedEmail() {
        // given
        UserDTO userDTO = UserDTO.builder()
                .email(TEST_EMAIL)
                .build();
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(UserEntity.builder()
                .email(TEST_EMAIL)
                .build()));
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.createUser(userDTO));
        // then
        assertEquals("test1@gmail.com is already in use, please provide another email address.",
                exception.getMessage());
    }

    @Test
    void createUser_throwsError_whenInvalidRole() {
        // given
        ContactDTO contactDTO = ContactDTO.builder()
                .build();
        UserDTO userDTO = UserDTO.builder()
                .email(TEST_EMAIL)
                .contact(contactDTO)
                .roles(Set.of(TEST_ROLE_ORGANIZATION))
                .build();
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(UserEntity.builder()
                .email(TEST_EMAIL)
                .build()));
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        when(roleRepository.findByName(TEST_ROLE_ORGANIZATION)).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.createUser(userDTO));
        // then
        assertEquals("Invalid role provided: " + userDTO.getRoles().iterator().next(),
                exception.getMessage());
    }

    @Test
    void createUser_returnsCreatedUser() {
        // given
        UserDTO userDTO = getUserDTO();
        UserEntity userEntity = modelMapper.map(userDTO, UserEntity.class);
        userEntity.setId(UUID.randomUUID());
        userDTO.setRoles(Set.of(TEST_ROLE_DOCTOR));
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(UserEntity.builder()
                .email(TEST_EMAIL)
                .build()));
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        when(roleRepository.findByName(TEST_ROLE_DOCTOR)).thenReturn(Optional.of(RoleEntity.builder()
                .id((short) 2)
                .name(TEST_ROLE_DOCTOR)
                .build()));
        when(userRepository.save(any())).thenReturn(userEntity);
        // when
        UserDTO createdUserDTO = underTest.createUser(userDTO);
        // then

        assertEquals(userDTO.getEmail(), createdUserDTO.getEmail());
        assertNotNull(userDTO.getPassword());
        assertEquals(userDTO.getName(), createdUserDTO.getName());
        assertEquals(userDTO.getLastName(), createdUserDTO.getLastName());
        assertEquals(userDTO.getBirthDate(), createdUserDTO.getBirthDate());
        assertEquals(userDTO.getDoctorTypeId(), createdUserDTO.getDoctorTypeId());
        assertEquals(userDTO.getAbout(), createdUserDTO.getAbout());
        assertEquals(TEST_ROLE_DOCTOR, createdUserDTO.getRoles().iterator().next());
        assertEquals(userDTO.getContact().getCityId(), createdUserDTO.getContact().getCityId());
        assertEquals(userDTO.getContact().getStreet(), createdUserDTO.getContact().getStreet());
        assertEquals(userDTO.getContact().getBuildingNumber(), createdUserDTO.getContact().getBuildingNumber());
        assertEquals(userDTO.getContact().getFlatNumber(), createdUserDTO.getContact().getFlatNumber());
        assertEquals(userDTO.getContact().getPhoneNumber1(), createdUserDTO.getContact().getPhoneNumber1());
        assertEquals(userDTO.getContact().getPhoneNumber2(), createdUserDTO.getContact().getPhoneNumber2());
    }

    @Test
    void authenticateUser_throwsError_whenInvalidCredentialsProvided() {
        // given
        LoginRequestDTO loginRequestDTO = LoginRequestDTO.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Unauthorized"));
        // when
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> underTest.authenticateUser(loginRequestDTO));
        // then
        assertEquals("Unauthorized", exception.getMessage());
    }

    @Test
    void authenticateUser_throwsError_whenUserNotFound() {
        // given
        LoginRequestDTO loginRequestDTO = LoginRequestDTO.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();
        when(authenticationManager.authenticate(any())).thenReturn(new TestingAuthenticationToken(null,
                null));
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> underTest.authenticateUser(loginRequestDTO));
        // then
        assertEquals(USER_BY_EMAIL_NOT_FOUND.getText(TEST_EMAIL), exception.getMessage());
    }
}