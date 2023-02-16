package kz.smarthealth.userservice.util;

import kz.smarthealth.userservice.model.RoleEnum;
import kz.smarthealth.userservice.model.dto.ContactDTO;
import kz.smarthealth.userservice.model.dto.UserDTO;
import kz.smarthealth.userservice.model.entity.ContactEntity;
import kz.smarthealth.userservice.model.entity.RoleEntity;
import kz.smarthealth.userservice.model.entity.UserEntity;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;

/**
 * Singleton class that holds user test data
 * <p>
 * Created by Samat Abibulla on 2022-10-13
 */
public final class TestData {

    private TestData() {
    }

    public static final String TEST_EXISTING_EMAIL = "org@test.com";
    public static final String TEST_EMAIL = "test1@gmail.com";
    public static final String TEST_PASSWORD = "Qwerty1!";
    public static final String TEST_NAME = "first name";
    public static final String TEST_LAST_NAME = "last name";
    public static final LocalDate TEST_BIRTH_DATE = LocalDate.of(2000, 1, 1);
    public static final short TEST_DOCTOR_TYPE = 1;
    public static final String TEST_ABOUT = "about";

    public static final Short TEST_CITY = 1;
    public static final String TEST_STREET = "street";
    public static final String TEST_BUILDING_NUMBER = "building number";
    public static final String TEST_FLAT_NUMBER = "flat number";
    public static final String TEST_PHONE_NUMBER_1 = "phone number 1";
    public static final String TEST_PHONE_NUMBER_2 = "phone number 2";
    public static final String TEST_ROLE_ORGANIZATION = "ROLE_ORGANIZATION";
    public static final String TEST_ROLE_DOCTOR = "ROLE_DOCTOR";

    public static UserDTO getUserDTO() {
        return UserDTO.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .name(TEST_NAME)
                .lastName(TEST_LAST_NAME)
                .birthDate(TEST_BIRTH_DATE)
                .doctorTypeId(TEST_DOCTOR_TYPE)
                .about(TEST_ABOUT)
                .contact(getContactDTO())
                .roles(Set.of(RoleEnum.ROLE_ORGANIZATION))
                .build();
    }

    public static ContactDTO getContactDTO() {
        return ContactDTO.builder()
                .cityId(TEST_CITY)
                .street(TEST_STREET)
                .buildingNumber(TEST_BUILDING_NUMBER)
                .flatNumber(TEST_FLAT_NUMBER)
                .phoneNumber1(TEST_PHONE_NUMBER_1)
                .phoneNumber2(TEST_PHONE_NUMBER_2)
                .build();
    }

    public static UserEntity getUserEntity() {
        UserEntity userEntity = UserEntity.builder()
                .createdAt(OffsetDateTime.now().minusDays(7))
                .createdBy(TEST_EMAIL)
                .updatedAt(OffsetDateTime.now().minusDays(7))
                .updatedBy(TEST_EMAIL)
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .name(TEST_NAME)
                .lastName(TEST_LAST_NAME)
                .birthDate(TEST_BIRTH_DATE)
                .about(TEST_ABOUT)
                .contact(getContactEntity())
                .roles(Set.of(RoleEntity.builder()
                        .id((short) 1)
                        .name(RoleEnum.ROLE_PATIENT.name())
                        .build()))
                .build();
        userEntity.getContact().setUser(userEntity);

        return userEntity;
    }

    public static ContactEntity getContactEntity() {
        return ContactEntity.builder()
                .createdAt(OffsetDateTime.now().minusDays(7))
                .createdBy(TEST_EMAIL)
                .updatedAt(OffsetDateTime.now().minusDays(7))
                .updatedBy(TEST_EMAIL)
                .cityId(TEST_CITY)
                .street(TEST_STREET)
                .buildingNumber(TEST_BUILDING_NUMBER)
                .flatNumber(TEST_FLAT_NUMBER)
                .phoneNumber1(TEST_PHONE_NUMBER_1)
                .phoneNumber2(TEST_PHONE_NUMBER_2)
                .build();
    }
}
