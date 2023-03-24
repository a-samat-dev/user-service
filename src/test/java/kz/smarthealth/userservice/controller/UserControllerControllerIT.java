package kz.smarthealth.userservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kz.smarthealth.userservice.model.dto.*;
import kz.smarthealth.userservice.model.entity.UserEntity;
import kz.smarthealth.userservice.repository.RoleRepository;
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

import java.util.*;

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

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void signUp_returnsBadRequest_whenMandatoryFieldsNotProvided() throws Exception {
        // given
        SignUpInDTO signUpInDTO = SignUpInDTO.builder().build();
        String requestBody = objectMapper.writeValueAsString(signUpInDTO);
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
        assertTrue(invalidFields.containsKey("email"));
        assertTrue(invalidFields.containsKey("password"));
    }

    @Test
    void signUp_returnsBadRequest_whenInvalidRoles() throws Exception {
        // given
        SignUpInDTO signUpInDTO = SignUpInDTO.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();
        String requestBody = objectMapper.writeValueAsString(signUpInDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                ErrorResponseDTO.class);

        assertNotNull(errorResponseDTO.getMessage());
        assertEquals("Invalid roles provided.", errorResponseDTO.getMessage());
    }

    @Test
    void signUp_returnsBadRequest_whenEmailInUse() throws Exception {
        // given
        List<UserEntity> userEntityList = userRepository.findAll();
        SignUpInDTO signUpInDTO = SignUpInDTO.builder()
                .email(userEntityList.get(0).getEmail())
                .password(TEST_PASSWORD)
                .roles(Set.of(RoleEnum.ROLE_PATIENT))
                .build();
        String requestBody = objectMapper.writeValueAsString(signUpInDTO);
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
        SignUpInDTO signUpInDTO = SignUpInDTO.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .roles(Set.of(RoleEnum.ROLE_PATIENT))
                .build();
        String requestBody = objectMapper.writeValueAsString(signUpInDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/sign-up")
                        .contentType(MediaType.APPLICATION_JSON).content(requestBody).characterEncoding("utf-8"))
                .andExpect(status().isCreated()).andReturn();
        // then
        SignUpInDTO createdSignUpInDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                SignUpInDTO.class);

        assertNotNull(createdSignUpInDTO);
        assertEquals(signUpInDTO.getEmail(), createdSignUpInDTO.getEmail());
        assertNull(createdSignUpInDTO.getPassword());
        assertEquals(signUpInDTO.getRoles(), createdSignUpInDTO.getRoles());
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
        UserEntity userEntity = UserEntity.builder()
                .email("temp@test.com")
                .password("$2a$10$Fv1.pLeI8jOaS8qN13vWWO60oLx.2yTQkDJssjcyssiuxjYeShnPm")
                .roles(Set.of(roleRepository.findAll().get(2)))
                .build();
        userRepository.save(userEntity);
        SignUpInDTO signUpInDTO = SignUpInDTO.builder()
                .email(userEntity.getEmail())
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
        userRepository.deleteById(userEntity.getId());
        SignInResponseDTO signInResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                SignInResponseDTO.class);

        assertNotNull(signInResponseDTO);
        assertFalse(StringUtils.isBlank(signInResponseDTO.getAccessToken()));
        assertFalse(StringUtils.isBlank(signInResponseDTO.getRefreshToken()));
        assertNotNull(signInResponseDTO.getUser());
    }

    @Test
    void getUserById_notFound_whenInvalidUserIdProvided() throws Exception {
        // given
        UUID invalidUserId = UUID.randomUUID();
        // when
        MvcResult mvcResult = this.mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/users/" + invalidUserId)
                        .contentType(MediaType.APPLICATION_JSON)
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
        UserEntity userEntity = userRepository.findAll().get(0);
        // when
        MvcResult mvcResult = this.mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/users/" + userEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON)
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
        assertNotNull(map.get("createdBy"));
        assertNotNull(map.get("updatedAt"));
        assertNotNull(map.get("updatedBy"));
        assertEquals(userEntity.getId().toString(), map.get("id").toString());
        assertEquals(userEntity.getEmail(), userDTO.getEmail());
        assertNull(map.get("password"));
        assertTrue(userDTO.getRoles().contains(RoleEnum.valueOf(userEntity.getRoles().iterator().next().getName())));
    }
}