create table if not exists roles
(
    id   smallserial primary key,
    name varchar(55) not null
);

insert into roles
values (1, 'ROLE_ADMIN');
insert into roles
values (2, 'ROLE_ORGANIZATION');
insert into roles
values (3, 'ROLE_DOCTOR');
insert into roles
values (4, 'ROLE_PATIENT');

create table if not exists users
(
    id                        uuid primary key,
    email                     varchar(155)                  not null unique,
    password                  varchar(255)                  not null,
    name                      varchar(155)                  not null,
    last_name                 varchar(155),
    birth_date                date,
    doctor_type_id            smallint,
    about                     varchar(255),
    refresh_token             varchar,
    profile_picture_file_name varchar(55),
    created_at                timestamp with time zone      not null
);

create table if not exists contacts
(
    id              uuid primary key,
    user_id         uuid references users (id) not null,
    city_id         smallint                   not null,
    street          varchar(155),
    building_number varchar(155),
    flat_number     varchar(155),
    phone_number1   varchar(155)               not null,
    phone_number2   varchar(155),
    created_at      timestamp with time zone   not null
);

create table if not exists user_roles
(
    user_id uuid     not null references users (id),
    role_id smallint not null references roles (id)
);