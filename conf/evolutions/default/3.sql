# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "token" (
  "uuid" VARCHAR NOT NULL,
  "email" VARCHAR NOT NULL,
  "creationTime" TIMESTAMP NOT NULL,
  "expirationTime" TIMESTAMP NOT NULL,
  "isSignUp" BOOLEAN NOT NULL
);

create table "USERS" (
  "ID" serial primary key,
  "PROVIDER_ID" VARCHAR NOT NULL,
  "USER_ID" VARCHAR NOT NULL,
  "FIRSTNAME" VARCHAR NOT NULL,
  "LASTNAME" VARCHAR NOT NULL,
  "AVATAR_URL" VARCHAR,
  "EMAIL" VARCHAR NOT NULL,
  "AUTH_METHOD" VARCHAR NOT NULL,
  "HASHER" VARCHAR,
  "PASSWORD" VARCHAR,
  "SALT" VARCHAR
);

# --- !Downs

DROP TABLE IF EXISTS "token";
DROP TABLE IF EXISTS "USERS";