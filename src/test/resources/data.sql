insert into roles
values (1, 'ROLE_ADMIN');
insert into roles
values (2, 'ROLE_ORGANIZATION');
insert into roles
values (3, 'ROLE_DOCTOR');
insert into roles
values (4, 'ROLE_PATIENT');

insert into users(id, email, password, created_at, updated_at, created_by, updated_by)
values ('33bb9554-c616-42e6-a9c6-88d3bba4221c', 'test@test.com',
        '$2a$10$Fv1.pLeI8jOaS8qN13vWWO60oLx.2yTQkDJssjcyssiuxjYeShnPm', current_timestamp(), current_timestamp(),
        'admin@test.com',
        'admin@test.com');

insert into user_roles
values ('33bb9554-c616-42e6-a9c6-88d3bba4221c', 4);