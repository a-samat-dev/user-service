package kz.smarthealth.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import kz.smarthealth.userservice.exception.CustomException;
import kz.smarthealth.userservice.model.dto.*;
import kz.smarthealth.userservice.model.entity.RoleEntity;
import kz.smarthealth.userservice.model.entity.UserEntity;
import kz.smarthealth.userservice.repository.ContactRepository;
import kz.smarthealth.userservice.repository.RoleRepository;
import kz.smarthealth.userservice.repository.UserRepository;
import kz.smarthealth.userservice.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static kz.smarthealth.userservice.util.MessageSource.*;

/**
 * Service class used to operate with user data
 *
 * Created by Samat Abibulla on 2022-10-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final Set<String> PROFILE_PICTURE_FILE_CONTENT_TYPES = Set.of("image/jpeg", "image/jpg", "image/png");

    private final UserRepository userRepository;
    private final ContactRepository contactRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AmazonS3Service amazonS3Service;
    private final PatientKafkaProducerService patientKafkaProducerService;

    /**
     * Creates new user
     *
     * @param userDTO user data
     * @return newly created user
     */
    @Transactional
    public void signUp(UserDTO userDTO) {
        validateUserData(userDTO);
        UserEntity userEntity = modelMapper.map(userDTO, UserEntity.class);
        userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        userEntity.setRoles(getUserRoles(userDTO.getRoles()));
        userEntity = userRepository.save(userEntity);
        userEntity.getContact().setUser(userEntity);
        contactRepository.save(userEntity.getContact());
        sendPatient(userEntity);
    }

    /**
     * Whenever new user is created, patient also should be created in patient-service
     *
     * @param userEntity newly created user
     */
    private void sendPatient(UserEntity userEntity) {
        try {
            patientKafkaProducerService.sendMessage(PatientDTO.builder()
                    .userId(userEntity.getId())
                    .firstName(userEntity.getName())
                    .lastName(userEntity.getLastName())
                    .birthDate(userEntity.getBirthDate())
                    .phoneNumber(userEntity.getContact().getPhoneNumber1())
                    .build());
        } catch (JsonProcessingException e) {
            // do nothing, the exception message will be handled by Spring AOP (See @Log annotation in aop package)
        }
    }

    /**
     * Validates user data
     *
     * @param userDTO user data to be created
     */
    private void validateUserData(UserDTO userDTO) {
        if (userDTO.getRoles() == null || userDTO.getRoles().isEmpty()) {
            throw CustomException.builder()
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .error(INVALID_ROLES.name())
                    .errorMessage(INVALID_ROLES.getText(userDTO.getEmail()))
                    .build();
        }

        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw CustomException.builder()
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .error(EMAIL_IN_USE.name())
                    .errorMessage(EMAIL_IN_USE.getText(userDTO.getEmail()))
                    .build();
        }
    }

    /**
     * Fetches user roles by role names
     *
     * @param roles set of role names
     * @return set of role entity
     */
    private Set<RoleEntity> getUserRoles(Set<UserRole> roles) {
        Set<RoleEntity> roleEntitySet = new HashSet<>(roles.size());

        roles.forEach(role -> roleEntitySet.add(
                roleRepository.findByName(role.name())
                        .orElseThrow(() -> CustomException.builder()
                                .httpStatus(HttpStatus.BAD_REQUEST)
                                .errorMessage(ROLE_BY_NAME_NOT_FOUND.getText(role.name()))
                                .build())));

        return roleEntitySet;
    }

    /**
     * Authenticates user.
     *
     * @param signUpInDTO user sign in information
     * @return access token and refresh token
     */
    @Transactional
    public SignInResponseDTO signIn(SignUpInDTO signUpInDTO) {
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                signUpInDTO.getEmail(), signUpInDTO.getPassword()));
        UserEntity userEntity = userRepository.findByEmail(signUpInDTO.getEmail()).orElseThrow(
                () -> CustomException.builder()
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .errorMessage(USER_BY_EMAIL_NOT_FOUND.getText(signUpInDTO.getEmail()))
                        .build());
        String token = jwtUtils.generateJwtToken(authenticate);
        String refreshToken = jwtUtils.generateRefreshToken(authenticate);
        userEntity.setRefreshToken(refreshToken);
        userRepository.save(userEntity);
        UserDTO userDTO = modelMapper.map(userEntity, UserDTO.class);
        userDTO.setRoles(userEntity.getRoles().stream()
                .map(entity -> UserRole.valueOf(entity.getName()))
                .collect(Collectors.toSet()));

        return SignInResponseDTO.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .user(userDTO)
                .build();
    }

    /**
     * Retrieves user by id
     *
     * @param id of user
     * @return user information
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID id) {
        UserEntity userEntity = findUserById(id);
        UserDTO userDTO = modelMapper.map(userEntity, UserDTO.class);
        userDTO.setRoles(userEntity.getRoles().stream()
                .map(entity -> UserRole.valueOf(entity.getName()))
                .collect(Collectors.toSet()));

        if (userEntity.getContact() != null) {
            userDTO.setContact(modelMapper.map(userEntity.getContact(), ContactDTO.class));
        }

        return userDTO;
    }

    /**
     * Retrieves user from DB
     *
     * @param id user id
     * @return user data if exists by id
     */
    private UserEntity findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> CustomException.builder()
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .errorMessage(USER_BY_ID_NOT_FOUND.getText(id.toString()))
                        .build());
    }

    /**
     * Saves user profile picture
     *
     * @param id   user id
     * @param file image file
     */
    public void uploadProfilePicture(UUID id, MultipartFile file) {
        UserEntity userEntity = findUserById(id);
        String fileContentType = getFileContentType(file);
        amazonS3Service.uploadUserProfilePicture(id.toString() + fileContentType, file);
        userEntity.setProfilePictureFileName(id + fileContentType);
        userRepository.save(userEntity);
    }

    /**
     * Identifies file extension, and returns it with '.' prefix
     *
     * @param file profile picture
     * @return file extension
     */
    private String getFileContentType(MultipartFile file) {
        String contentType = file.getContentType();

        if (contentType == null || !PROFILE_PICTURE_FILE_CONTENT_TYPES.contains(contentType)) {
            throw CustomException.builder()
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .error(HttpStatus.BAD_REQUEST.name())
                    .errorMessage(INVALID_PROFILE_PICTURE_FILE_EXTENSION.getText())
                    .build();
        }

        return contentType.contains("jpeg") || contentType.contains("jpg") ? ".jpeg" : ".png";
    }

    /**
     * Generates AWS S3 pre-signed url for user profile picture
     *
     * @param id user id
     * @return pre-signed url
     */
    public String getProfilePicturePreSignedUrl(UUID id) {
        UserEntity userEntity = findUserById(id);

        return amazonS3Service.generateProfilePicturePreSignedUrl(userEntity.getProfilePictureFileName());
    }
}
