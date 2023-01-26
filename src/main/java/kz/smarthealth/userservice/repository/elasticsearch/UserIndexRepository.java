package kz.smarthealth.userservice.repository.elasticsearch;

import kz.smarthealth.userservice.model.dto.UserDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserIndexRepository extends ElasticsearchRepository<UserDTO, String> {
}
