package kz.smarthealth.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import kz.smarthealth.userservice.exception.CustomException;
import kz.smarthealth.userservice.model.dto.*;
import kz.smarthealth.userservice.model.entity.ContactEntity;
import kz.smarthealth.userservice.model.entity.RoleEntity;
import kz.smarthealth.userservice.model.entity.UserEntity;
import kz.smarthealth.userservice.repository.ContactRepository;
import kz.smarthealth.userservice.repository.RoleRepository;
import kz.smarthealth.userservice.repository.UserRepository;
import kz.smarthealth.userservice.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static kz.smarthealth.userservice.util.MessageSource.*;
import static kz.smarthealth.userservice.util.TestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    private ContactRepository contactRepository;
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
    @Mock
    private AmazonS3Service amazonS3Service;
    @Mock
    private PatientKafkaProducerService patientKafkaProducerService;
    @InjectMocks
    private UserService underTest;

    @Test
    void signUp_throwsException_whenInvalidRoles() {
        // given
        UserDTO userDTO = UserDTO.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .name(TEST_NAME)
                .lastName(TEST_NAME)
                .contact(ContactDTO.builder()
                        .cityId(TEST_CITY)
                        .phoneNumber1(TEST_PHONE_NUMBER_1)
                        .build())
                .build();
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.signUp(userDTO));
        // then
        assertEquals("Invalid roles provided.", exception.getErrorMessage());
    }

    @Test
    void signUp_throwsException_whenEmailInUse() {
        // given
        UserDTO userDTO = UserDTO.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .name(TEST_NAME)
                .lastName(TEST_NAME)
                .contact(ContactDTO.builder()
                        .cityId(TEST_CITY)
                        .phoneNumber1(TEST_PHONE_NUMBER_1)
                        .build())
                .roles(Set.of(UserRole.ROLE_PATIENT))
                .build();
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Optional.of(UserEntity.builder().build()));
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.signUp(userDTO));
        // then
        assertEquals("test1@gmail.com is already in use, please provide another email address.",
                exception.getErrorMessage());
    }

    @Test
    void signUp_throwsException_whenInvalidRoleProvided() {
        // given
        UserDTO userDTO = UserDTO.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .name(TEST_NAME)
                .lastName(TEST_NAME)
                .contact(ContactDTO.builder()
                        .cityId(TEST_CITY)
                        .phoneNumber1(TEST_PHONE_NUMBER_1)
                        .build())
                .roles(Set.of(UserRole.ROLE_PATIENT))
                .build();
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByName(UserRole.ROLE_PATIENT.name())).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.signUp(userDTO));
        // then
        assertEquals(ROLE_BY_NAME_NOT_FOUND.getText(UserRole.ROLE_PATIENT.name()), exception.getErrorMessage());
    }

    @Test
    void signUp_createsUser() throws JsonProcessingException {
        // given
        ArgumentCaptor<UserEntity> userEntityArgumentCaptor = ArgumentCaptor.forClass(UserEntity.class);
        ArgumentCaptor<ContactEntity> contactEntityArgumentCaptor = ArgumentCaptor.forClass(ContactEntity.class);
        ArgumentCaptor<PatientDTO> patientDTOArgumentCaptor = ArgumentCaptor.forClass(PatientDTO.class);
        UserDTO userDTO = UserDTO.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .name(TEST_NAME)
                .lastName(TEST_LAST_NAME)
                .birthDate(TEST_BIRTH_DATE)
                .contact(ContactDTO.builder()
                        .cityId(TEST_CITY)
                        .phoneNumber1(TEST_PHONE_NUMBER_1)
                        .build())
                .roles(Set.of(UserRole.ROLE_PATIENT))
                .build();
        UserEntity userEntity = modelMapper.map(userDTO, UserEntity.class);
        userEntity.setId(UUID.randomUUID());
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn("encrypted_password");
        when(roleRepository.findByName(userDTO.getRoles().iterator().next().name()))
                .thenReturn(Optional.of(RoleEntity.builder()
                        .id((short) 1)
                        .name(userDTO.getRoles().iterator().next().name())
                        .build()));
        when(userRepository.save(any())).thenReturn(userEntity);
        // when
        underTest.signUp(userDTO);
        // then
        verify(userRepository).save(userEntityArgumentCaptor.capture());
        verify(contactRepository).save(contactEntityArgumentCaptor.capture());
        verify(patientKafkaProducerService).sendMessage(patientDTOArgumentCaptor.capture());
        UserEntity actualUserEntity = userEntityArgumentCaptor.getValue();
        ContactEntity actualContactEntity = contactEntityArgumentCaptor.getValue();
        PatientDTO actualPatientDTO = patientDTOArgumentCaptor.getValue();

        assertNotNull(actualUserEntity);
        assertEquals(TEST_EMAIL, actualUserEntity.getEmail());
        assertNotNull(actualUserEntity.getPassword());
        assertEquals(TEST_NAME, actualUserEntity.getName());
        assertEquals(TEST_LAST_NAME, actualUserEntity.getLastName());
        assertEquals(TEST_BIRTH_DATE, actualUserEntity.getBirthDate());
        assertEquals(Set.of(UserRole.ROLE_PATIENT), actualUserEntity.getRoles().stream()
                .map(role -> UserRole.valueOf(role.getName())).collect(Collectors.toSet()));
        assertNotNull(actualUserEntity.getContact());

        assertNotNull(actualContactEntity);
        assertEquals(TEST_CITY, actualContactEntity.getCityId());
        assertEquals(TEST_PHONE_NUMBER_1, actualContactEntity.getPhoneNumber1());

        assertNotNull(actualPatientDTO);
        assertNotNull(actualPatientDTO.getUserId());
        assertEquals(TEST_NAME, actualPatientDTO.getFirstName());
        assertEquals(TEST_LAST_NAME, actualPatientDTO.getLastName());
        assertEquals(TEST_BIRTH_DATE, actualPatientDTO.getBirthDate());
        assertEquals(TEST_PHONE_NUMBER_1, actualPatientDTO.getPhoneNumber());
    }

    @Test
    void signIn_throwsError_whenInvalidCredentialsProvided() {
        // given
        SignInDTO signInDTO = SignInDTO.builder()
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
        SignInDTO signInDTO = SignInDTO.builder()
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
    void signIn_successfullyAuthenticatesUser() {
        // given
        String token = "token";
        String refreshToken = "refresh-token";
        SignInDTO signInDTO = SignInDTO.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();
        UserEntity userEntity = getUserEntity();
        when(authenticationManager.authenticate(any())).thenReturn(new TestingAuthenticationToken(null,
                null));
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(userEntity));
        when(jwtUtils.generateJwtToken(any())).thenReturn(token);
        when(jwtUtils.generateRefreshToken(any())).thenReturn(refreshToken);
        // when
        SignInResponseDTO signInResponseDTO = underTest.signIn(signInDTO);
        // then
        ArgumentCaptor<UserEntity> argumentCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(argumentCaptor.capture());
        UserEntity updatedUserEntity = argumentCaptor.getValue();

        assertNotNull(signInResponseDTO);
        assertEquals(token, signInResponseDTO.getAccessToken());
        assertEquals(refreshToken, signInResponseDTO.getRefreshToken());
        assertNotNull(signInResponseDTO.getUser());
        assertFalse(StringUtils.isBlank(updatedUserEntity.getRefreshToken()));
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
        assertEquals(userEntity.getId(), userDTO.getId());
        assertEquals(userEntity.getEmail(), userDTO.getEmail());
        assertEquals(userEntity.getName(), userDTO.getName());
        assertEquals(userEntity.getLastName(), userDTO.getLastName());
        assertEquals(userEntity.getBirthDate(), userDTO.getBirthDate());
        assertEquals(userEntity.getAbout(), userDTO.getAbout());
        assertNotNull(userDTO.getContact());
        assertEquals(userEntity.getContact().getCreatedAt(), userDTO.getContact().getCreatedAt());
        assertEquals(userEntity.getContact().getId(), userDTO.getContact().getId());
        assertEquals(userEntity.getContact().getCityId(), userDTO.getContact().getCityId());
        assertEquals(userEntity.getContact().getStreet(), userDTO.getContact().getStreet());
        assertEquals(userEntity.getContact().getBuildingNumber(), userDTO.getContact().getBuildingNumber());
        assertEquals(userEntity.getContact().getFlatNumber(), userDTO.getContact().getFlatNumber());
        assertEquals(userEntity.getContact().getPhoneNumber1(), userDTO.getContact().getPhoneNumber1());
        assertEquals(userEntity.getContact().getPhoneNumber2(), userDTO.getContact().getPhoneNumber2());
    }

    @Test
    void uploadProfilePicture_throwsException_whenUserNotFound() {
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
    void uploadProfilePicture_throwsException_whenInvalidFileExtension() {
        // given
        UserEntity userEntity = getUserEntity();
        UUID id = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "file.pdf",
                "application/pdf", "some xml".getBytes());
        when(userRepository.findById(id)).thenReturn(Optional.of(userEntity));
        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> underTest.uploadProfilePicture(id, file));
        // then
        assertEquals(INVALID_PROFILE_PICTURE_FILE_EXTENSION.getText(id.toString()), exception.getErrorMessage());
    }

    @Test
    void uploadProfilePicture_throwsException_whenAmazonS3ClientThrowsException() {
        // given
        String expectedErrorMessage = "Unable to upload";
        UserEntity userEntity = getUserEntity();
        UUID id = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "file.jpeg",
                "image/jpeg", "some xml".getBytes());
        when(userRepository.findById(id)).thenReturn(Optional.of(userEntity));
        doThrow(
                CustomException.builder()
                        .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .error(HttpStatus.INTERNAL_SERVER_ERROR.name())
                        .errorMessage(expectedErrorMessage)
                        .build())
                .when(amazonS3Service).uploadUserProfilePicture(id.toString() + ".jpeg", file);
        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> underTest.uploadProfilePicture(id, file));
        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.name(), exception.getError());
        assertEquals(expectedErrorMessage, exception.getErrorMessage());
    }

    @Test
    void uploadProfilePicture_successfullyUploadsProfilePicture() {
        // given
        ArgumentCaptor<UserEntity> argumentCaptor = ArgumentCaptor.forClass(UserEntity.class);
        UserEntity userEntity = getUserEntity();
        UUID id = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "file.jpeg",
                "image/jpeg", "some xml".getBytes());
        when(userRepository.findById(id)).thenReturn(Optional.of(userEntity));
        // when
        underTest.uploadProfilePicture(id, file);
        // then
        verify(userRepository).save(argumentCaptor.capture());

        UserEntity updatedUserEntity = argumentCaptor.getValue();

        assertEquals(id + ".jpeg", updatedUserEntity.getProfilePictureFileName());
    }

    @Test
    void getProfilePicturePreSignedUrl_throwsException_whenUserNotFound() {
        // given
        UUID invalidId = UUID.randomUUID();
        when(userRepository.findById(invalidId)).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> underTest.getProfilePicturePreSignedUrl(invalidId));
        // then
        assertEquals(USER_BY_ID_NOT_FOUND.getText(invalidId.toString()), exception.getErrorMessage());
    }

    @Test
    void getProfilePicturePreSignedUrl_returnsPreSignedUrl() {
        // given
        String expectedPreSignedUrl = "http://pre-signed-url.com";
        UUID id = UUID.randomUUID();
        UserEntity userEntity = getUserEntity();
        userEntity.setProfilePictureFileName(id + ".jpeg");
        when(userRepository.findById(id)).thenReturn(Optional.of(userEntity));
        when(amazonS3Service.generateProfilePicturePreSignedUrl(userEntity.getProfilePictureFileName()))
                .thenReturn(expectedPreSignedUrl);
        // when
        String actualPreSignedUrl = underTest.getProfilePicturePreSignedUrl(id);
        // then
        assertEquals(expectedPreSignedUrl, actualPreSignedUrl);
    }
}