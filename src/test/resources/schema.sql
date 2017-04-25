drop schema if exists dialect cascade;
create schema dialect; 
set search_path to dialect;


CREATE TABLE "exotic_types"(
 "login" Varchar NOT NULL,
 "countries" Bit(7) NOT NULL,
 "authorizations" Character varying(20)[] NOT NULL,
 "scores" Bigint[] NULL,
 "gpa" Bigint[] NULL,
 "status" Character(2) NOT NULL,
 "custom" varchar NOT NULL,
 "total" integer not null,
 "color" text,
 "capitals" Character varying(20)[] null,
 "student_id" varchar not null
)
WITH (OIDS=FALSE)
;

ALTER TABLE "exotic_types" ADD CONSTRAINT "Key1" PRIMARY KEY ("login")
;

CREATE TABLE "graduate_student" (
"id" varchar NOT NULL,
"name" varchar NOT NULL,
"zone" varchar NOT NULL,
"gpa" numeric(10, 4) NOT NULL,
"inserted_on" timestamp NOT NULL
)
WITH (OIDS=FALSE)
;

ALTER TABLE "graduate_student" ADD CONSTRAINT "Key2" PRIMARY KEY ("id")
;

CREATE TABLE "planet" (
    "name" varchar not null,
    "position" integer not null,
    "distance" integer not null
) WITH (OIDS = FALSE)
;
    

ALTER TABLE "planet" ADD CONSTRAINT "Key4" PRIMARY KEY ("name", "position")
;

CREATE TABLE "embed_simple" (
    "owner" varchar not null,
    "state" varchar,
    "make" varchar not null,
    "model" varchar,
    "year" integer not null
) WITH (OIDS = FALSE)
;

ALTER TABLE "embed_simple" ADD CONSTRAINT "Key5" PRIMARY KEY ("owner")
;


CREATE TABLE "embed_override" (
    "owner" varchar not null,
    "state" varchar,
    "my_make" varchar not null,
    "my_model" varchar,
    "my_year" integer not null
) WITH (OIDS = FALSE)
;

ALTER TABLE "embed_override" ADD CONSTRAINT "Key6" PRIMARY KEY ("owner")
;
