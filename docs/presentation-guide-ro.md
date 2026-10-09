# Ghid complet de prezentare — Smart Home Automation System

---

## 1. Ce este proiectul (răspuns rapid la "despre ce e proiectul tău?")

Proiectul este un sistem de management al unei case inteligente implementat în Java. Permite unui utilizator să:
- gestioneze o casă cu mai multe camere;
- adauge și controleze dispozitive (lumini, termostate, camere de supraveghere, yale inteligente);
- citească și simuleze senzori (temperatură, mișcare, fum, lumină);
- definească reguli de automatizare tip "dacă temperatura > 25°C → pornește termostatul";
- calculeze consumul energetic și genereze rapoarte;
- persisteze toate datele într-o bază de date MySQL;
- auditeze fiecare acțiune într-un fișier CSV.

---

## 2. Etapa I — cerințe și implementare

### 2.1 Minim 8 tipuri de obiecte → **17 clase model**

Toate clasele se află în `src/model/`.

| Clasa | Atribute cheie | Rol |
|---|---|---|
| `User` | id, nume, email, password | proprietarul casei |
| `House` | id, adresa, owner (User), rooms (List\<Room\>) | casa inteligentă |
| `Room` | id, nume, type, devices (List\<Device\>), senzori (Set\<Senzor\>) | cameră din casă |
| `Device` *(abstract)* | id, nume, status, putereConsumata, room | baza ierarhiei de dispozitive |
| `Lumina` | luminozitate (0-100), color | extends Device |
| `Termostat` | temperatura, targetTemperatura | extends Device |
| `Camera` | recording, rezolutie | extends Device |
| `DoorLock` | locked, codAcces | extends Device |
| `Senzor` *(abstract)* | id, nume, valoare, room | baza ierarhiei de senzori |
| `SenzorTemperatura` | temperatura (double) | extends Senzor |
| `SenzorMiscare` | miscareDetectata (boolean) | extends Senzor |
| `SenzorFum` | fumDetectat (boolean) | extends Senzor |
| `SenzorLumina` | nivelLumina (double) | extends Senzor |
| `RegulaAutomatizare` | id, nume, activ, conditii, actiuni | regulă IF-THEN |
| `Conditie` | id, senzor, operator, valoare | condiția din IF |
| `Actiune` | id, device, comanda, valoare | acțiunea din THEN |
| `RaportEnergie` | id, casa, totalConsum, generat (LocalDateTime) | raport energie |

**De ce clasele Device și Senzor sunt abstracte?**
Fiindcă nu are sens să instanțiezi un "dispozitiv generic" — fiecare dispozitiv real are atribute specifice. Clasele abstracte forțează implementarea corectă a subclaselor.

### 2.2 Minim 10 acțiuni → **25 operații în 5 servicii**

Fiecare serviciu expune operații distincte. Exemple concrete:

- `DeviceService.turnOnDevice(device)` → setează `status=true` pe obiect + update în DB + log audit
- `AutomationService.executeRules()` → parcurge toate regulile active, evaluează condițiile față de valorile curente ale senzorilor, execută acțiunile dacă toate condițiile sunt îndeplinite
- `EnergieService.calculateConsum(house)` → suma `putereConsumata` pentru toate device-urile cu `status=true` din toate camerele casei

### 2.3 Colecții — ce colecții există și de ce

| Colecție | Unde | De ce acea colecție |
|---|---|---|
| `List<Room>` | `House.rooms` | ordinea camerelor contează (adăugate în ordine) |
| `List<Device>` | `Room.devices` | ordinea dispozitivelor contează |
| `Set<Senzor>` | `Room.senzori` | senzorii nu se repetă, ordinea nu contează (HashSet) |
| `TreeMap<Integer, RegulaAutomatizare>` | `AutomationService.reguli` | **colecție sortată** — regulile sunt ordonate automat după id (cheia Integer) |

**Colecția sortată:** `TreeMap` menține cheile (id-urile regulilor) în ordine naturală crescătoare. Fără nicio sortare explicită, iterând `reguli.values()` rezultatele apar mereu sortate.

**Al doilea mecanism de sortare:** `DeviceService.getDevicesSortedByConsum()` apelează `Collections.sort(sorted)` unde `Device implements Comparable<Device>` cu `compareTo` bazat pe `putereConsumata`.

### 2.4 Moștenire — 3 ierarhii

**Ierarhia Device:**
```
Device (abstract)
├── Lumina        — luminozitate, color
├── Termostat     — temperatura, targetTemperatura
├── Camera        — recording, rezolutie
└── DoorLock      — locked, codAcces
```

**Ierarhia Senzor:**
```
Senzor (abstract)
├── SenzorTemperatura  — temperatura (double)
├── SenzorMiscare      — miscareDetectata (boolean)
├── SenzorFum          — fumDetectat (boolean)
└── SenzorLumina       — nivelLumina (double)
```

**Ierarhia Exception:**
```
RuntimeException
└── AppException (runtime, unchecked)
    ├── ValidationException      — input invalid (id negativ, string gol etc.)
    ├── DuplicateEntityException — id deja existent
    └── NotFoundException        — entitate negăsită
```

### 2.5 Clase serviciu

5 clase de serviciu în `src/service/`:
- `HouseService` — gestionează case și camere
- `DeviceService` — gestionează dispozitive (CRUD + pornire/oprire/mutare)
- `SenzorService` — gestionează senzori (CRUD + simulare valori)
- `AutomationService` — gestionează reguli IF-THEN (creare, activare, execuție)
- `EnergieService` — calcul consum și rapoarte energie

---

## 3. Etapa II — cerințe și implementare

### 3.1 Persistență cu bază de date relațională și JDBC

**Baza de date:** MySQL 8, baza de date `smart_house_db`.

**Conexiunea:** `DatabaseConfig` (singleton) citește credențialele din `src/db.properties` și menține o singură conexiune JDBC deschisă pe durata aplicației.

```java
// DatabaseConfig.java — pattern Singleton cu lazy initialization thread-safe
public static synchronized DatabaseConfig getInstance() {
    if (instance == null) instance = new DatabaseConfig();
    return instance;
}

public Connection getConnection() {
    if (connection == null || connection.isClosed()) {
        connection = DriverManager.getConnection(url, user, password);
    }
    return connection;
}
```

**Schema bazei de date** — 9 tabele, definite în `src/schema.sql`:

```
users → houses → rooms → devices
                      └→ senzori
              └→ rapoarte_energie
reguli_automatizare → conditii (FK: senzori)
                    → actiuni  (FK: devices)
```

Toate relațiile au `ON DELETE CASCADE` unde are sens (de ex: dacă ștergi o casă, se șterg automat camerele, dispozitivele, senzorii și rapoartele).

**Strategia pentru moștenire în baza de date:** Single-Table Inheritance. Toate tipurile de `Device` sunt într-un singur tabel `devices` cu coloana `type` (LUMINA / TERMOSTAT / CAMERA / DOORLOCK) și coloane nullable pentru atributele specifice fiecărui subtip. La fel pentru `Senzor`.

**De ce Single-Table Inheritance și nu un tabel per subclasă?**
- Simplitate: nu ai nevoie de JOIN-uri pentru a reconstrui un obiect
- Performanță: un singur SELECT pentru a obține toate dispozitivele
- Dezavantaj acceptat: coloane nullable pentru atributele ce nu aparțin tuturor subtipurilor

### 3.2 Servicii singleton generice — AbstractRepository\<T\>

`AbstractRepository<T>` din `src/repository/AbstractRepository.java` este o **clasă generică abstractă** care implementează operațiile comune pentru orice entitate:

```java
public abstract class AbstractRepository<T> {
    protected abstract String getTableName();
    protected abstract T mapRow(ResultSet rs) throws SQLException;
    public abstract void save(T entity);
    public abstract void update(T entity);

    // implementate o singură dată în clasa de bază:
    public void deleteById(int id) { ... }
    public Optional<T> findById(int id) { ... }
    public List<T> findAll() { ... }
    public void deleteAll() { ... }
}
```

**Subclasele** (de ex. `HouseRepository`) trebuie să implementeze doar:
- `getTableName()` → returnează `"houses"`
- `mapRow(ResultSet rs)` → construiește un obiect `House` din linia curentă din ResultSet
- `save(House)` → INSERT cu parametri specifici
- `update(House)` → UPDATE cu parametri specifici

Tot restul (findById, findAll, deleteById, deleteAll) este moștenit din clasa de bază.

**Fiecare repository este și Singleton:**
```java
public class HouseRepository extends AbstractRepository<House> {
    private static HouseRepository instance;

    private HouseRepository() {}

    public static synchronized HouseRepository getInstance() {
        if (instance == null) instance = new HouseRepository();
        return instance;
    }
}
```

### 3.3 CRUD complet pentru 7 entități (cerința: minim 4)

| Repository | Tabel | Operații speciale |
|---|---|---|
| `UserRepository` | users | `findByEmail(email)` — folosit la autentificare JavaFX |
| `HouseRepository` | houses | `findByOwnerId(ownerId)` — case per utilizator |
| `RoomRepository` | rooms | `saveForHouse(room, houseId)`, `findByHouseId(houseId)` |
| `DeviceRepository` | devices | `saveForRoom(device, roomId)`, `findByRoomId(roomId)`, `updateRoom(deviceId, roomId)` |
| `SenzorRepository` | senzori | `saveForRoom(senzor, roomId)`, `findByRoomId(roomId)` |
| `RegulaAutomatizareRepository` | reguli + conditii + actiuni | `saveConditie`, `saveActiune`, `findConditiiByRegulaId`, `findActiuniByRegulaId` |
| `RaportEnergieRepository` | rapoarte_energie | — |

**Cum funcționează mapRow pentru Device** (exemplu de polymorphism la citire din DB):
```java
protected Device mapRow(ResultSet rs) throws SQLException {
    String type = rs.getString("type");
    return switch (type) {
        case "LUMINA"    -> new Lumina(id, nume, status, putere, null,
                               rs.getInt("luminozitate"), rs.getString("color"));
        case "TERMOSTAT" -> new Termostat(id, nume, status, putere, null,
                               rs.getDouble("temperatura"), rs.getDouble("target_temperatura"));
        case "CAMERA"    -> new Camera(id, nume, status, putere, null,
                               rs.getBoolean("recording"), rs.getInt("rezolutie"));
        case "DOORLOCK"  -> new DoorLock(id, nume, status, putere, null,
                               rs.getBoolean("locked"), rs.getString("cod_acces"));
        default -> throw new AppException("Tip device necunoscut: " + type);
    };
}
```

### 3.4 Serviciul de audit CSV

`AuditService` din `src/audit/AuditService.java`:

- **Singleton** cu inițializare lazy și thread-safe (`synchronized`)
- La fiecare apel `audit.log("numeActiune")` scrie o linie în `audit.csv`:
  ```
  createHouse,2025-06-09T14:23:01.452
  addRoom,2025-06-09T14:23:01.478
  addDevice,2025-06-09T14:23:01.502
  ...
  ```
- Fișierul este creat automat cu header dacă nu există
- Folosește `StandardOpenOption.APPEND` → nu suprascrie, adaugă mereu la final
- Timestamp format: `ISO_LOCAL_DATE_TIME` (ex: `2025-06-09T14:23:01.452`)

**Fiecare metodă de serviciu care modifică starea apelează `audit.log()`:**
```java
public House createHouse(int id, String adresa, User owner) {
    // validari...
    houseRepository.save(house);
    audit.log("createHouse");  // ← audit
    return house;
}
```

Acțiuni auditate: `createHouse`, `addRoom`, `removeRoom`, `addDevice`, `removeDevice`, `turnOnDevice`, `turnOffDevice`, `moveDevice`, `addSenzor`, `removeSenzor`, `readSenzor`, `updateSenzorValue`, `simulateSenzorValue`, `createRule`, `addConditie`, `addActiune`, `activareRule`, `dezactivareRule`, `executeRules`, `deleteRule`, `calculateConsum`, `generateRaportEnergie`, `loadFromDatabase`, `loadDevicesFromDatabase`.

---

## 4. Fluxul aplicației (cum funcționează end-to-end)

### Modul Demo (opțiunea 1 din Main):
1. Resetează DB-ul (DELETE în ordinea corectă, respectând FK-urile)
2. Creează User + House + 4 camere → salvate în DB
3. Adaugă dispozitive (Lumina, Termostat, Camera, DoorLock) → salvate în DB
4. Pornește/oprește device-uri → UPDATE în DB + audit
5. Mută un device între camere → UPDATE room_id în DB
6. Adaugă senzori → salvați în DB
7. Simulează valori senzori (Random în interval)
8. Creează 3 reguli de automatizare cu condiții și acțiuni → salvate în DB
9. Setează manual valori senzori astfel încât regulile să se declanșeze
10. `executeRules()` → evaluează condițiile, execută acțiunile, actualizează device-urile în DB
11. Calculează consumul energetic total al casei
12. Generează raport energie → salvat în DB
13. Toate operațiile sunt logate în `audit.csv`

### Modul Interactiv (opțiunea 2 din Main):
1. Încarcă toate datele din DB în memorie (`loadFromDatabase()` pe fiecare serviciu)
2. Afișează meniu de navigare cu submeniuri pentru fiecare entitate
3. Operațiile utilizatorului se reflectă atât în memorie cât și în DB simultan

### Interfața JavaFX:
1. `Launcher` → pornește JavaFX Application Thread
2. `LoginWindow` → autentificare: caută userul în DB după email + verifică parola
3. `RegisterWindow` → creare user nou → INSERT în DB
4. `MainWindow` cu 5 tab-uri:
   - **Casa**: listare case, adăugare casă/cameră
   - **Dispozitive**: adăugare dispozitiv, pornire/oprire, mutare
   - **Senzori**: adăugare senzor, simulare valori
   - **Automatizări**: creare reguli, adăugare condiții/acțiuni, execuție
   - **Energie**: calcul consum, generare raport

---

## 5. Decizii de design — ce și de ce

### De ce PreparedStatement și nu Statement?
Previne SQL injection. Parametrii sunt trimiși separat de query, driverul JDBC gestionează escaping-ul automat.

### De ce Singleton pentru Repository-uri și AuditService?
- Repository-urile accesează o singură conexiune la DB (din `DatabaseConfig`)
- Nu are sens să ai mai multe instanțe care ar putea intra în conflict
- AuditService scrie în același fișier CSV — o singură instanță evită race conditions

### De ce AbstractRepository\<T\> este generic?
Evită duplicarea codului pentru `findById`, `findAll`, `deleteById`, `deleteAll` — aceleași SQL-uri pentru orice tabel. Subclasele adaugă doar ce e specific.

### De ce excepțiile sunt unchecked (RuntimeException)?
Validările sunt interne sistemului — nu are sens să forțezi caller-ul să prindă excepții pentru erori care nu ar trebui să apară dacă codul e corect. Excepțiile checked ar fi adăugat verbozitate fără beneficiu real.

### De ce TreeMap pentru reguli de automatizare?
Regulile trebuie executate în ordine consistentă (după id). TreeMap menține cheile sortate automat, eliminând nevoia de sortare la fiecare execuție.

### De ce Set\<Senzor\> pentru senzorii dintr-o cameră?
Un senzor nu poate apărea de două ori în aceeași cameră. Set-ul garantează unicitatea fără verificări manuale.

---

## 6. Întrebări tipice la prezentare și răspunsuri

**Q: Ce este JDBC și cum l-ai folosit?**
A: JDBC (Java Database Connectivity) este API-ul standard Java pentru a comunica cu baze de date relaționale. Am folosit `DriverManager.getConnection()` pentru a obține o conexiune, `PreparedStatement` pentru a executa query-uri parametrizate, și `ResultSet` pentru a itera rezultatele. Nu am folosit niciun ORM (Hibernate, JPA) — totul este JDBC pur.

**Q: Explică AbstractRepository\<T\>.**
A: Este o clasă generică abstractă care factorizează codul comun pentru toate repository-urile. Definește metodele `findById`, `findAll`, `deleteById`, `deleteAll` o singură dată, folosind `getTableName()` și `mapRow()` furnizate de subclase. Subclasele trebuie să implementeze doar logica specifică entității lor — mapping-ul din ResultSet și INSERT/UPDATE.

**Q: Ce este Single-Table Inheritance?**
A: O strategie de persistență pentru ierarhii de clase în care toate subclasele sunt stocate într-un singur tabel, cu o coloană discriminatorie (`type`) care indică tipul real al obiectului. La citire, `mapRow()` citește `type` și instanțiază subclasa corectă. Alternativa ar fi un tabel per subclasă (Table Per Class), dar ar fi necesitat JOIN-uri mai complexe.

**Q: Cum funcționează executeRules()?**
A: Iterează `TreeMap`-ul de reguli în ordine. Pentru fiecare regulă activă, verifică toate condițiile: compară `senzor.getValoare()` cu `conditie.getValoare()` folosind operatorul definit (`>`, `<`, `==`, `>=`, `<=`, `!=`). Dacă **toate** condițiile sunt îndeplinite, execută fiecare acțiune: `turnOn`, `turnOff`, `setLuminozitate`, `setTemperature`, `lock`, `unlock`, `startRecording`, `stopRecording`. Fiecare acțiune executată face și UPDATE în DB.

**Q: Cum asiguri că nu ai duplicate în colecții?**
A: Înainte de orice `add`, serviciul verifică cu `stream().anyMatch(x -> x.getId() == id)` dacă există deja un obiect cu acel id. Dacă da, aruncă `DuplicateEntityException`. La nivel de DB, coloana `id` este `PRIMARY KEY`, deci MySQL refuză oricum duplicatele la nivel de bază de date.

**Q: Cum funcționează auditul?**
A: `AuditService` este un singleton. Fiecare metodă de serviciu care modifică starea (create, update, delete, pornire/oprire etc.) apelează `audit.log("numeActiune")` la final. `log()` construiește un string `numeActiune,timestamp` și îl scrie cu `BufferedWriter` în modul `APPEND` în fișierul `audit.csv`. Dacă fișierul nu există, îl creează cu headerul `nume_actiune,timestamp`.

**Q: Cum se face încărcarea din DB la modul interactiv?**
A: Fiecare serviciu are o metodă `loadFromDatabase()`. `HouseService.loadFromDatabase()` apelează `houseRepository.findAll()` → pentru fiecare casă apelează `roomRepository.findByHouseId()` și atașează camerele. `DeviceService.loadFromDatabase()` apelează `deviceRepository.findAll()` și reface legăturile dintre device-uri și camere în memorie. Ordinea contează: mai întâi se încarcă utilizatorii/casele/camerele, apoi device-urile/senzorii, apoi regulile (care referă device-uri și senzori deja încărcați).

**Q: De ce ai ales MySQL față de SQLite sau PostgreSQL?**
A: Proiectul a trecut prin PostgreSQL (Etapa II inițial) și SQLite. MySQL 8 a fost ales pentru că este disponibil local, are suport bun pentru `ON DELETE CASCADE`, și driverul `mysql-connector-j` se integrează simplu în Maven. Schimbarea este transparentă pentru cod — doar `db.properties` și driverul trebuie actualizate pentru a schimba baza de date.

**Q: Explică ierarhia de excepții.**
A: `AppException extends RuntimeException` — baza ierarhiei de excepții proprii, unchecked. Din ea derivă:
- `ValidationException` — input invalid (ex: id negativ, string gol, luminozitate în afara intervalului)
- `DuplicateEntityException` — încearcă să adaugi o entitate cu un id deja existent
- `NotFoundException` — caută o entitate care nu există (ex: device nu e în camera specificată)

Folosesc excepții specializate (nu mesaje de eroare) pentru că permit caller-ului să prindă tipuri specifice dacă are nevoie să le trateze diferit (ex: UI-ul poate afișa mesaje diferite pentru ValidationException față de NotFoundException).

---

## 7. Structura fișierului audit.csv (exemplu)

```csv
nume_actiune,timestamp
createHouse,2025-06-09T14:23:01.452
addRoom,2025-06-09T14:23:01.478
addRoom,2025-06-09T14:23:01.480
addRoom,2025-06-09T14:23:01.481
addRoom,2025-06-09T14:23:01.482
addDevice,2025-06-09T14:23:01.502
turnOnDevice,2025-06-09T14:23:01.510
addSenzor,2025-06-09T14:23:01.521
simulateSenzorValue,2025-06-09T14:23:01.534
createRule,2025-06-09T14:23:01.545
executeRules,2025-06-09T14:23:01.567
generateRaportEnergie,2025-06-09T14:23:01.589
```

---

## 8. Rezumat cerințe → ce ai implementat

### Etapa I
| Cerință | Status | Detaliu |
|---|---|---|
| ≥ 8 tipuri de obiecte | ✅ 17 clase model | User, House, Room, 4 Device-uri, 4 Senzori, RegulaAutomatizare, Conditie, Actiune, RaportEnergie |
| ≥ 10 acțiuni/interogări | ✅ 25 operații | 5 servicii × multiple metode |
| Atribute private/protected + getteri/setteri | ✅ toate clasele | ex: `Device.protected String nume`, `Senzor.protected double valoare` |
| ≥ 2 colecții diferite | ✅ List, Set, TreeMap | `List<Room>`, `List<Device>`, `Set<Senzor>`, `TreeMap<Integer, RegulaAutomatizare>` |
| ≥ 1 colecție sortată | ✅ 2 mecanisme | `TreeMap` (automat) + `Collections.sort` cu `Comparable` |
| Moștenire | ✅ 3 ierarhii | Device, Senzor, AppException |
| ≥ 1 clasă serviciu | ✅ 5 servicii | HouseService, DeviceService, SenzorService, AutomationService, EnergieService |
| Clasă Main | ✅ | demo automat + mod interactiv |

### Etapa II
| Cerință | Status | Detaliu |
|---|---|---|
| Persistență BD relațională + JDBC | ✅ MySQL 8 + JDBC pur | `DatabaseConfig`, `schema.sql` (9 tabele) |
| CRUD pentru ≥ 4 clase | ✅ 7 entități cu CRUD complet | User, House, Room, Device, Senzor, RegulaAutomatizare (+Conditie/Actiune), RaportEnergie |
| Servicii singleton generice pentru BD | ✅ `AbstractRepository<T>` | clasă generică abstractă cu operații CRUD comune |
| Fiecare repository este Singleton | ✅ toate 7 repository-uri | pattern Singleton cu `synchronized getInstance()` |
| Serviciu audit CSV | ✅ `AuditService` | singleton, scrie `numeActiune,timestamp` în `audit.csv` |
