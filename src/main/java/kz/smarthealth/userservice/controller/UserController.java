package kz.smarthealth.userservice.controller;

import kz.smarthealth.userservice.model.dto.LoginRequestDTO;
import kz.smarthealth.userservice.model.dto.LoginResponseDTO;
import kz.smarthealth.userservice.model.dto.UserDTO;
import kz.smarthealth.userservice.service.UserService;
import kz.smarthealth.userservice.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST API to manipulate user data
 */
@RestController
@RequestMapping(value = "/api/v1/users", produces = AppConstants.JSON_UTF_8)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/email")
    public boolean isEmailAvailable(@RequestParam String email) {
        return userService.isEmailAvailable(email.toLowerCase());
    }

    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO createUser(@RequestBody @Valid UserDTO userDTO) {
        return userService.createUser(userDTO);
    }

    @PostMapping("/sign-in")
    public LoginResponseDTO authenticateUser(@RequestBody @Valid LoginRequestDTO loginRequestDTO) {
        return userService.authenticateUser(loginRequestDTO);
    }
}
