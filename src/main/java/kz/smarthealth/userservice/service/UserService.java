package kz.smarthealth.userservice.service;

import kz.smarthealth.userservice.exception.CustomException;
import kz.smarthealth.userservice.model.dto.*;
import kz.smarthealth.userservice.model.entity.ContactEntity;
import kz.smarthealth.userservice.model.entity.RoleEntity;
import kz.smarthealth.userservice.model.entity.UserEntity;
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

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    /**
     * Creates new user
     *
     * @param signUpInDTO user data
     * @return newly created user
     */
    @Transactional
    public SignUpInDTO signUp(SignUpInDTO signUpInDTO) {
        validateUserData(signUpInDTO);
        UserEntity userEntity = UserEntity.builder()
                .email(signUpInDTO.getEmail())
                .password(passwordEncoder.encode(signUpInDTO.getPassword()))
                .roles(getUserRoles(signUpInDTO.getRoles()))
                .contact(new ContactEntity())
                .build();
        userRepository.save(userEntity);
        signUpInDTO.setPassword(null);

        return signUpInDTO;
    }

    /**
     * Validates user data
     *
     * @param signUpInDTO user data to be created
     */
    private void validateUserData(SignUpInDTO signUpInDTO) {
        if (signUpInDTO.getRoles() == null || signUpInDTO.getRoles().isEmpty()) {
            throw CustomException.builder()
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .error(INVALID_ROLES.name())
                    .errorMessage(INVALID_ROLES.getText(signUpInDTO.getEmail()))
                    .build();
        }

        if (userRepository.findByEmail(signUpInDTO.getEmail()).isPresent()) {
            throw CustomException.builder()
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .error(EMAIL_IN_USE.name())
                    .errorMessage(EMAIL_IN_USE.getText(signUpInDTO.getEmail()))
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
        UserEntity userEntity = userRepository.findByEmail(signUpInDTO.getEmail())
                .orElseThrow(() -> CustomException.builder()
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
}
