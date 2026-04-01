import model.*;
import model.device.*;
import model.senzor.*;
import model.automatizare.*;
import service.*;

public class Main {
    public static void main(String[] args) {
        // initializare servicii
        HouseService houseService = new HouseService();
        DeviceService deviceService = new DeviceService();
        SenzorService senzorService = new SenzorService();
        AutomationService automationService = new AutomationService();
        EnergieService energieService = new EnergieService();

        // management casa
        System.out.println("========== MANAGEMENT CASA ==========\n");

        User user = new User(1, "Alexandru Popescu", "alex@email.com", "parola123");
        House casa = houseService.createHouse(1, "Str. Florilor Nr. 15, Bucuresti", user);

        Room livingRoom = houseService.addRoom(casa, 1, "Living", "living_room");
        Room dormitor = houseService.addRoom(casa, 2, "Dormitor Master", "bedroom");
        Room bucatarie = houseService.addRoom(casa, 3, "Bucatarie", "kitchen");

        System.out.println("Camere in casa: " + houseService.getRooms(casa));
        System.out.println();

        // management device uri
        System.out.println("========== MANAGEMENT DEVICES ==========\n");

        Lumina luminaLiving = new Lumina(1, "Lampa Living", false, 0.06, null, 80, "alb_cald");
        Termostat termostatLiving = new Termostat(2, "Termostat Living", false, 0.03, null, 21.5, 22.0);
        Camera cameraSec = new Camera(3, "Camera Intrare", false, 0.05, null, false, 1080);
        DoorLook doorLock = new DoorLook(4, "Yala Usa Principala", false, 0.01, null, true, "1234");
        Lumina luminaDormitor = new Lumina(5, "Lampa Dormitor", false, 0.04, null, 50, "alb_rece");

        deviceService.addDevice(livingRoom, luminaLiving);
        deviceService.addDevice(livingRoom, termostatLiving);
        deviceService.addDevice(livingRoom, cameraSec);
        deviceService.addDevice(livingRoom, doorLock);
        deviceService.addDevice(dormitor, luminaDormitor);

        System.out.println();
        deviceService.turnOnDevice(luminaLiving);
        deviceService.turnOnDevice(termostatLiving);
        deviceService.turnOnDevice(cameraSec);

        System.out.println();
        System.out.println("Devices in Living: " + deviceService.getDevicesByRoom(livingRoom));
        System.out.println();

        deviceService.moveDevice(cameraSec, livingRoom, bucatarie);
        System.out.println();

        // management senzori
        System.out.println("========== MANAGEMENT SENZORI ==========\n");

        SenzorTemperatura senzorTemp = new SenzorTemperatura(1, "Senzor Temp Living", 0, null, 21.5);
        SenzorMiscare senzorMiscare = new SenzorMiscare(2, "Senzor Miscare Living", 0, null, false);
        SenzorFum senzorFum = new SenzorFum(3, "Senzor Fum Bucatarie", 0, null, false);
        SenzorLumina senzorLumina = new SenzorLumina(4, "Senzor Lumina Living", 0, null, 350.0);

        senzorService.addSenzor(livingRoom, senzorTemp);
        senzorService.addSenzor(livingRoom, senzorMiscare);
        senzorService.addSenzor(bucatarie, senzorFum);
        senzorService.addSenzor(livingRoom, senzorLumina);

        System.out.println();
        senzorService.simulateSenzorValue(senzorTemp, 18.0, 30.0);
        senzorService.simulateSenzorValue(senzorLumina, 0, 1000);
        senzorService.readSenzor(senzorTemp);
        System.out.println();

        // automatizari
        System.out.println("========== MANAGEMENT AUTOMATIZARI ==========\n");

        // id=2 introdus inainte de id=1 — TreeMap pastreaza ordinea dupa id la listare si la executeRules
        RegulaAutomatizare regulaLumina = automationService.createRule(2, "Lumina automata");
        automationService.addConditie(regulaLumina, 2, senzorLumina, "<", 200.0);
        automationService.addActiune(regulaLumina, 3, luminaLiving, "turnOn", 0);
        automationService.addActiune(regulaLumina, 4, luminaLiving, "setLuminozitate", 100);
        automationService.activareRule(regulaLumina);

        System.out.println();

        RegulaAutomatizare regulaTemp = automationService.createRule(1, "Racire automata");
        automationService.addConditie(regulaTemp, 1, senzorTemp, ">", 25.0);
        automationService.addActiune(regulaTemp, 1, termostatLiving, "turnOn", 0);
        automationService.addActiune(regulaTemp, 2, termostatLiving, "setTemperature", 22.0);
        automationService.activareRule(regulaTemp);

        System.out.println();
        System.out.println("(Colectie sortata) Reguli in ordinea id: " + automationService.getAllRules());

        System.out.println();

        // simulam valori care triggeruiesc regulile
        senzorTemp.setValoare(27.0);
        senzorLumina.setValoare(150);
        automationService.executeRules();

        // energie
        System.out.println("========== MANAGEMENT ENERGIE ==========\n");

        energieService.calculateConsum(casa);
        energieService.generateRaportEnergie(1, casa);

        System.out.println("\n========== SMART HOME SYSTEM READY ==========");
    }
}