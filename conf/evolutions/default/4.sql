# --- !Ups

ALTER TABLE "USERS" ADD "CREATION" TIMESTAMP DEFAULT NOW() NOT NULL;

# --- !Downs

ALTER TABLE "USERS" DROP "CREATION";