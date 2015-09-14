
# --- !Ups

CREATE TABLE credits (
  id serial primary key,
  nom varchar(255) not null,
  capital DECIMAL,
  taux DECIMAL,
  duree DECIMAL,
  depart DATE
);

# --- !Downs

DROP TABLE IF EXISTS credits;
