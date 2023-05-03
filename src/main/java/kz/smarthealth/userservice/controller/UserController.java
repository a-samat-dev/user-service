package kz.smarthealth.userservice.controller;

import jakarta.validation.Valid;
import kz.smarthealth.userservice.aop.Log;
import kz.smarthealth.userservice.model.dto.SignInResponseDTO;
import kz.smarthealth.userservice.model.dto.SignUpInDTO;
import kz.smarthealth.userservice.model.dto.UserDTO;
import kz.smarthealth.userservice.service.UserService;
import kz.smarthealth.userservice.util.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST API to manipulate user data
 *
 * Created by Samat Abibulla on 2023-02-20
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/users", produces = AppConstants.JSON_UTF_8)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Registers new user
     *
     * @param signUpInDTO user data
     * @return newly created user
     */
    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public SignUpInDTO signUp(@RequestBody @Valid SignUpInDTO signUpInDTO) {
        log.info("Incoming request to sign up, email={}", signUpInDTO.getEmail());
        return userService.signUp(signUpInDTO);
    }

    /**
     * Authenticates user.
     *
     * @param signUpInDTO user sign in information
     * @return access token and refresh token
     */
    @PostMapping("/sign-in")
    public SignInResponseDTO singIn(@RequestBody @Valid SignUpInDTO signUpInDTO) {
        log.info("Incoming request to sign in, email={}", signUpInDTO.getEmail());
        return userService.signIn(signUpInDTO);
    }

    /**
     * Retrieves user by id
     *
     * @param id of user
     * @return user information
     */
    @Log
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_ORGANIZATION') " +
            "or (authenticated and authentication.principal.username == #id.toString())")
    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable UUID id) {
        return userService.getUserById(id);
    }
}
