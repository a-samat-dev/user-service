package kz.smarthealth.userservice.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kz.smarthealth.userservice.model.dto.*;
import kz.smarthealth.userservice.model.entity.UserEntity;
import kz.smarthealth.userservice.repository.ContactRepository;
import kz.smarthealth.userservice.repository.UserRepository;
import kz.smarthealth.userservice.service.PatientKafkaProducerService;
import kz.smarthealth.userservice.util.MessageSource;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.*;

import static kz.smarthealth.userservice.util.TestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link UserController}
 *
 * Created by Samat Abibulla on 11/9/2022
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
class UserControllerControllerIT {

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @MockBean
    private PatientKafkaProducerService patientKafkaProducerService;

    @MockBean
    private AmazonS3 amazonS3;

    @BeforeEach
    void beforeEach() throws JsonProcessingException {
        doNothing().when(patientKafkaProducerService).sendMessage(any());
    }

    @AfterEach
    void afterEach() {
        userRepository.findByEmail(TEST_EMAIL).ifPresent(entity -> {
            contactRepository.deleteById(entity.getContact().getId());
            userRepository.deleteById(entity.getId());
        });
    }

    @Test
    void signUp_returnsBadRequest_whenMandatoryFieldsNotProvided() throws Exception {
        // given
        UserDTO userDTO = UserDTO.builder().build();
        String requestBody = objectMapper.writeValueAsString(userDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                ErrorResponseDTO.class);
        Map<String, String> invalidFields = errorResponseDTO.getInvalidFields();

        assertEquals(5, invalidFields.size());
        assertTrue(invalidFields.containsKey("email"));
        assertTrue(invalidFields.containsKey("password"));
        assertTrue(invalidFields.containsKey("name"));
        assertTrue(invalidFields.containsKey("contact"));
        assertTrue(invalidFields.containsKey("roles"));
    }

    @Test
    void signUp_returnsBadRequest_whenContactMandatoryFieldsNotProvided() throws Exception {
        // given
        UserDTO userDTO = UserDTO.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .name(TEST_NAME)
                .birthDate(TEST_BIRTH_DATE)
                .contact(ContactDTO.builder()
                        .build())
                .roles(Set.of(UserRole.ROLE_PATIENT))
                .build();
        String requestBody = objectMapper.writeValueAsString(userDTO);
        requestBody = requestBody.substring(0, requestBody.length() - 1) +
                ",\"password\":" + "\"" + TEST_PASSWORD + "\"" + "}";
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                ErrorResponseDTO.class);
        Map<String, String> invalidFields = errorResponseDTO.getInvalidFields();

        assertEquals(2, invalidFields.size());
        assertTrue(invalidFields.containsKey("contact.cityId"));
        assertTrue(invalidFields.containsKey("contact.phoneNumber1"));
    }

    @Test
    void signUp_returnsBadRequest_whenEmailInUse() throws Exception {
        // given
        List<UserEntity> userEntityList = userRepository.findAll();
        UserDTO userDTO = UserDTO.builder()
                .email(userEntityList.get(0).getEmail())
                .password(TEST_PASSWORD)
                .name(TEST_NAME)
                .birthDate(TEST_BIRTH_DATE)
                .contact(ContactDTO.builder()
                        .cityId(TEST_CITY)
                        .phoneNumber1(TEST_PHONE_NUMBER_1)
                        .build())
                .roles(Set.of(UserRole.ROLE_PATIENT))
                .build();
        String requestBody = objectMapper.writeValueAsString(userDTO);
        requestBody = requestBody.substring(0, requestBody.length() - 1) +
                ",\"password\":" + "\"" + TEST_PASSWORD + "\"" + "}";
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                ErrorResponseDTO.class);

        assertNotNull(errorResponseDTO.getDateTime());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponseDTO.getCode());
        assertEquals(userEntityList.get(0).getEmail() + " is already in use, please provide another email address.",
                errorResponseDTO.getMessage());
    }

    @Test
    void signUp_returnsCreatedUser() throws Exception {
        // given
        UserDTO userDTO = UserDTO.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .name(TEST_NAME)
                .birthDate(TEST_BIRTH_DATE)
                .contact(ContactDTO.builder()
                        .cityId(TEST_CITY)
                        .phoneNumber1(TEST_PHONE_NUMBER_1)
                        .build())
                .roles(Set.of(UserRole.ROLE_PATIENT))
                .build();
        String requestBody = objectMapper.writeValueAsString(userDTO);
        requestBody = requestBody.substring(0, requestBody.length() - 1) +
                ",\"password\":" + "\"" + TEST_PASSWORD + "\"" + "}";
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/sign-up")
                        .contentType(MediaType.APPLICATION_JSON).content(requestBody).characterEncoding("utf-8"))
                .andExpect(status().isCreated());
    }

    @Test
    void authenticateUser_returnsBadRequest_whenMandatoryFieldsNotProvided() throws Exception {
        // given
        SignUpInDTO signUpInDTO = SignUpInDTO.builder().build();
        String requestBody = objectMapper.writeValueAsString(signUpInDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                ErrorResponseDTO.class);
        Map<String, String> invalidFields = errorResponseDTO.getInvalidFields();

        assertEquals(2, invalidFields.size());
        assertTrue(invalidFields.containsKey("email"));
        assertTrue(invalidFields.containsKey("password"));
    }

    @Test
    void authenticateUser_returnsUnauthorized_whenInvalidCredentialsProvided() throws Exception {
        // given
        SignUpInDTO signUpInDTO = SignUpInDTO.builder()
                .email(TEST_EXISTING_EMAIL)
                .password("Invalid1!")
                .build();
        String requestBody = objectMapper.writeValueAsString(signUpInDTO);
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isUnauthorized()).andReturn();
    }

    @Test
    void authenticateUser_returnsTokens() throws Exception {
        // given
        createUser(TEST_EMAIL, TEST_PASSWORD, UserRole.ROLE_PATIENT);
        SignUpInDTO signUpInDTO = SignUpInDTO.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();
        String requestBody = objectMapper.writeValueAsString(signUpInDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        // then
        SignInResponseDTO signInResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                SignInResponseDTO.class);

        assertNotNull(signInResponseDTO);
        assertFalse(StringUtils.isBlank(signInResponseDTO.getAccessToken()));
        assertFalse(StringUtils.isBlank(signInResponseDTO.getRefreshToken()));
        assertNotNull(signInResponseDTO.getUser());
    }

    @Test
    void getUserById_forbidden_whenAccessRestricted() throws Exception {
        // given
        UUID invalidUserId = UUID.randomUUID();
        createUser(TEST_EMAIL, TEST_PASSWORD, UserRole.ROLE_PATIENT);
        UserEntity userEntity = userRepository.findByEmail(TEST_EMAIL).get();
        // when
        this.mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/users/" + invalidUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("userId", userEntity.getId().toString())
                        .header("role", UserRole.ROLE_PATIENT)
                        .characterEncoding("utf-8"))
                .andExpect(status().isForbidden()).andReturn();
    }

    @Test
    void getUserById_notFound_whenInvalidUserIdProvided() throws Exception {
        // given
        UUID invalidUserId = UUID.randomUUID();
        createUser(TEST_EMAIL, TEST_PASSWORD, UserRole.ROLE_DOCTOR);
        UserEntity userEntity = userRepository.findByEmail(TEST_EMAIL).get();
        // when
        MvcResult mvcResult = this.mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/users/" + invalidUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("userId", userEntity.getId().toString())
                        .header("role", UserRole.ROLE_DOCTOR)
                        .characterEncoding("utf-8"))
                .andExpect(status().isNotFound()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                ErrorResponseDTO.class);

        assertNotNull(errorResponseDTO.getDateTime());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponseDTO.getCode());
        assertEquals(MessageSource.USER_BY_ID_NOT_FOUND.getText(invalidUserId.toString()),
                errorResponseDTO.getMessage());
    }

    @Test
    void getUserById_returnsUserData() throws Exception {
        // given
        createUser(TEST_EMAIL, TEST_PASSWORD, UserRole.ROLE_PATIENT);
        UserEntity userEntity = userRepository.findByEmail(TEST_EMAIL).get();
        // when
        MvcResult mvcResult = this.mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/users/" + userEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("userId", userEntity.getId().toString())
                        .header("role", UserRole.ROLE_PATIENT)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        // then
        UserDTO userDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                UserDTO.class);
        Map<String, Object> map = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<HashMap<String, Object>>() {
                });

        assertNotNull(userDTO);
        assertNotNull(map.get("createdAt"));
        assertEquals(userEntity.getId().toString(), map.get("id").toString());
        assertEquals(userEntity.getEmail(), userDTO.getEmail());
        assertNull(map.get("password"));
        assertTrue(userDTO.getRoles().contains(UserRole.valueOf(userEntity.getRoles().iterator().next().getName())));
        assertNotNull(userEntity.getContact());
    }

    @Test
    void uploadProfilePicture_returnsForbidden() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile("file", "original_filename.jpeg",
                MediaType.IMAGE_JPEG_VALUE, "data".getBytes());
        createUser(TEST_EMAIL, TEST_PASSWORD, UserRole.ROLE_PATIENT);
        UserEntity userEntity = userRepository.findByEmail(TEST_EMAIL).get();
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/users/" + UUID.randomUUID() + "/profile-picture")
                        .file(file)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("userId", userEntity.getId().toString())
                        .header("role", UserRole.ROLE_PATIENT)
                        .contentType(MediaType.IMAGE_JPEG_VALUE)
                        .content(file.getBytes())
                        .characterEncoding("utf-8"))
                .andExpect(status().isForbidden()).andReturn();
    }

    @Test
    void uploadProfilePicture_uploadsPicture() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile("file", "original_filename.jpeg",
                MediaType.IMAGE_JPEG_VALUE, "data".getBytes());
        createUser(TEST_EMAIL, TEST_PASSWORD, UserRole.ROLE_PATIENT);
        UserEntity userEntity = userRepository.findByEmail(TEST_EMAIL).get();
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/users/" + userEntity.getId() + "/profile-picture")
                        .file(file)
                        .header("userId", userEntity.getId().toString())
                        .header("role", UserRole.ROLE_PATIENT)
                        .contentType(MediaType.IMAGE_JPEG_VALUE)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        // then
        userEntity = userRepository.findByEmail(TEST_EMAIL).get();

        assertFalse(StringUtils.isEmpty(userEntity.getProfilePictureFileName()));
    }

    @Test
    void getProfilePicturePreSignedUrl() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile("file", "original_filename.jpeg",
                MediaType.IMAGE_JPEG_VALUE, "data".getBytes());
        createUser(TEST_EMAIL, TEST_PASSWORD, UserRole.ROLE_PATIENT);
        UserEntity userEntity = userRepository.findByEmail(TEST_EMAIL).get();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, 1);
        when(amazonS3.doesObjectExist(any(), any())).thenReturn(false);
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/users/" + userEntity.getId() + "/profile-picture")
                        .file(file)
                        .header("userId", userEntity.getId().toString())
                        .header("role", UserRole.ROLE_PATIENT)
                        .contentType(MediaType.IMAGE_JPEG_VALUE)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/" + userEntity.getId() + "/profile-picture")
                        .header("userId", userEntity.getId().toString())
                        .header("role", UserRole.ROLE_PATIENT))
                .andExpect(status().isOk()).andReturn();

        // then
        String preSignedUrl = mvcResult.getResponse().getContentAsString();

        assertTrue(StringUtils.isEmpty(preSignedUrl));
    }

    private void createUser(String email, String password, UserRole role) throws Exception {
        UserDTO userDTO = UserDTO.builder()
                .email(email)
                .name(TEST_NAME)
                .birthDate(TEST_BIRTH_DATE)
                .contact(ContactDTO.builder()
                        .cityId(TEST_CITY)
                        .phoneNumber1(TEST_PHONE_NUMBER_1)
                        .build())
                .roles(Set.of(role))
                .doctorTypeId(TEST_DOCTOR_TYPE)
                .build();
        String requestBody = objectMapper.writeValueAsString(userDTO);
        requestBody = requestBody.substring(0, requestBody.length() - 1) +
                ",\"password\":" + "\"" + password + "\"" + "}";
        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/sign-up")
                        .contentType(MediaType.APPLICATION_JSON).content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isCreated());
    }
}