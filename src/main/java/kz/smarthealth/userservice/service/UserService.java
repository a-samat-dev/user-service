package kz.smarthealth.userservice.service;

import kz.smarthealth.userservice.exception.CustomException;
import kz.smarthealth.userservice.model.RoleEnum;
import kz.smarthealth.userservice.model.dto.ContactDTO;
import kz.smarthealth.userservice.model.dto.LoginRequestDTO;
import kz.smarthealth.userservice.model.dto.LoginResponseDTO;
import kz.smarthealth.userservice.model.dto.UserDTO;
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
     * Checks if email is already in use
     *
     * @param email user email
     * @return true if user is available, otherwise false
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }

    /**
     * Creates new user
     *
     * @param userDTO user information to be created
     * @return newly created user
     */
    public UserDTO createUser(UserDTO userDTO) {
        validateEmail(userDTO.getEmail());
        UserEntity userEntity = modelMapper.map(userDTO, UserEntity.class);
        userEntity.setCreatedBy(userEntity.getEmail());
        userEntity.setUpdatedBy(userEntity.getEmail());
        userEntity.getContact().setCreatedBy(userEntity.getEmail());
        userEntity.getContact().setUpdatedBy(userEntity.getEmail());
        userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        userEntity.getContact().setUser(userEntity);
        userEntity.setRoles(getUserRoles(userDTO.getRoles()));
        userEntity = userRepository.save(userEntity);
        UserDTO createdUserDTO = modelMapper.map(userEntity, UserDTO.class);
        createdUserDTO.setRoles(userDTO.getRoles());

        return createdUserDTO;
    }

    /**
     * Checks if user email address is in use
     *
     * @param email address of user
     */
    private void validateEmail(String email) {
        userRepository.findByEmail(email)
                .ifPresent(entity -> {
                    throw CustomException.builder()
                            .httpStatus(HttpStatus.BAD_REQUEST)
                            .message(EMAIL_IN_USE.getText(email))
                            .build();
                });
    }

    /**
     * Fetches user roles by role names
     *
     * @param roles set of role names
     * @return set of role entity
     */
    private Set<RoleEntity> getUserRoles(Set<RoleEnum> roles) {
        Set<RoleEntity> roleEntitySet = new HashSet<>(roles.size());

        roles.forEach(role -> roleEntitySet.add(
                roleRepository.findByName(role.name())
                        .orElseThrow(() -> CustomException.builder()
                                .httpStatus(HttpStatus.BAD_REQUEST)
                                .message(ROLE_BY_NAME_NOT_FOUND.getText(role.name()))
                                .build())));

        return roleEntitySet;
    }

    /**
     * Authenticates user.
     *
     * @param loginRequestDTO user sign in information
     * @return access token and refresh token
     */
    @Transactional
    public LoginResponseDTO authenticateUser(LoginRequestDTO loginRequestDTO) {
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequestDTO.getEmail(), loginRequestDTO.getPassword()));
        UserEntity userEntity = userRepository.findByEmail(loginRequestDTO.getEmail())
                .orElseThrow(() -> CustomException.builder()
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .message(USER_BY_EMAIL_NOT_FOUND.getText(loginRequestDTO.getEmail()))
                        .build());
        String token = jwtUtils.generateJwtToken(authenticate);
        String refreshToken = jwtUtils.generateRefreshToken(authenticate);
        userEntity.setRefreshToken(refreshToken);
        userRepository.save(userEntity);
        UserDTO userDTO = modelMapper.map(userEntity, UserDTO.class);
        userDTO.setRoles(userEntity.getRoles().stream()
                .map(entity -> RoleEnum.valueOf(entity.getName()))
                .collect(Collectors.toSet()));

        return LoginResponseDTO.builder()
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
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> CustomException.builder()
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .message(USER_BY_ID_NOT_FOUND.getText(id.toString()))
                        .build());
        UserDTO userDTO = modelMapper.map(userEntity, UserDTO.class);
        userDTO.setContact(modelMapper.map(userEntity.getContact(), ContactDTO.class));
        userDTO.setRoles(userEntity.getRoles().stream()
                .map(entity -> RoleEnum.valueOf(entity.getName()))
                .collect(Collectors.toSet()));

        return userDTO;
    }
}
