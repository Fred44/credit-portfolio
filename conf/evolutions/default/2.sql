
# --- !Ups

CREATE TABLE operations (
  id serial primary key,
  type_op char(4) not null,
  id_credit INT not null,
  date_operation DATE not null,
  frais DECIMAL,
  mensualite DECIMAL,
  montant DECIMAL,
  taux DECIMAL
);

# --- !Downs

DROP TABLE IF EXISTS operations;
