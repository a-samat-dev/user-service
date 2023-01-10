insert into roles
values (1, 'ORGANIZATION');
insert into roles
values (2, 'DOCTOR');
insert into roles
values (3, 'PATIENT');

insert into users (id, created_at, updated_at, created_by, updated_by, email, password, name, last_name, birth_date,
                   doctor_type_id, about)
values ('33bb9554-c616-42e6-a9c6-88d3bba4221c', current_timestamp(), current_timestamp(), 'test@gmail.com',
        'test@gmail.com', 'test@gmail.com', '$2a$10$Fv1.pLeI8jOaS8qN13vWWO60oLx.2yTQkDJssjcyssiuxjYeShnPm',
        'Organization Name', null, null, null, 'About');

insert into user_roles
values ('33bb9554-c616-42e6-a9c6-88d3bba4221c', 1);