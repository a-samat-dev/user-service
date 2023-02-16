insert into roles
values (1, 'ROLE_ADMIN');
insert into roles
values (2, 'ROLE_ORGANIZATION');
insert into roles
values (3, 'ROLE_DOCTOR');
insert into roles
values (4, 'ROLE_PATIENT');

insert into contacts
values ('32af1e5a-e958-4d9e-bfb8-3bcc0a798a39', 1, 'street', '1', '1', '123456', '123456', current_timestamp(),
        current_timestamp(), 'admin@test.com', 'admin@test.com');

insert into contacts
values ('3f2cdc7b-9b1f-405e-a5a3-d3ad945e4cab', 1, 'street', '1', '1', '123456', '123456', current_timestamp(),
        current_timestamp(), 'admin@test.com', 'admin@test.com');

insert into contacts
values ('f6c66a9e-8954-470c-9a65-f10b37abc30d', 1, 'street', '1', '1', '123456', '123456', current_timestamp(),
        current_timestamp(), 'admin@test.com', 'admin@test.com');

insert into users (id, email, password, name, last_name, birth_date,
                   doctor_type_id, about, created_at, updated_at, created_by, updated_by)
values ('33bb9554-c616-42e6-a9c6-88d3bba4221c', 'admin@test.com',
        '$2a$10$Fv1.pLeI8jOaS8qN13vWWO60oLx.2yTQkDJssjcyssiuxjYeShnPm', 'Organization Name', null, null, null, 'About',
        current_timestamp(), current_timestamp(), 'admin@test.com', 'admin@test.com');

insert into users (id, email, password, name, last_name, birth_date,
                   doctor_type_id, about, contact_id, created_at, updated_at, created_by, updated_by)
values ('880f59da-853e-4445-83cc-3e1365c6db8f', 'org@test.com',
        '$2a$10$Fv1.pLeI8jOaS8qN13vWWO60oLx.2yTQkDJssjcyssiuxjYeShnPm', 'Organization Name', null, null, null, 'About',
        '32af1e5a-e958-4d9e-bfb8-3bcc0a798a39', current_timestamp(), current_timestamp(), 'org@test.com',
        'org@test.com');

insert into users (id, email, password, name, last_name, birth_date,
                   doctor_type_id, about, contact_id, created_at, updated_at, created_by, updated_by)
values ('91b0b677-7a3f-460b-bc0b-649ccd5c456e', 'doctor@test.com',
        '$2a$10$Fv1.pLeI8jOaS8qN13vWWO60oLx.2yTQkDJssjcyssiuxjYeShnPm', 'Organization Name', null, null, null, 'About',
        '3f2cdc7b-9b1f-405e-a5a3-d3ad945e4cab', current_timestamp(), current_timestamp(), 'doctor@test.com',
        'doctor@test.com');

insert into users (id, email, password, name, last_name, birth_date,
                   doctor_type_id, about, contact_id, created_at, updated_at, created_by, updated_by)
values ('d9eb4bf9-9727-4ef5-a7bc-c5c6bf8482ce', 'patient@test.com',
        '$2a$10$Fv1.pLeI8jOaS8qN13vWWO60oLx.2yTQkDJssjcyssiuxjYeShnPm', 'Organization Name', null, null, null, 'About',
        'f6c66a9e-8954-470c-9a65-f10b37abc30d', current_timestamp(), current_timestamp(), 'patient@test.com',
        'patient@test.com');

insert into user_roles
values ('33bb9554-c616-42e6-a9c6-88d3bba4221c', 1);

insert into user_roles
values ('880f59da-853e-4445-83cc-3e1365c6db8f', 2);

insert into user_roles
values ('91b0b677-7a3f-460b-bc0b-649ccd5c456e', 3);

insert into user_roles
values ('d9eb4bf9-9727-4ef5-a7bc-c5c6bf8482ce', 4);