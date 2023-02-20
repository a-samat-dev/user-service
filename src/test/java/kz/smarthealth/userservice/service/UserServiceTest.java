package kz.smarthealth.userservice.service;

import kz.smarthealth.commonlogic.exception.CustomException;
import kz.smarthealth.userservice.model.RoleEnum;
import kz.smarthealth.userservice.model.dto.ContactDTO;
import kz.smarthealth.userservice.model.dto.LoginRequestDTO;
import kz.smarthealth.userservice.model.dto.UserDTO;
import kz.smarthealth.userservice.model.entity.RoleEntity;
import kz.smarthealth.userservice.model.entity.UserEntity;
import kz.smarthealth.userservice.repository.RoleRepository;
import kz.smarthealth.userservice.repository.UserRepository;
import kz.smarthealth.userservice.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static kz.smarthealth.userservice.util.MessageSource.USER_BY_EMAIL_NOT_FOUND;
import static kz.smarthealth.userservice.util.MessageSource.USER_BY_ID_NOT_FOUND;
import static kz.smarthealth.userservice.util.TestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
                .roles(Set.of(RoleEnum.ROLE_ORGANIZATION))
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
        userDTO.setRoles(Set.of(RoleEnum.ROLE_DOCTOR));
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
        assertEquals(RoleEnum.ROLE_DOCTOR, createdUserDTO.getRoles().iterator().next());
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

    @Test
    void getUserById_throwsError_whenUserNotFound() {
        // given
        UUID invalidId = UUID.randomUUID();
        when(userRepository.findById(invalidId)).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> underTest.getUserById(invalidId));
        // then
        assertEquals(USER_BY_ID_NOT_FOUND.getText(invalidId.toString()), exception.getMessage());
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

    @Test
    void updateUserById_throwsError_whenUserNotFound() {
        // given
        UUID invalidId = UUID.randomUUID();
        when(userRepository.findById(invalidId)).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> underTest.updateUserById(invalidId, UserDTO.builder().build()));
        // then
        assertEquals(USER_BY_ID_NOT_FOUND.getText(invalidId.toString()), exception.getMessage());
    }

    @Test
    void updateUserById_updatesUser() {
        // given
        ArgumentCaptor<UserEntity> argumentCaptor = ArgumentCaptor.forClass(UserEntity.class);
        UserEntity userEntity = getUserEntity();
        UUID id = UUID.randomUUID();
        userEntity.setId(id);
        UserDTO userDTO = UserDTO.builder()
                .name("UpdatedName")
                .lastName("UpdatedLastName")
                .birthDate(LocalDate.of(2000, 1, 1))
                .doctorTypeId((short) 2)
                .about("UpdatedAbout")
                .build();
        when(userRepository.findById(id)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any())).thenReturn(UserEntity.builder().build());
        // when
        UserDTO actualUserDTO = underTest.updateUserById(id, userDTO);
        // then
        verify(userRepository).save(argumentCaptor.capture());
        UserEntity updatedUserEntity = argumentCaptor.getValue();

        assertNotNull(actualUserDTO);
        assertNotNull(updatedUserEntity);
        assertEquals(userDTO.getName(), updatedUserEntity.getName());
        assertEquals(userDTO.getLastName(), updatedUserEntity.getLastName());
        assertEquals(userDTO.getBirthDate(), updatedUserEntity.getBirthDate());
        assertEquals(userDTO.getDoctorTypeId(), updatedUserEntity.getDoctorTypeId());
        assertEquals(userDTO.getAbout(), updatedUserEntity.getAbout());
    }

    @Test
    void deleteUserById_throwsError_whenUserNotFound() {
        // given
        UUID invalidId = UUID.randomUUID();
        when(userRepository.findById(invalidId)).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> underTest.deleteUserById(invalidId));
        // then
        assertEquals(USER_BY_ID_NOT_FOUND.getText(invalidId.toString()), exception.getMessage());
    }

    @Test
    void deleteUserById_deletesUser() {
        // given
        ArgumentCaptor<UserEntity> argumentCaptor = ArgumentCaptor.forClass(UserEntity.class);
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.of(UserEntity.builder().build()));
        // when
        underTest.deleteUserById(id);
        // then
        verify(userRepository).save(argumentCaptor.capture());
        UserEntity userEntity = argumentCaptor.getValue();

        assertNotNull(userEntity);
        assertNotNull(userEntity.getDeletedAt());
    }
}