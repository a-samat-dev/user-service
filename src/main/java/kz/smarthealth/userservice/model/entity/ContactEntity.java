package kz.smarthealth.userservice.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

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

    private Short cityId;

    private String street;

    private String buildingNumber;

    private String flatNumber;

    private String phoneNumber1;

    private String phoneNumber2;

    @OneToOne(mappedBy = "contact", fetch = FetchType.LAZY)
    @ToString.Exclude
    private UserEntity user;

    @Column(nullable = false)
    protected OffsetDateTime createdAt;

    @Column(nullable = false)
    protected OffsetDateTime updatedAt;

    @Column(nullable = false)
    protected String createdBy;

    @Column(nullable = false)
    protected String updatedBy;

    @Column(name = "deleted_at")
    protected OffsetDateTime deletedAt;

    @PrePersist
    private void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
        this.updatedAt = OffsetDateTime.now();
        if (this.createdBy == null) {
            this.createdBy = "system";
        }
        if (this.updatedBy == null) {
            this.updatedBy = "system";
        }
    }
}
