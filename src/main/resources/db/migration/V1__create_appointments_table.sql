CREATE SCHEMA IF NOT EXISTS "scheduling-service";
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE SEQUENCE IF NOT EXISTS "scheduling-service".user_types_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

create table appointments (
    id BIGINT NOT NULL DEFAULT nextval('"scheduling-service".user_types_seq'),
    uuid UUID NOT NULL DEFAULT gen_random_uuid(),
    patient_id uuid not null,
    doctor_id uuid not null,
    appointment_date_time timestamp with time zone not null,
    status varchar(32) not null,
    deleted_at timestamp with time zone null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,

    CONSTRAINT appointments_pkey         PRIMARY KEY (id),
    CONSTRAINT appointments_uuid_unique UNIQUE (uuid)
);
