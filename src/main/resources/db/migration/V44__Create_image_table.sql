create table if not exists image
(
    id uuid not null default gen_random_uuid()
        constraint image_pk primary key,
    nom_fichier varchar not null,
    email_utilisateur varchar not null
);
