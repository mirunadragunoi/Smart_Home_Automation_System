-- Schema MySQL pentru Smart Home Project - Etapa II
-- Strategie pentru ierarhii: single-table inheritance cu coloana "type"
-- Ruleaza acest fisier in DataGrip (Query Console) cu baza smart_house_db selectata.

DROP TABLE IF EXISTS actiuni;
DROP TABLE IF EXISTS conditii;
DROP TABLE IF EXISTS reguli_automatizare;
DROP TABLE IF EXISTS rapoarte_energie;
DROP TABLE IF EXISTS senzori;
DROP TABLE IF EXISTS devices;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS houses;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id       INT PRIMARY KEY,
    nume     VARCHAR(150) NOT NULL,
    email    VARCHAR(150) NOT NULL,
    password VARCHAR(150) NOT NULL
);

CREATE TABLE houses (
    id       INT PRIMARY KEY,
    adresa   VARCHAR(255) NOT NULL,
    owner_id INT NOT NULL,
    CONSTRAINT fk_houses_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE rooms (
    id       INT PRIMARY KEY,
    nume     VARCHAR(150) NOT NULL,
    type     VARCHAR(50)  NOT NULL,
    house_id INT NOT NULL,
    CONSTRAINT fk_rooms_house FOREIGN KEY (house_id) REFERENCES houses(id) ON DELETE CASCADE
);

CREATE TABLE devices (
    id                  INT PRIMARY KEY,
    nume                VARCHAR(150) NOT NULL,
    status              BOOLEAN NOT NULL DEFAULT FALSE,
    putere_consumata    DOUBLE NOT NULL,
    room_id             INT NULL,
    type                VARCHAR(30) NOT NULL,
    luminozitate        INT NULL,
    color               VARCHAR(50) NULL,
    temperatura         DOUBLE NULL,
    target_temperatura  DOUBLE NULL,
    recording           BOOLEAN NULL,
    rezolutie           INT NULL,
    locked              BOOLEAN NULL,
    cod_acces           VARCHAR(50) NULL,
    CONSTRAINT chk_devices_putere CHECK (putere_consumata > 0),
    CONSTRAINT fk_devices_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL
);

CREATE TABLE senzori (
    id                INT PRIMARY KEY,
    nume              VARCHAR(150) NOT NULL,
    valoare           DOUBLE NOT NULL,
    room_id           INT NULL,
    type              VARCHAR(30) NOT NULL,
    temperatura       DOUBLE NULL,
    nivel_lumina      DOUBLE NULL,
    miscare_detectata BOOLEAN NULL,
    fum_detectat      BOOLEAN NULL,
    CONSTRAINT fk_senzori_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL
);

CREATE TABLE rapoarte_energie (
    id            INT PRIMARY KEY,
    house_id      INT NOT NULL,
    total_consum  DOUBLE NOT NULL,
    generat       TIMESTAMP NOT NULL,
    CONSTRAINT fk_rapoarte_house FOREIGN KEY (house_id) REFERENCES houses(id) ON DELETE CASCADE
);

CREATE TABLE reguli_automatizare (
    id    INT PRIMARY KEY,
    nume  VARCHAR(150) NOT NULL,
    activ BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE conditii (
    id        INT PRIMARY KEY,
    regula_id INT NOT NULL,
    senzor_id INT NOT NULL,
    operator  VARCHAR(5) NOT NULL,
    valoare   DOUBLE NOT NULL,
    CONSTRAINT fk_conditii_regula FOREIGN KEY (regula_id) REFERENCES reguli_automatizare(id) ON DELETE CASCADE,
    CONSTRAINT fk_conditii_senzor FOREIGN KEY (senzor_id) REFERENCES senzori(id) ON DELETE CASCADE
);

CREATE TABLE actiuni (
    id        INT PRIMARY KEY,
    regula_id INT NOT NULL,
    device_id INT NOT NULL,
    comanda   VARCHAR(50) NOT NULL,
    valoare   DOUBLE NOT NULL,
    CONSTRAINT fk_actiuni_regula FOREIGN KEY (regula_id) REFERENCES reguli_automatizare(id) ON DELETE CASCADE,
    CONSTRAINT fk_actiuni_device FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
);

CREATE INDEX idx_rooms_house  ON rooms(house_id);
CREATE INDEX idx_devices_room ON devices(room_id);
CREATE INDEX idx_senzori_room ON senzori(room_id);
CREATE INDEX idx_conditii_reg ON conditii(regula_id);
CREATE INDEX idx_actiuni_reg  ON actiuni(regula_id);
