# Smart Home Automation System

Sistem de automatizare a unei case inteligente, dezvoltat în **Java**, ca proiect pentru disciplina **Programare Avansată pe Obiecte - Java**.

Aplicația permite gestionarea dispozitivelor, senzorilor și regulilor de automatizare dintr-o locuință inteligentă, oferind funcționalități de monitorizare a consumului energetic, execuție automată a acțiunilor pe baza condițiilor din mediu, persistență în bază de date MySQL și audit CSV al acțiunilor.

---

## Tema aleasă

**Smart House** - Sistem de management al unei case inteligente care permite controlul dispozitivelor (lumini, termostate, camere de securitate, yale inteligente), monitorizarea senzorilor (temperatură, mișcare, fum, lumină) și automatizarea acțiunilor pe baza regulilor definite de utilizator.

---

## Tipuri de obiecte (17 clase model)

1. **User** → utilizatorul care deține casa
2. **House** → casa inteligentă, conține camere
3. **Room** → cameră din casă, conține dispozitive și senzori
4. **Device** *(abstract)* → dispozitiv generic
5. **Lumina** → dispozitiv de iluminat (luminozitate, culoare)
6. **Termostat** → dispozitiv de control al temperaturii
7. **Camera** → cameră de supraveghere video
8. **DoorLock** → yală inteligentă
9. **Senzor** *(abstract)* → senzor generic
10. **SenzorTemperatura** → senzor de temperatură
11. **SenzorMiscare** → senzor de mișcare
12. **SenzorFum** → senzor de fum
13. **SenzorLumina** → senzor de lumină ambientală
14. **RegulaAutomatizare** → regulă de automatizare (condiții + acțiuni)
15. **Conditie** → condiție bazată pe un senzor
16. **Actiune** → acțiune executată pe un dispozitiv
17. **RaportEnergie** → raport de consum energetic

---

## Acțiuni / Interogări (25 operații)

### HouseService
1. `createHouse(id, adresa, owner)` → creare casă nouă asociată unui utilizator
2. `addRoom(house, id, nume, type)` → adăugare cameră în casă
3. `removeRoom(house, room)` → ștergere cameră din casă
4. `getRooms(house)` → listare camere dintr-o casă
5. `getAllHouses()` → listare toate casele

### DeviceService
6. `addDevice(room, device)` → adăugare dispozitiv într-o cameră
7. `removeDevice(room, device)` → ștergere dispozitiv din cameră
8. `turnOnDevice(device)` → pornire dispozitiv
9. `turnOffDevice(device)` → oprire dispozitiv
10. `moveDevice(device, fromRoom, toRoom)` → mutare dispozitiv între camere
11. `getDevicesByRoom(room)` → listare dispozitive per cameră
12. `getDevicesSortedByConsum(room)` → listare dispozitive sortate după consum energetic

### SenzorService
13. `addSenzor(room, senzor)` → adăugare senzor într-o cameră
14. `readSenzor(senzor)` → citire valoare curentă a senzorului
15. `updateSenzorValue(senzor, valoare)` → actualizare manuală valoare senzor
16. `simulateSenzorValue(senzor, min, max)` → simulare valoare aleatoare în interval

### AutomationService
17. `createRule(id, nume)` → creare regulă de automatizare
18. `addConditie(regula, id, senzor, operator, valoare)` → adăugare condiție la regulă
19. `addActiune(regula, id, device, comanda, valoare)` → adăugare acțiune la regulă
20. `activareRule(regula)` → activare regulă
21. `dezactivareRule(regula)` → dezactivare regulă
22. `executeRules()` → evaluare și execuție automată a tuturor regulilor active
23. `deleteRule(id)` → ștergere regulă după id

### EnergieService
24. `calculateConsum(house)` → calcul consum energetic total (doar dispozitivele pornite)
25. `generateRaportEnergie(id, house)` → generare raport energie cu timestamp

---

## Structura proiectului

```
src/
├── Main.java                               — punct de intrare (demo + mod interactiv)
├── schema.sql                              — schema MySQL (9 tabele)
├── db.properties                           — configurare conexiune MySQL
├── config/
│   └── DatabaseConfig.java                 — singleton conexiune JDBC
├── audit/
│   └── AuditService.java                   — singleton audit → audit.csv
├── exception/
│   ├── AppException.java                   — excepție de bază (RuntimeException)
│   ├── ValidationException.java            — erori de validare date
│   ├── DuplicateEntityException.java       — entitate duplicat (id existent)
│   └── NotFoundException.java              — entitate negăsită
├── model/
│   ├── User.java
│   ├── House.java                          — conține List<Room>
│   ├── Room.java                           — conține List<Device> + Set<Senzor>
│   ├── RaportEnergie.java
│   ├── device/
│   │   ├── Device.java                     — clasă abstractă, implements Comparable<Device>
│   │   ├── Lumina.java                     — extends Device
│   │   ├── Termostat.java                  — extends Device
│   │   ├── Camera.java                     — extends Device
│   │   └── DoorLock.java                   — extends Device
│   ├── senzor/
│   │   ├── Senzor.java                     — clasă abstractă
│   │   ├── SenzorTemperatura.java          — extends Senzor
│   │   ├── SenzorMiscare.java              — extends Senzor
│   │   ├── SenzorFum.java                  — extends Senzor
│   │   └── SenzorLumina.java              — extends Senzor
│   └── automatizare/
│       ├── RegulaAutomatizare.java
│       ├── Conditie.java
│       └── Actiune.java
├── repository/
│   ├── AbstractRepository.java             — clasă generică abstractă (CRUD comun)
│   ├── UserRepository.java                 — singleton CRUD pentru users
│   ├── HouseRepository.java                — singleton CRUD pentru houses
│   ├── RoomRepository.java                 — singleton CRUD pentru rooms
│   ├── DeviceRepository.java               — singleton CRUD pentru devices
│   ├── SenzorRepository.java               — singleton CRUD pentru senzori
│   ├── RegulaAutomatizareRepository.java   — singleton CRUD + conditii + actiuni
│   └── RaportEnergieRepository.java        — singleton CRUD pentru rapoarte
├── service/
│   ├── HouseService.java
│   ├── DeviceService.java
│   ├── SenzorService.java
│   ├── AutomationService.java
│   └── EnergieService.java
└── ui/
    ├── ConsoleReader.java                  — utilitar citire date din terminal
    ├── SmartHomeConsoleApp.java            — interfață interactivă cu meniuri
    └── fx/
        ├── Launcher.java                   — entry point JavaFX
        ├── SmartHomeFxApp.java             — Application JavaFX
        ├── LoginWindow.java                — ecran autentificare
        ├── RegisterWindow.java             — ecran înregistrare
        ├── MainWindow.java                 — fereastra principală cu tab-uri
        ├── AppContext.java                 — context partajat între ferestre
        ├── Dialogs.java                    — utilitare dialog
        └── tabs/
            ├── CasaTab.java
            ├── DeviceTab.java
            ├── SenzorTab.java
            ├── AutomationTab.java
            └── EnergieTab.java
```

---

## Schema bazei de date (MySQL)

Fișierul `src/schema.sql` definește 9 tabele cu relații de tip foreign key și ON DELETE CASCADE:

```
users ──< houses ──< rooms ──< devices
                         └──< senzori
                    └──< rapoarte_energie
reguli_automatizare ──< conditii (→ senzori)
                    └──< actiuni  (→ devices)
```

**Strategia de moștenire:** Single-Table Inheritance cu coloana discriminatorie `type` pentru ierarhiile `Device` (LUMINA / TERMOSTAT / CAMERA / DOORLOCK) și `Senzor` (TEMPERATURA / LUMINA / MISCARE / FUM).

---

## Diagrame

### Diagrama de clase
Prezintă toate entitățile sistemului, atributele fiecărei clase, relațiile de moștenire și asocierile dintre obiecte.

![Diagrama de clase](docs/diagrama_clase.png)

### Logica aplicației
Ilustrează relațiile dintre entități: un User deține mai multe House-uri, o House conține Room-uri, iar fiecare Room poate avea Device-uri și Senzori. Regulile de automatizare leagă Condiții (bazate pe Senzori) de Acțiuni (bazate pe Device-uri).

![Logica aplicației](docs/logica_clase.png)

### Operații sistem (Service Layer)
Prezintă cele 5 servicii ale aplicației și metodele expuse de fiecare.

![Operații sistem](docs/operatii_sistem.png)

---

## Cerințe tehnice acoperite

### Etapa I

| Cerință | Implementare | Locație în cod |
|---|---|---|
| Minim 8 tipuri de obiecte | 17 clase model | `src/model/` |
| Minim 10 acțiuni/interogări | 25 operații în 5 servicii | `src/service/` |
| Clase cu atribute private/protected + metode de acces | Toate clasele folosesc encapsulare | `User.java`, `Device.java`, `Senzor.java` |
| Minim 2 colecții diferite | List, Set, TreeMap | `List<Room>` în House, `Set<Senzor>` în Room, `TreeMap` în AutomationService |
| Minim 1 colecție sortată | TreeMap + Collections.sort cu Comparable | `TreeMap` în AutomationService; `DeviceService.getDevicesSortedByConsum()` |
| Moștenire | 3 ierarhii | Device → 4 subclase; Senzor → 4 subclase; AppException → 3 subclase |
| Clase serviciu | 5 servicii | HouseService, DeviceService, SenzorService, AutomationService, EnergieService |
| Clasă Main | Demo automat + mod interactiv | `Main.java` |

### Etapa II

| Cerință | Implementare | Locație în cod |
|---|---|---|
| Persistență cu bază de date relațională + JDBC | MySQL + JDBC pur (fără ORM) | `src/config/DatabaseConfig.java`, `src/schema.sql` |
| Servicii singleton generice pentru CRUD | `AbstractRepository<T>` — clasă generică abstractă cu `findById`, `findAll`, `deleteById`, `deleteAll`, `save`, `update` | `src/repository/AbstractRepository.java` |
| CRUD pentru cel puțin 4 clase | CRUD complet pentru 7 entități: User, House, Room, Device, Senzor, RegulaAutomatizare (+ Conditie/Actiune), RaportEnergie | `src/repository/` |
| Serviciu de audit CSV | `AuditService` singleton, scrie `nume_actiune,timestamp` la fiecare operație | `src/audit/AuditService.java` → `audit.csv` |

---

## Moduri de rulare

### 1. Demo automat (opțiunea 1)
Resetează baza de date, recreează toate datele de la zero: casă cu 4 camere, dispozitive, senzori, reguli de automatizare, raport de energie. Toate operațiile sunt logate în `audit.csv`.

### 2. Mod interactiv consolă (opțiunea 2)
Încarcă datele existente din DB și oferă un meniu interactiv pentru toate operațiile.

### 3. Interfață grafică JavaFX
Ecran de login/register cu autentificare prin MySQL, urmat de o fereastră principală cu tab-uri pentru Case, Dispozitive, Senzori, Automatizări și Energie.

---

## Etape dezvoltare

- [x] **Etapa I** — Definirea sistemului și implementarea in-memory
- [x] **Etapa II** — Persistență MySQL + JDBC + serviciu de audit CSV + interfață JavaFX

---

## Tehnologii

- **Java 17**
- **MySQL 8** + **JDBC** (mysql-connector-j 8.4.0)
- **JavaFX 21.0.4** — interfață grafică
- **Maven** — build & dependency management
