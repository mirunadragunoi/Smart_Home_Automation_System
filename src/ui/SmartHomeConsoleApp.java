package ui;

import exception.AppException;
import exception.ValidationException;
import model.House;
import model.RaportEnergie;
import model.Room;
import model.User;
import model.automatizare.RegulaAutomatizare;
import model.device.Camera;
import model.device.Device;
import model.device.DoorLock;
import model.device.Lumina;
import model.device.Termostat;
import model.senzor.Senzor;
import model.senzor.SenzorFum;
import model.senzor.SenzorLumina;
import model.senzor.SenzorMiscare;
import model.senzor.SenzorTemperatura;
import service.AutomationService;
import service.DeviceService;
import service.EnergieService;
import service.HouseService;
import service.SenzorService;

import java.util.ArrayList;
import java.util.List;

public class SmartHomeConsoleApp {
    private final ConsoleReader in = new ConsoleReader();
    private final HouseService houseService;
    private final DeviceService deviceService;
    private final SenzorService senzorService;
    private final AutomationService automationService;
    private final EnergieService energieService;

    private House casaCurenta;

    public SmartHomeConsoleApp(HouseService houseService, DeviceService deviceService,
                               SenzorService senzorService, AutomationService automationService,
                               EnergieService energieService) {
        this.houseService = houseService;
        this.deviceService = deviceService;
        this.senzorService = senzorService;
        this.automationService = automationService;
        this.energieService = energieService;
    }

    public void run() {
        System.out.println("\n-------------------------------------------------------");
        System.out.println("     SMART HOME - mod interactiv (citire din terminal)   ");
        System.out.println("--------------------------------------------------------\n");

        boolean ruleaza = true;
        while (ruleaza) {
            afiseazaMeniuPrincipal();
            int opt = in.readChoice("Alege optiunea: ", 0, 6);
            switch (opt) {
                case 1 -> meniuCasa();
                case 2 -> meniuDispozitive();
                case 3 -> meniuSenzori();
                case 4 -> meniuAutomatizari();
                case 5 -> meniuEnergie();
                case 6 -> meniuListe();
                case 0 -> {
                    ruleaza = false;
                    System.out.println("La revedere.");
                }
                default -> { }
            }
        }
    }

    private void afiseazaMeniuPrincipal() {
        System.out.println("\n--- MENIU PRINCIPAL ---");
        System.out.println("Casa activa: " + (casaCurenta == null ? "(niciuna - creeaza / selecteaza din 1)" : casaCurenta));
        System.out.println(" 1 - Management casa (utilizator, casa, camere)");
        System.out.println(" 2 - Management dispozitive");
        System.out.println(" 3 - Management senzori");
        System.out.println(" 4 - Management automatizari (reguli)");
        System.out.println(" 5 - Management energie (consum, rapoarte)");
        System.out.println(" 6 - Liste rapide (case, camere, device-uri, senzori, reguli)");
        System.out.println(" 0 - Iesire");
    }

    // citiri dedicate
    private User citesteUser() {
        System.out.println("\n--- Date utilizator ---");
        int id = in.readPositiveInt("Id utilizator: ");
        String nume = in.readNonEmptyLine("Nume complet: ");
        String email = in.readNonEmptyLine("Email: ");
        String parola = in.readNonEmptyLine("Parola: ");
        return new User(id, nume, email, parola);
    }

    private void meniuCasa() {
        boolean inapoi = false;
        while (!inapoi) {
            System.out.println("\n------- MANAGEMENT CASA ---------");
            System.out.println(" 1 - Creare casa (+ utilizator)");
            System.out.println(" 2 - Selectare casa activa");
            System.out.println(" 3 - Adaugare camera in casa activa");
            System.out.println(" 4 - Stergere camera din casa activa");
            System.out.println(" 5 - Afisare camere (casa activa)");
            System.out.println(" 0 - Inapoi");
            int o = in.readChoice("Optiune: ", 0, 5);
            if (o == 0) {
                inapoi = true;
                continue;
            }
            try {
                switch (o) {
                    case 1 -> {
                        User u = citesteUser();
                        int hid = in.readPositiveInt("Id casa: ");
                        String adr = in.readNonEmptyLine("Adresa: ");
                        casaCurenta = houseService.createHouse(hid, adr, u);
                    }
                    case 2 -> selecteazaCasa();
                    case 3 -> {
                        House h = cereCasaActiva();
                        int rid = in.readPositiveInt("Id camera: ");
                        String nume = in.readNonEmptyLine("Nume camera: ");
                        String tip = in.readNonEmptyLine("Tip: ");
                        houseService.addRoom(h, rid, nume, tip);
                    }
                    case 4 -> {
                        House h = cereCasaActiva();
                        Room r = alegeCamera(h, "Selecteaza camera de sters");
                        if (r != null) {
                            houseService.removeRoom(h, r);
                        }
                    }
                    case 5 -> {
                        House h = cereCasaActiva();
                        System.out.println("Camere: " + houseService.getRooms(h));
                    }
                    default -> { }
                }
            } catch (Exception ex) {
                handleError("CASA", ex);
            }
        }
    }

    private void selecteazaCasa() {
        List<House> list = new ArrayList<>(houseService.getAllHouses());
        if (list.isEmpty()) {
            System.out.println("Nu exista case. Creeaza o casa mai intai.");
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            System.out.println((i + 1) + ") " + list.get(i));
        }
        int idx = in.readChoice("Numar casa (1-" + list.size() + "): ", 1, list.size());
        casaCurenta = list.get(idx - 1);
        System.out.println("Casa activa setata: " + casaCurenta.getAdresa());
    }

    private House cereCasaActiva() {
        if (casaCurenta == null) {
            throw new ValidationException("Nu ai selectat o casa activa. Foloseste meniul Casa -> selectare.");
        }
        return casaCurenta;
    }

    private Room alegeCamera(House house, String titlu) {
        List<Room> rooms = new ArrayList<>(houseService.getRooms(house));
        if (rooms.isEmpty()) {
            System.out.println("Nu exista camere in aceasta casa.");
            return null;
        }
        System.out.println(titlu + ":");
        for (int i = 0; i < rooms.size(); i++) {
            System.out.println((i + 1) + ") " + rooms.get(i));
        }
        int idx = in.readChoice("Numar camera: ", 1, rooms.size());
        return rooms.get(idx - 1);
    }

    private void meniuDispozitive() {
        boolean inapoi = false;
        while (!inapoi) {
            System.out.println("\n------ MANAGEMENT DISPOZITIVE -----");
            System.out.println(" 1 - Adaugare dispozitiv (tip: Lumina / Termostat / Camera / DoorLock)");
            System.out.println(" 2 - Stergere dispozitiv din camera");
            System.out.println(" 3 - Pornire dispozitiv");
            System.out.println(" 4 - Oprire dispozitiv");
            System.out.println(" 5 - Mutare dispozitiv intre camere");
            System.out.println(" 6 - Lista dispozitive dintr-o camera");
            System.out.println(" 7 - Lista dispozitive sortate dupa consum (camera)");
            System.out.println(" 0 - Inapoi");
            int o = in.readChoice("Optiune: ", 0, 7);
            if (o == 0) {
                inapoi = true;
                continue;
            }
            try {
                switch (o) {
                    case 1 -> adaugaDispozitivInteractiv();
                    case 2 -> {
                        House h = cereCasaActiva();
                        Room r = alegeCamera(h, "Camera");
                        Device d = alegeDeviceDinCamera(r);
                        if (d != null) {
                            deviceService.removeDevice(r, d);
                        }
                    }
                    case 3 -> {
                        Device d = alegeDeviceGlobal("Dispozitiv de pornit");
                        if (d != null) {
                            deviceService.turnOnDevice(d);
                        }
                    }
                    case 4 -> {
                        Device d = alegeDeviceGlobal("Dispozitiv de oprit");
                        if (d != null) {
                            deviceService.turnOffDevice(d);
                        }
                    }
                    case 5 -> mutaDispozitivInteractiv();
                    case 6 -> {
                        House h = cereCasaActiva();
                        Room r = alegeCamera(h, "Camera");
                        if (r != null) {
                            System.out.println(deviceService.getDevicesByRoom(r));
                        }
                    }
                    case 7 -> {
                        House h = cereCasaActiva();
                        Room r = alegeCamera(h, "Camera");
                        if (r != null) {
                            System.out.println("Sortate dupa consum: " + deviceService.getDevicesSortedByConsum(r));
                        }
                    }
                    default -> { }
                }
            } catch (Exception ex) {
                handleError("DEVICES", ex);
            }
        }
    }

    private void adaugaDispozitivInteractiv() {
        House h = cereCasaActiva();
        Room room = alegeCamera(h, "Camera destinatie");
        if (room == null) {
            return;
        }
        System.out.println("Tip dispozitiv: 1 Lumina | 2 Termostat | 3 Camera | 4 DoorLock");
        int tip = in.readChoice("Tip: ", 1, 4);
        int id = in.readPositiveInt("Id device (unic): ");
        String nume = in.readNonEmptyLine("Nume: ");
        boolean status = in.readYesNo("Pornit acum?");
        double putere = in.readDouble("Putere consumata (kW): ");

        Device device = switch (tip) {
            case 1 -> {
                int lum = in.readInt("Luminozitate (0-100): ");
                String culoare = in.readNonEmptyLine("Culoare (ex. alb_cald): ");
                yield new Lumina(id, nume, status, putere, null, lum, culoare);
            }
            case 2 -> {
                double temp = in.readDouble("Temperatura curenta (°C): ");
                double target = in.readDouble("Temperatura tinta (°C): ");
                yield new Termostat(id, nume, status, putere, null, temp, target);
            }
            case 3 -> {
                boolean rec = in.readYesNo("Inregistrare activa?");
                int rez = in.readPositiveInt("Rezolutie (px): ");
                yield new Camera(id, nume, status, putere, null, rec, rez);
            }
            case 4 -> {
                boolean locked = in.readYesNo("Incuiat?");
                String cod = in.readNonEmptyLine("Cod acces: ");
                yield new DoorLock(id, nume, status, putere, null, locked, cod);
            }
            default -> throw new IllegalStateException("Tip invalid");
        };
        deviceService.addDevice(room, device);
    }

    private void mutaDispozitivInteractiv() {
        House h = cereCasaActiva();
        System.out.println("Camera sursa:");
        Room from = alegeCamera(h, "From");
        if (from == null) {
            return;
        }
        Device d = alegeDeviceDinCamera(from);
        if (d == null) {
            return;
        }
        Room to = alegeCamera(h, "Camera destinatie");
        if (to == null) {
            return;
        }
        deviceService.moveDevice(d, from, to);
    }

    private Device alegeDeviceDinCamera(Room room) {
        List<Device> list = new ArrayList<>(deviceService.getDevicesByRoom(room));
        if (list.isEmpty()) {
            System.out.println("Nu exista dispozitive in aceasta camera.");
            return null;
        }
        for (int i = 0; i < list.size(); i++) {
            System.out.println((i + 1) + ") " + list.get(i));
        }
        int idx = in.readChoice("Numar dispozitiv: ", 1, list.size());
        return list.get(idx - 1);
    }

    private Device alegeDeviceGlobal(String titlu) {
        List<Device> list = new ArrayList<>(deviceService.getAllDevices());
        if (list.isEmpty()) {
            System.out.println("Nu exista dispozitive in sistem.");
            return null;
        }
        System.out.println(titlu + ":");
        for (int i = 0; i < list.size(); i++) {
            System.out.println((i + 1) + ") " + list.get(i));
        }
        int idx = in.readChoice("Numar: ", 1, list.size());
        return list.get(idx - 1);
    }

    private void meniuSenzori() {
        boolean inapoi = false;
        while (!inapoi) {
            System.out.println("\n----- MANAGEMENT SENZORI -----");
            System.out.println(" 1 - Adaugare senzor");
            System.out.println(" 2 - Citire valoare senzor");
            System.out.println(" 3 - Simulare valoare aleatoare (min..max)");
            System.out.println(" 4 - Setare manuala valoare (setValoare)");
            System.out.println(" 0 - Inapoi");
            int o = in.readChoice("Optiune: ", 0, 4);
            if (o == 0) {
                inapoi = true;
                continue;
            }
            try {
                switch (o) {
                    case 1 -> adaugaSenzorInteractiv();
                    case 2 -> {
                        Senzor s = alegeSenzorGlobal("Senzor");
                        if (s != null) {
                            senzorService.readSenzor(s);
                        }
                    }
                    case 3 -> {
                        Senzor s = alegeSenzorGlobal("Senzor");
                        if (s != null) {
                            double mn = in.readDouble("Min: ");
                            double mx = in.readDouble("Max: ");
                            senzorService.simulateSenzorValue(s, mn, mx);
                        }
                    }
                    case 4 -> {
                        Senzor s = alegeSenzorGlobal("Senzor");
                        if (s != null) {
                            double v = in.readDouble("Noua valoare: ");
                            senzorService.updateSenzorValue(s, v);
                            System.out.println("Valoare setata: " + s);
                        }
                    }
                    default -> { }
                }
            } catch (Exception ex) {
                handleError("SENZORI", ex);
            }
        }
    }

    private void adaugaSenzorInteractiv() {
        House h = cereCasaActiva();
        Room room = alegeCamera(h, "Camera");
        if (room == null) {
            return;
        }
        System.out.println("Tip: 1 Temperatura | 2 Miscare | 3 Fum | 4 Lumina");
        int tip = in.readChoice("Tip: ", 1, 4);
        int id = in.readPositiveInt("Id senzor (unic): ");
        String nume = in.readNonEmptyLine("Nume: ");
        double valInit = in.readDouble("Valoare initiala (sau 0): ");

        Senzor senzor = switch (tip) {
            case 1 -> {
                double t = in.readDouble("Temperatura afisata: ");
                yield new SenzorTemperatura(id, nume, valInit, null, t);
            }
            case 2 -> {
                boolean m = in.readYesNo("Miscare detectata initial?");
                yield new SenzorMiscare(id, nume, valInit, null, m);
            }
            case 3 -> {
                boolean f = in.readYesNo("Fum detectat initial?");
                yield new SenzorFum(id, nume, valInit, null, f);
            }
            case 4 -> {
                double n = in.readDouble("Nivel lumina: ");
                yield new SenzorLumina(id, nume, valInit, null, n);
            }
            default -> throw new IllegalStateException();
        };
        senzorService.addSenzor(room, senzor);
    }

    private Senzor alegeSenzorGlobal(String titlu) {
        List<Senzor> list = new ArrayList<>(senzorService.getAllSenzori());
        if (list.isEmpty()) {
            System.out.println("Nu exista senzori in sistem.");
            return null;
        }
        System.out.println(titlu + ":");
        for (int i = 0; i < list.size(); i++) {
            System.out.println((i + 1) + ") " + list.get(i));
        }
        int idx = in.readChoice("Numar: ", 1, list.size());
        return list.get(idx - 1);
    }

    private void meniuAutomatizari() {
        boolean inapoi = false;
        while (!inapoi) {
            System.out.println("\n------ MANAGEMENT AUTOMATIZARI -----");
            System.out.println(" 1 - Creare regula");
            System.out.println(" 2 - Adaugare conditie la regula");
            System.out.println(" 3 - Adaugare actiune la regula");
            System.out.println(" 4 - Activare regula");
            System.out.println(" 5 - Dezactivare regula");
            System.out.println(" 6 - Executare toate regulile (active)");
            System.out.println(" 7 - Stergere regula dupa id");
            System.out.println(" 8 - Lista reguli (sortate dupa id)");
            System.out.println(" 0 - Inapoi");
            int o = in.readChoice("Optiune: ", 0, 8);
            if (o == 0) {
                inapoi = true;
                continue;
            }
            try {
                switch (o) {
                    case 1 -> {
                        int id = in.readPositiveInt("Id regula: ");
                        String nume = in.readNonEmptyLine("Nume regula: ");
                        automationService.createRule(id, nume);
                    }
                    case 2 -> {
                        RegulaAutomatizare r = alegeRegula("Regula");
                        if (r == null) {
                            continue;
                        }
                        int cid = in.readPositiveInt("Id conditie (unic in regula): ");
                        Senzor s = alegeSenzorGlobal("Senzor pentru conditie");
                        if (s == null) {
                            continue;
                        }
                        System.out.println("Operatori: > < >= <= ==");
                        String op = in.readNonEmptyLine("Operator: ");
                        double val = in.readDouble("Valoare de comparat: ");
                        automationService.addConditie(r, cid, s, op, val);
                    }
                    case 3 -> {
                        RegulaAutomatizare r = alegeRegula("Regula");
                        if (r == null) {
                            continue;
                        }
                        int aid = in.readPositiveInt("Id actiune (unic in regula): ");
                        Device d = alegeDeviceGlobal("Device tinta");
                        if (d == null) {
                            continue;
                        }
                        System.out.println("Comenzi: turnOn, turnOff, setTemperature, setLuminozitate, lock, unlock");
                        String cmd = in.readNonEmptyLine("Comanda: ");
                        double val = in.readDouble("Valoare (0 daca nu se foloseste): ");
                        automationService.addActiune(r, aid, d, cmd, val);
                    }
                    case 4 -> {
                        RegulaAutomatizare r = alegeRegula("Regula de activat");
                        if (r != null) {
                            automationService.activareRule(r);
                        }
                    }
                    case 5 -> {
                        RegulaAutomatizare r = alegeRegula("Regula de dezactivat");
                        if (r != null) {
                            automationService.dezactivareRule(r);
                        }
                    }
                    case 6 -> automationService.executeRules();
                    case 7 -> {
                        int id = in.readPositiveInt("Id regula de sters: ");
                        automationService.deleteRule(id);
                    }
                    case 8 -> System.out.println(automationService.getAllRules());
                    default -> { }
                }
            } catch (Exception ex) {
                handleError("AUTOMATIZARI", ex);
            }
        }
    }

    private RegulaAutomatizare alegeRegula(String titlu) {
        List<RegulaAutomatizare> list = automationService.getAllRules();
        if (list.isEmpty()) {
            System.out.println("Nu exista reguli. Creeaza o regula mai intai.");
            return null;
        }
        System.out.println(titlu + ":");
        for (int i = 0; i < list.size(); i++) {
            System.out.println((i + 1) + ") " + list.get(i));
        }
        int idx = in.readChoice("Numar: ", 1, list.size());
        return list.get(idx - 1);
    }

    private void meniuEnergie() {
        boolean inapoi = false;
        while (!inapoi) {
            System.out.println("\n----- MANAGEMENT ENERGIE -----");
            System.out.println(" 1 - Calcul consum total (casa activa, device-uri pornite)");
            System.out.println(" 2 - Generare raport energie");
            System.out.println(" 3 - Lista rapoarte generate");
            System.out.println(" 0 - Inapoi");
            int o = in.readChoice("Optiune: ", 0, 3);
            if (o == 0) {
                inapoi = true;
                continue;
            }
            try {
                House h = cereCasaActiva();
                switch (o) {
                    case 1 -> energieService.calculateConsum(h);
                    case 2 -> {
                        int rid = in.readPositiveInt("Id raport: ");
                        energieService.generateRaportEnergie(rid, h);
                    }
                    case 3 -> {
                        List<RaportEnergie> rap = energieService.getAllRapoarte();
                        if (rap.isEmpty()) {
                            System.out.println("Nu exista rapoarte.");
                        } else {
                            rap.forEach(System.out::println);
                        }
                    }
                    default -> { }
                }
            } catch (Exception ex) {
                handleError("ENERGIE", ex);
            }
        }
    }

    private void meniuListe() {
        boolean inapoi = false;
        while (!inapoi) {
            System.out.println("\n----- LISTE RAPIDE -----");
            System.out.println(" 1 - Toate casele");
            System.out.println(" 2 - Camere (casa activa)");
            System.out.println(" 3 - Toate dispozitivele");
            System.out.println(" 4 - Toti senzorii");
            System.out.println(" 5 - Toate regulile");
            System.out.println(" 0 - Inapoi");
            int o = in.readChoice("Optiune: ", 0, 5);
            if (o == 0) {
                inapoi = true;
                continue;
            }
            switch (o) {
                case 1 -> houseService.getAllHouses().forEach(System.out::println);
                case 2 -> {
                    try {
                        System.out.println(houseService.getRooms(cereCasaActiva()));
                    } catch (Exception ex) {
                        handleError("LISTE", ex);
                    }
                }
                case 3 -> deviceService.getAllDevices().forEach(System.out::println);
                case 4 -> senzorService.getAllSenzori().forEach(System.out::println);
                case 5 -> System.out.println(automationService.getAllRules());
                default -> { }
            }
        }
    }

    private void handleError(String modul, Exception ex) {
        if (ex instanceof AppException) {
            System.out.println("[EROARE][" + modul + "] " + ex.getMessage());
            return;
        }
        String msg = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
        System.out.println("[EROARE][" + modul + "] Eroare neasteptata: " + msg);
    }
}
