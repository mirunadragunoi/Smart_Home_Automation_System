CREATE SCHEMA IF NOT EXISTS smart_house;
SET search_path TO smart_house;

DROP TABLE IF EXISTS actiuni CASCADE;
DROP TABLE IF EXISTS conditii CASCADE;
DROP TABLE IF EXISTS reguli_automatizare CASCADE;
DROP TABLE IF EXISTS rapoarte_energie CASCADE;
DROP TABLE IF EXISTS senzori CASCADE;
DROP TABLE IF EXISTS devices CASCADE;
DROP TABLE IF EXISTS rooms CASCADE;
DROP TABLE IF EXISTS houses CASCADE;
DROP TABLE IF EXISTS users CASCADE;



CREATE TABLE users (
                       id       INTEGER PRIMARY KEY,
                       nume     VARCHAR(150) NOT NULL,
                       email    VARCHAR(150) NOT NULL,
                       password VARCHAR(150) NOT NULL
);

CREATE TABLE houses (
                        id       INTEGER PRIMARY KEY,
                        adresa   VARCHAR(255) NOT NULL,
                        owner_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE rooms (
                       id       INTEGER PRIMARY KEY,
                       nume     VARCHAR(150) NOT NULL,
                       type     VARCHAR(50)  NOT NULL,
                       house_id INTEGER NOT NULL REFERENCES houses(id) ON DELETE CASCADE
);

CREATE TABLE devices (
                         id                INTEGER PRIMARY KEY,
                         nume              VARCHAR(150) NOT NULL,
                         status            BOOLEAN      NOT NULL DEFAULT FALSE,
                         putere_consumata  DOUBLE PRECISION NOT NULL CHECK (putere_consumata > 0),
                         room_id           INTEGER REFERENCES rooms(id) ON DELETE SET NULL,
                         type              VARCHAR(30)  NOT NULL,
                         luminozitate      INTEGER,
                         color             VARCHAR(50),
                         temperatura       DOUBLE PRECISION,
                         target_temperatura DOUBLE PRECISION,
                         recording         BOOLEAN,
                         rezolutie         INTEGER,
                         locked            BOOLEAN,
                         cod_acces         VARCHAR(50)
);

CREATE TABLE senzori (
                         id                INTEGER PRIMARY KEY,
                         nume              VARCHAR(150) NOT NULL,
                         valoare           DOUBLE PRECISION NOT NULL,
                         room_id           INTEGER REFERENCES rooms(id) ON DELETE SET NULL,
                         type              VARCHAR(30)  NOT NULL,
                         temperatura       DOUBLE PRECISION,
                         nivel_lumina      DOUBLE PRECISION,
                         miscare_detectata BOOLEAN,
                         fum_detectat      BOOLEAN
);

CREATE TABLE rapoarte_energie (
                                  id            INTEGER PRIMARY KEY,
                                  house_id      INTEGER NOT NULL REFERENCES houses(id) ON DELETE CASCADE,
                                  total_consum  DOUBLE PRECISION NOT NULL,
                                  generat       TIMESTAMP NOT NULL
);

CREATE TABLE reguli_automatizare (
                                     id    INTEGER PRIMARY KEY,
                                     nume  VARCHAR(150) NOT NULL,
                                     activ BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE conditii (
                          id        INTEGER PRIMARY KEY,
                          regula_id INTEGER NOT NULL REFERENCES reguli_automatizare(id) ON DELETE CASCADE,
                          senzor_id INTEGER NOT NULL REFERENCES senzori(id) ON DELETE CASCADE,
                          operator  VARCHAR(5)   NOT NULL,
                          valoare   DOUBLE PRECISION NOT NULL
);

CREATE TABLE actiuni (
                         id        INTEGER PRIMARY KEY,
                         regula_id INTEGER NOT NULL REFERENCES reguli_automatizare(id) ON DELETE CASCADE,
                         device_id INTEGER NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
                         comanda   VARCHAR(50)  NOT NULL,
                         valoare   DOUBLE PRECISION NOT NULL
);

CREATE INDEX idx_rooms_house  ON rooms(house_id);
CREATE INDEX idx_devices_room ON devices(room_id);
CREATE INDEX idx_senzori_room ON senzori(room_id);
CREATE INDEX idx_conditii_reg ON conditii(regula_id);
CREATE INDEX idx_actiuni_reg  ON actiuni(regula_id);