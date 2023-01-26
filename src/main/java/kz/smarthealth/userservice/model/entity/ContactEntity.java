package kz.smarthealth.userservice.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entity represents user
 *
 * Created by Samat Abibulla 5/10/22
 */
@Entity
@Data
@Table(name = "contacts")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(nullable = false)
    private Short cityId;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String buildingNumber;

    private String flatNumber;

    @Column(nullable = false)
    private String phoneNumber1;

    private String phoneNumber2;

    @OneToOne(mappedBy = "contact")
    private UserEntity user;

    @Column(nullable = false)
    protected OffsetDateTime createdAt;

    @Column(nullable = false)
    protected OffsetDateTime updatedAt;

    @Column(nullable = false)
    protected String createdBy;

    @Column(nullable = false)
    protected String updatedBy;

    @PrePersist
    private void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
        this.updatedAt = OffsetDateTime.now();
    }
}
