package kz.smarthealth.userservice.controller;

import jakarta.validation.Valid;
import kz.smarthealth.commonlogic.util.AppConstants;
import kz.smarthealth.userservice.model.dto.LoginRequestDTO;
import kz.smarthealth.userservice.model.dto.LoginResponseDTO;
import kz.smarthealth.userservice.model.dto.UserDTO;
import kz.smarthealth.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST API to manipulate user data
 *
 * Created by Samat Abibulla on 2023-02-20
 */
@RestController
@RequestMapping(value = "/api/v1/users", produces = AppConstants.JSON_UTF_8)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Checks if email is available to use
     *
     * @param email user email address
     * @return result of email existence
     */
    @GetMapping("/email")
    public boolean isEmailAvailable(@RequestParam String email) {
        return userService.isEmailAvailable(email.toLowerCase());
    }

    /**
     * Creates new user
     *
     * @param userDTO user information
     * @return newly created user
     */
    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO createUser(@RequestBody @Valid UserDTO userDTO) {
        return userService.createUser(userDTO);
    }

    /**
     * Authenticates user.
     *
     * @param loginRequestDTO user sign in information
     * @return access token and refresh token
     */
    @PostMapping("/sign-in")
    public LoginResponseDTO authenticateUser(@RequestBody @Valid LoginRequestDTO loginRequestDTO) {
        return userService.authenticateUser(loginRequestDTO);
    }

    /**
     * Retrieves user by id
     *
     * @param id of user
     * @return user information
     */
    @Secured({"ROLE_ADMIN", "ROLE_ORGANIZATION", "ROLE_DOCTOR", "ROLE_PATIENT"})
    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable UUID id) {
        return userService.getUserById(id);
    }
}
