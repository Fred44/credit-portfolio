# --- !Ups

ALTER TABLE "USERS" ADD "IS_ADMIN" BOOLEAN DEFAULT FALSE NOT NULL;

# --- !Downs

ALTER TABLE "USERS" DROP "IS_ADMIN";
