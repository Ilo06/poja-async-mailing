create table if not exists app_user
(
    id varchar(36) not null constraint app_user_pk primary key,
    first_name varchar not null,
    last_name varchar not null,
    user_name varchar not null,
    email varchar not null,
    constraint app_user_user_name_key unique (user_name),
    constraint app_user_email_key unique (email)
);

create table if not exists course
(
    id varchar(36) not null constraint course_pk primary key,
    title varchar not null,
    start_date timestamp,
    end_date timestamp
);

create table if not exists user_course
(
    user_id varchar(36) not null references app_user(id),
    course_id varchar(36) not null references course(id),
    constraint user_course_pk primary key (user_id, course_id)
);