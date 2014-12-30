drop schema if exists dialect cascade;
create schema dialect; 
set search_path to dialect;


CREATE TABLE "exotic_types"(
 "login" Varchar NOT NULL,
 "countries" Bit(7) NOT NULL,
 "authorizations" Character varying(20)[] NOT NULL,
 "scores" Bigint[] NULL,
 "gpa" Bigint[] NULL,
 "status" Character(2) NOT NULL
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
"insertedOn" timestamp NOT NULL
)
WITH (OIDS=FALSE)
;

ALTER TABLE "graduate_student" ADD CONSTRAINT "Key2" PRIMARY KEY ("id")
;