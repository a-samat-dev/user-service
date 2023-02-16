package kz.smarthealth.userservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kz.smarthealth.userservice.model.RoleEnum;
import kz.smarthealth.userservice.model.dto.*;
import kz.smarthealth.userservice.model.entity.UserEntity;
import kz.smarthealth.userservice.repository.UserRepository;
import kz.smarthealth.userservice.util.MessageSource;
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

import java.util.List;
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

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

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
    void createUser_returnsBadRequest_whenMandatoryFieldsNotProvided() throws Exception {
        // given
        UserDTO userDTO = UserDTO.builder()
                .build();
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

        assertEquals(4, invalidFields.size());
        assertTrue(invalidFields.containsKey("email"));
        assertTrue(invalidFields.containsKey("name"));
        assertTrue(invalidFields.containsKey("contact"));
        assertTrue(invalidFields.containsKey("roles"));
    }

    @Test
    void createUser_returnsBadRequest_whenDoctorMandatoryFieldsNotProvided() throws Exception {
        // given
        UserDTO userDTO = UserDTO.builder()
                .roles(Set.of(RoleEnum.ROLE_DOCTOR))
                .build();
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
        assertTrue(invalidFields.containsKey("name"));
        assertTrue(invalidFields.containsKey("contact"));
        assertTrue(invalidFields.containsKey("birthDate"));
        assertTrue(invalidFields.containsKey("doctorTypeId"));
    }

    @Test
    void createUser_returnsBadRequest_whenPatientMandatoryFieldsNotProvided() throws Exception {
        // given
        UserDTO userDTO = UserDTO.builder()
                .roles(Set.of(RoleEnum.ROLE_PATIENT))
                .build();
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

        assertEquals(4, invalidFields.size());
        assertTrue(invalidFields.containsKey("email"));
        assertTrue(invalidFields.containsKey("name"));
        assertTrue(invalidFields.containsKey("contact"));
        assertTrue(invalidFields.containsKey("birthDate"));
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
                .roles(Set.of(RoleEnum.ROLE_ORGANIZATION))
                .build();
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

        assertEquals(2, invalidFields.size());
        assertTrue(invalidFields.containsKey("contact.cityId"));
        assertTrue(invalidFields.containsKey("contact.phoneNumber1"));
    }

    @Test
    void createUser_returnsBadRequest_whenEmailInUse() throws Exception {
        // given
        List<UserEntity> userEntityList = userRepository.findAll();
        UserDTO userDTO = UserDTO.builder()
                .email(userEntityList.get(0).getEmail())
                .password(TEST_PASSWORD)
                .name(TEST_NAME)
                .contact(ContactDTO.builder()
                        .cityId(TEST_CITY)
                        .phoneNumber1(TEST_PHONE_NUMBER_1)
                        .build())
                .roles(Set.of(RoleEnum.ROLE_ORGANIZATION))
                .build();
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

        assertNotNull(errorResponseDTO.getDateTime());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponseDTO.getCode());
        assertEquals(userEntityList.get(0).getEmail() + " is already in use, please provide another email address.",
                errorResponseDTO.getMessage());
    }

    @Test
    void createUser_returnsCreatedOrganization() throws Exception {
        // given
        UserDTO userDTO = getUserDTO();
        String requestBody = objectMapper.writeValueAsString(userDTO);
        requestBody = requestBody.replaceFirst("\"id\":null", "\"password\":\"" + TEST_PASSWORD + "\"");
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isCreated()).andReturn();
        // then
        UserDTO createdUserDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserDTO.class);
        Map<String, Object> userDTOMap = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        assertEquals(TEST_EMAIL, createdUserDTO.getEmail());
        assertNull(createdUserDTO.getPassword());
        assertEquals(TEST_NAME, createdUserDTO.getName());
        assertEquals(TEST_LAST_NAME, createdUserDTO.getLastName());
        assertEquals(TEST_BIRTH_DATE, createdUserDTO.getBirthDate());
        assertEquals(TEST_DOCTOR_TYPE, createdUserDTO.getDoctorTypeId());
        assertEquals(TEST_ABOUT, createdUserDTO.getAbout());
        assertEquals(RoleEnum.ROLE_ORGANIZATION, createdUserDTO.getRoles().iterator().next());
        assertNotNull(createdUserDTO.getContact());
        assertEquals(TEST_CITY, createdUserDTO.getContact().getCityId());
        assertEquals(TEST_STREET, createdUserDTO.getContact().getStreet());
        assertEquals(TEST_BUILDING_NUMBER, createdUserDTO.getContact().getBuildingNumber());
        assertEquals(TEST_FLAT_NUMBER, createdUserDTO.getContact().getFlatNumber());
        assertEquals(TEST_PHONE_NUMBER_1, createdUserDTO.getContact().getPhoneNumber1());
        assertEquals(TEST_PHONE_NUMBER_2, createdUserDTO.getContact().getPhoneNumber2());

        assertNotNull(userDTOMap.get("id"));
        assertNotNull(userDTOMap.get("createdAt"));
        assertNotNull(userDTOMap.get("createdBy"));
        assertNotNull(userDTOMap.get("updatedAt"));
        assertNotNull(userDTOMap.get("updatedBy"));

        try {
            Map<String, Object> contactDTOMap = (Map<String, Object>) userDTOMap.get("contact");
            assertNotNull(contactDTOMap.get("id"));
            assertNotNull(contactDTOMap.get("createdAt"));
            assertNotNull(contactDTOMap.get("createdBy"));
            assertNotNull(contactDTOMap.get("updatedAt"));
            assertNotNull(contactDTOMap.get("updatedBy"));
        } catch (ClassCastException ex) {
            fail();
        }
    }

    @Test
    void authenticateUser_returnsBadRequest_whenEmailAndPasswordNotProvided() throws Exception {
        // given
        LoginRequestDTO loginRequestDTO = LoginRequestDTO.builder().build();
        String requestBody = objectMapper.writeValueAsString(loginRequestDTO);
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
        LoginRequestDTO loginRequestDTO = LoginRequestDTO.builder()
                .email(TEST_EXISTING_EMAIL)
                .password("Invalid1!")
                .build();
        String requestBody = objectMapper.writeValueAsString(loginRequestDTO);
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
        String requestBody = objectMapper.writeValueAsString(loginRequestDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        // then
        LoginResponseDTO loginResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
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
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                ErrorResponseDTO.class);

        assertNotNull(errorResponseDTO);
        assertEquals(MessageSource.USER_BY_ID_NOT_FOUND.getText(userId.toString()), errorResponseDTO.getMessage());
    }

    @Test
    void getUserById_returnsUser() throws Exception {
        // given
        UserEntity userEntity = getUserEntity();
        userEntity = userRepository.save(userEntity);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/"
                                + userEntity.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        // then
        userRepository.deleteById(userEntity.getId());
        UserDTO userDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserDTO.class);
        Map<String, Object> userDTOMap = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        assertEquals(userEntity.getEmail(), userDTO.getEmail());
        assertNull(userDTO.getPassword());
        assertEquals(userEntity.getName(), userDTO.getName());
        assertEquals(userEntity.getLastName(), userDTO.getLastName());
        assertEquals(userEntity.getBirthDate(), userDTO.getBirthDate());
        assertEquals(userEntity.getAbout(), userDTO.getAbout());
        assertNotNull(userDTO.getContact());
        assertEquals(userEntity.getContact().getCityId(), userDTO.getContact().getCityId());
        assertEquals(userEntity.getContact().getStreet(), userDTO.getContact().getStreet());
        assertEquals(userEntity.getContact().getBuildingNumber(), userDTO.getContact().getBuildingNumber());
        assertEquals(userEntity.getContact().getFlatNumber(), userDTO.getContact().getFlatNumber());
        assertEquals(userEntity.getContact().getPhoneNumber1(), userDTO.getContact().getPhoneNumber1());
        assertEquals(userEntity.getContact().getPhoneNumber2(), userDTO.getContact().getPhoneNumber2());

        assertNotNull(userDTOMap.get("id"));
        assertNotNull(userDTOMap.get("createdAt"));
        assertNotNull(userDTOMap.get("createdBy"));
        assertNotNull(userDTOMap.get("updatedAt"));
        assertNotNull(userDTOMap.get("updatedBy"));

        try {
            Map<String, Object> contactDTOMap = (Map<String, Object>) userDTOMap.get("contact");
            assertNotNull(contactDTOMap.get("id"));
            assertNotNull(contactDTOMap.get("createdAt"));
            assertNotNull(contactDTOMap.get("createdBy"));
            assertNotNull(contactDTOMap.get("updatedAt"));
            assertNotNull(contactDTOMap.get("updatedBy"));
        } catch (ClassCastException ex) {
            fail();
        }
    }
}