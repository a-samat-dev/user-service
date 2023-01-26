package kz.smarthealth.userservice.service;

import kz.smarthealth.userservice.model.dto.UserDTO;
import kz.smarthealth.userservice.model.dto.UserSearchDTO;
import kz.smarthealth.userservice.repository.elasticsearch.UserIndexRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class used to manipulate user index in elasticsearch
 *
 * Created by Samat Abibulla on 2023-01-26
 */
@Service
@Slf4j
@AllArgsConstructor
public class UserIndexService {

    private final UserIndexRepository userIndexRepository;

    public void indexUser(UserDTO userDTO) {
        try {
            userIndexRepository.save(userDTO);
        } catch (Exception e) {
            log.error("Unable to index user, id= {} email={}", userDTO.getId(), userDTO.getEmail());
            // todo need to keep ids of such users in redis, to retry indexing later
        }
    }

    public List<UserDTO> searchUser(UserSearchDTO searchDTO) {
        return List.of();
    }
}
