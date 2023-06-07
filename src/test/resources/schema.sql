drop table if exists roles;
create table if not exists roles
(
    id   smallint primary key,
    name varchar(55) not null
);

drop table if exists users;
create table if not exists users
(
    id                        uuid primary key,
    email                     varchar(155)             not null unique,
    password                  varchar(255)             not null,
    name                      varchar(55)              not null,
    last_name                 varchar(55),
    birth_date                date,
    doctor_type_id            smallint,
    about                     varchar(255),
    refresh_token             varchar,
    profile_picture_file_name varchar(55),
    created_at                timestamp with time zone not null
);

drop table if exists contacts;
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

drop table if exists user_roles;
create table if not exists user_roles
(
    user_id uuid     not null,
    role_id smallint not null
);