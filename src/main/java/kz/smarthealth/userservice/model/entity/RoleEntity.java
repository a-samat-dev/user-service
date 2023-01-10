package kz.smarthealth.userservice.model.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleEntity {

    @Id
    private Short id;

    @Column(nullable = false)
    private String name;
}
