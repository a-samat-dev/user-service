package kz.smarthealth.userservice.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kz.smarthealth.userservice.model.dto.*;
import kz.smarthealth.userservice.model.entity.UserEntity;
import kz.smarthealth.userservice.repository.UserRepository;
import kz.smarthealth.userservice.util.AppConstants;
import kz.smarthealth.userservice.util.LocalDateTypeConverter;
import kz.smarthealth.userservice.util.MessageSource;
import kz.smarthealth.userservice.util.OffsetDateTimeTypeConverter;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static kz.smarthealth.userservice.util.TestData.*;
import static org.junit.jupiter.api.Assertions.*;
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

    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeTypeConverter())
            .registerTypeAdapter(LocalDate.class, new LocalDateTypeConverter())
            .create();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void isEmailAvailable_returnsTrue_whenEmailNotInUse() throws Exception {
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/email?email="
                        + TEST_EMAIL))
                .andExpect(status().isOk()).andReturn();
        // then
        boolean result = Boolean.parseBoolean(mvcResult.getResponse().getContentAsString());
        assertTrue(result);
    }

    @Test
    void isEmailAvailable_returnsFalse_whenEmailInUse() throws Exception {
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/email?email="
                        + TEST_EXISTING_EMAIL))
                .andExpect(status().isOk()).andReturn();
        // then
        boolean result = Boolean.parseBoolean(mvcResult.getResponse().getContentAsString());
        assertFalse(result);
    }

    @Test
    void createUser_returnsBadRequest_whenUserMandatoryFieldsNotProvided() throws Exception {
        // given
        UserDTO userDTO = UserDTO.builder()
                .build();
        String requestBody = gson.toJson(userDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = gson.fromJson(mvcResult.getResponse().getContentAsString(),
                ErrorResponseDTO.class);
        Map<String, String> invalidFields = errorResponseDTO.getInvalidFields();

        assertEquals(4, invalidFields.size());
        assertTrue(invalidFields.containsKey("email"));
        assertTrue(invalidFields.containsKey("name"));
        assertTrue(invalidFields.containsKey("contact"));
        assertTrue(invalidFields.containsKey("roles"));
    }

    @Test
    void createUser_returnsBadRequest_whenContactMandatoryFieldsNotProvided() throws Exception {
        // given
        UserDTO userDTO = UserDTO.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .name(TEST_NAME)
                .contact(ContactDTO.builder()
                        .build())
                .roles(Set.of(TEST_ROLE_ORGANIZATION))
                .build();
        String requestBody = gson.toJson(userDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = gson.fromJson(mvcResult.getResponse().getContentAsString(),
                ErrorResponseDTO.class);
        Map<String, String> invalidFields = errorResponseDTO.getInvalidFields();

        assertEquals(2, invalidFields.size());
        assertTrue(invalidFields.containsKey("contact.cityId"));
        assertTrue(invalidFields.containsKey("contact.phoneNumber1"));
    }

    @Test
    void createUser_returnsBadRequest_whenEmailInUse() throws Exception {
        // given
        UserDTO userDTO = UserDTO.builder()
                .email(TEST_EXISTING_EMAIL)
                .password(TEST_PASSWORD)
                .name(TEST_NAME)
                .contact(ContactDTO.builder()
                        .cityId(TEST_CITY)
                        .phoneNumber1(TEST_PHONE_NUMBER_1)
                        .build())
                .roles(Set.of(TEST_ROLE_ORGANIZATION))
                .build();
        String requestBody = gson.toJson(userDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = gson.fromJson(mvcResult.getResponse().getContentAsString(),
                ErrorResponseDTO.class);
        assertEquals(errorResponseDTO.getError(), "Invalid email address");
        assertEquals(errorResponseDTO.getMessage(),
                "test@gmail.com is already in use, please provide another email address.");
    }

    @Test
    void createUser_returnsBadRequest_whenDoctorTypeIsNotProvided() throws Exception {
        // given
        UserDTO userDTO = UserDTO.builder()
                .email(TEST_EXISTING_EMAIL)
                .password(TEST_PASSWORD)
                .name(TEST_NAME)
                .contact(ContactDTO.builder()
                        .cityId(TEST_CITY)
                        .phoneNumber1(TEST_PHONE_NUMBER_1)
                        .build())
                .roles(Set.of(TEST_ROLE_DOCTOR))
                .build();
        String requestBody = gson.toJson(userDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = gson.fromJson(mvcResult.getResponse().getContentAsString(),
                ErrorResponseDTO.class);
        Map<String, String> invalidFields = errorResponseDTO.getInvalidFields();

        assertEquals(2, invalidFields.size());
        assertTrue(invalidFields.containsKey("birthDate"));
        assertTrue(invalidFields.containsKey("doctorTypeId"));
    }

    @Test
    void createUser_returnsCreatedOrganization() throws Exception {
        // given
        UserDTO userDTO = getUserDTO();
        String requestBody = gson.toJson(userDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isCreated()).andReturn();
        // then
        UserDTO createdUserDTO = gson.fromJson(mvcResult.getResponse().getContentAsString(),
                UserDTO.class);

        assertNotNull(createdUserDTO);
        assertNotNull(createdUserDTO.getId());
        assertNotNull(createdUserDTO.getCreatedAt());
        assertNotNull(createdUserDTO.getUpdatedAt());
        assertNotNull(createdUserDTO.getCreatedBy());
        assertNotNull(createdUserDTO.getUpdatedBy());
        assertEquals(TEST_EMAIL, createdUserDTO.getEmail());
        assertNull(createdUserDTO.getPassword());
        assertEquals(TEST_NAME, createdUserDTO.getName());
        assertEquals(TEST_LAST_NAME, createdUserDTO.getLastName());
        assertEquals(TEST_BIRTH_DATE, createdUserDTO.getBirthDate());
        assertEquals(TEST_DOCTOR_TYPE, createdUserDTO.getDoctorTypeId());
        assertEquals(TEST_ABOUT, createdUserDTO.getAbout());
        assertEquals(TEST_ROLE_ORGANIZATION, createdUserDTO.getRoles().iterator().next());

        assertNotNull(createdUserDTO.getContact());
        assertNotNull(createdUserDTO.getContact().getId());
        assertNotNull(createdUserDTO.getContact().getCreatedAt());
        assertNotNull(createdUserDTO.getContact().getUpdatedAt());
        assertNotNull(createdUserDTO.getContact().getCreatedBy());
        assertNotNull(createdUserDTO.getContact().getUpdatedBy());
        assertEquals(TEST_CITY, createdUserDTO.getContact().getCityId());
        assertEquals(TEST_STREET, createdUserDTO.getContact().getStreet());
        assertEquals(TEST_BUILDING_NUMBER, createdUserDTO.getContact().getBuildingNumber());
        assertEquals(TEST_FLAT_NUMBER, createdUserDTO.getContact().getFlatNumber());
        assertEquals(TEST_PHONE_NUMBER_1, createdUserDTO.getContact().getPhoneNumber1());
        assertEquals(TEST_PHONE_NUMBER_2, createdUserDTO.getContact().getPhoneNumber2());
    }

    @Test
    void authenticateUser_returnsBadRequest_whenEmailAndPasswordNotProvided() throws Exception {
        // given
        LoginRequestDTO loginRequestDTO = LoginRequestDTO.builder().build();
        String requestBody = gson.toJson(loginRequestDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = gson.fromJson(mvcResult.getResponse().getContentAsString(),
                ErrorResponseDTO.class);
        Map<String, String> invalidFields = errorResponseDTO.getInvalidFields();

        assertEquals(2, invalidFields.size());
        assertTrue(invalidFields.containsKey("email"));
        assertTrue(invalidFields.containsKey("password"));
    }

    @Test
    void authenticateUser_returnsUnauthorized_whenInvalidCredentialsProvided() throws Exception {
        // given
        LoginRequestDTO loginRequestDTO = LoginRequestDTO.builder()
                .email(TEST_EXISTING_EMAIL)
                .password("Invalid1!")
                .build();
        String requestBody = gson.toJson(loginRequestDTO);
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
        LoginRequestDTO loginRequestDTO = LoginRequestDTO.builder()
                .email(TEST_EXISTING_EMAIL)
                .password(TEST_PASSWORD)
                .build();
        String requestBody = gson.toJson(loginRequestDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        // then
        LoginResponseDTO loginResponseDTO = gson.fromJson(mvcResult.getResponse().getContentAsString(),
                LoginResponseDTO.class);

        assertNotNull(loginResponseDTO);
        assertFalse(StringUtils.isBlank(loginResponseDTO.getAccessToken()));
        assertFalse(StringUtils.isBlank(loginResponseDTO.getRefreshToken()));
        assertNotNull(loginResponseDTO.getUser());
    }

    @Test
    void getUserById_returnsNotFound_whenInvalidUserIdProvided() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isNotFound()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = gson.fromJson(mvcResult.getResponse().getContentAsString(),
                ErrorResponseDTO.class);

        assertNotNull(errorResponseDTO);
        assertEquals(HttpStatus.NOT_FOUND, errorResponseDTO.getStatus());
        assertEquals(MessageSource.USER_BY_ID_NOT_FOUND.getText(userId.toString()), errorResponseDTO.getMessage());
    }

    @Test
    void getUserById_returnsUser() throws Exception {
        // given
        UserEntity userEntity = getUserEntity();
        userRepository.save(userEntity);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(AppConstants.DEFAULT_OFFSET_DATE_TIME_FORMAT);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/" + userEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        // then
        userRepository.deleteById(userEntity.getId());
        UserDTO userDTO = gson.fromJson(mvcResult.getResponse().getContentAsString(), UserDTO.class);

        assertNotNull(userDTO);
        assertEquals(userEntity.getCreatedAt().format(formatter), userDTO.getCreatedAt().format(formatter));
        assertEquals(userEntity.getCreatedBy(), userDTO.getCreatedBy());
        assertEquals(userEntity.getUpdatedAt().format(formatter), userDTO.getUpdatedAt().format(formatter));
        assertEquals(userEntity.getUpdatedBy(), userDTO.getUpdatedBy());
        assertEquals(userEntity.getId(), userDTO.getId());
        assertEquals(userEntity.getEmail(), userDTO.getEmail());
        assertEquals(userEntity.getName(), userDTO.getName());
        assertEquals(userEntity.getLastName(), userDTO.getLastName());
        assertEquals(userEntity.getBirthDate(), userDTO.getBirthDate());
        assertEquals(userEntity.getAbout(), userDTO.getAbout());
        assertNotNull(userDTO.getContact());
        assertEquals(userEntity.getContact().getCreatedAt().format(formatter),
                userDTO.getContact().getCreatedAt().format(formatter));
        assertEquals(userEntity.getContact().getCreatedBy(), userDTO.getContact().getCreatedBy());
        assertEquals(userEntity.getContact().getUpdatedAt().format(formatter),
                userDTO.getContact().getUpdatedAt().format(formatter));
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
    void deleteUserById_returnsNotFound_whenInvalidUserIdProvided() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isNotFound()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = gson.fromJson(mvcResult.getResponse().getContentAsString(),
                ErrorResponseDTO.class);

        assertNotNull(errorResponseDTO);
        assertEquals(HttpStatus.NOT_FOUND, errorResponseDTO.getStatus());
        assertEquals(MessageSource.USER_BY_ID_NOT_FOUND.getText(userId.toString()), errorResponseDTO.getMessage());
    }

    @Test
    void deleteUserById_deletesUser() throws Exception {
        // given
        UserEntity userEntity = getUserEntity();
        userRepository.save(userEntity);
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/users/" + userEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isNoContent());
    }
}