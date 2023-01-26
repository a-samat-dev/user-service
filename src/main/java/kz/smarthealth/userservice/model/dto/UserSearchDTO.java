package kz.smarthealth.userservice.model.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserSearchDTO {

    public UUID id;
    public String email;
    public String name;
    public String lastName;
}
