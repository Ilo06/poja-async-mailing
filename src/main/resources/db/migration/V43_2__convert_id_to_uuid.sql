-- V2__convert_ids_to_uuid.sql

-- Activer l'extension pour la génération d'UUID côté Postgres (optionnel mais utile)
create extension if not exists pgcrypto;

-- 1. Supprimer les contraintes de clé étrangère dépendantes avant de changer les types
alter table user_course drop constraint if exists user_course_user_id_fkey;
alter table user_course drop constraint if exists user_course_course_id_fkey;

-- 2. Convertir les colonnes id des tables principales
alter table app_user
    alter column id type uuid using id::uuid,
    alter column id set default gen_random_uuid();

alter table course
    alter column id type uuid using id::uuid,
    alter column id set default gen_random_uuid();

-- 3. Convertir les colonnes de clé étrangère dans la table de jointure
alter table user_course
    alter column user_id type uuid using user_id::uuid,
    alter column course_id type uuid using course_id::uuid;

-- 4. Recréer les contraintes de clé étrangère
alter table user_course
    add constraint user_course_user_id_fkey
        foreign key (user_id) references app_user(id);

alter table user_course
    add constraint user_course_course_id_fkey
        foreign key (course_id) references course(id);