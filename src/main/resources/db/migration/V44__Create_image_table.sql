create table if not exists image
(
    id uuid not null default gen_random_uuid()
        constraint image_pk primary key,
    file_name varchar not null,
    user_email varchar not null,
    created_at timestamp not null default now()
);
