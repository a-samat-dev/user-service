insert into roles
values (1, 'ROLE_ADMIN');
insert into roles
values (2, 'ROLE_ORGANIZATION');
insert into roles
values (3, 'ROLE_DOCTOR');
insert into roles
values (4, 'ROLE_PATIENT');

insert into users(id, email, password, name, birth_date, created_at)
values ('33bb9554-c616-42e6-a9c6-88d3bba4221c', 'test@test.com',
        '$2a$10$Fv1.pLeI8jOaS8qN13vWWO60oLx.2yTQkDJssjcyssiuxjYeShnPm', 'Test', '2000-01-01', current_timestamp());

insert into contacts (id, user_id, city_id, phone_number1, created_at)
values ('33bb9554-c616-42e6-a9c6-88d3bba4221c', '33bb9554-c616-42e6-a9c6-88d3bba4221c', 1, '12345678',
        current_timestamp());

insert into user_roles
values ('33bb9554-c616-42e6-a9c6-88d3bba4221c', 4);