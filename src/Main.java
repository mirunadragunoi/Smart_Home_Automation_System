import model.*;
import model.device.*;
import model.senzor.*;
import model.automatizare.*;
import service.*;
import ui.ConsoleReader;
import ui.SmartHomeConsoleApp;

public class Main {

    public static void main(String[] args) {
        ConsoleReader in = new ConsoleReader();
        System.out.println("\n-------------------------------------------------------");
        System.out.println("              SMART HOME - punct de intrare               ");
        System.out.println("-------------------------------------------------------");
        System.out.println(" 1 - Rulare demo automata");
        System.out.println(" 2 - Mod interactiv cu meniu");
        System.out.println(" 0 - Iesire");

        boolean continua = true;
        while (continua) {
            int mod = in.readChoice("Alege modul: ", 0, 2);
            switch (mod) {
                case 1 -> runDemo();
                case 2 -> {
                    HouseService houseService = new HouseService();
                    DeviceService deviceService = new DeviceService();
                    SenzorService senzorService = new SenzorService();
                    AutomationService automationService = new AutomationService();
                    EnergieService energieService = new EnergieService();
                    new SmartHomeConsoleApp(houseService, deviceService, senzorService,
                            automationService, energieService).run();
                }
                case 0 -> continua = false;
                default -> { }
            }
            if (continua && mod != 0) {
                if (!in.readYesNo("\nRevii la meniul principal?")) {
                    continua = false;
                }
            }
        }
        System.out.println("Program incheiat.");
    }

    private static void runDemo() {
        HouseService houseService = new HouseService();
        DeviceService deviceService = new DeviceService();
        SenzorService senzorService = new SenzorService();
        AutomationService automationService = new AutomationService();
        EnergieService energieService = new EnergieService();

        User user = new User(1, "Alexandru Popescu", "alex@email.com", "parola123");
        House casa = houseService.createHouse(1, "Str. Florilor Nr. 15, Bucuresti", user);

        Room livingRoom = houseService.addRoom(casa, 1, "Living", "living_room");
        Room dormitor = houseService.addRoom(casa, 2, "Dormitor Master", "bedroom");
        Room bucatarie = houseService.addRoom(casa, 3, "Bucatarie", "kitchen");
        Room birou = houseService.addRoom(casa, 4, "Birou", "office");

        DemoDevicesContext devices = demoDeviceManagement(deviceService, livingRoom, dormitor, bucatarie, birou);
        DemoSenzoriContext senzori = demoSenzorManagement(senzorService, livingRoom, bucatarie);
        demoAutomationManagement(automationService, devices, senzori);
        demoHouseManagement(houseService, casa, birou);
        demoEnergie(energieService, casa);

        System.out.println("\n-------------- SMART HOME DEMO — FINALIZAT --------------");
    }

    private static DemoDevicesContext demoDeviceManagement(DeviceService deviceService,
                                                           Room livingRoom, Room dormitor, Room bucatarie, Room birou) {
        System.out.println("-------------- MANAGEMENT DEVICES --------------\n");

        Lumina luminaLiving = new Lumina(1, "Lampa Living", false, 0.06, null, 80, "alb_cald");
        Termostat termostatLiving = new Termostat(2, "Termostat Living", false, 0.03, null, 21.5, 22.0);
        Camera cameraSec = new Camera(3, "Camera Intrare", false, 0.05, null, false, 1080);
        DoorLock doorLock = new DoorLock(4, "Yala Usa Principala", false, 0.01, null, true, "1234");
        Lumina luminaDormitor = new Lumina(5, "Lampa Dormitor", false, 0.04, null, 50, "alb_rece");

        deviceService.addDevice(livingRoom, luminaLiving);
        deviceService.addDevice(livingRoom, termostatLiving);
        deviceService.addDevice(livingRoom, cameraSec);
        deviceService.addDevice(livingRoom, doorLock);
        deviceService.addDevice(dormitor, luminaDormitor);

        deviceService.turnOnDevice(luminaLiving);
        deviceService.turnOnDevice(termostatLiving);
        deviceService.turnOnDevice(cameraSec);
        deviceService.turnOffDevice(cameraSec);

        System.out.println("Devices in Living: " + deviceService.getDevicesByRoom(livingRoom));
        System.out.println("Devices in Living (sortate dupa consum): " + deviceService.getDevicesSortedByConsum(livingRoom));

        deviceService.moveDevice(cameraSec, livingRoom, bucatarie);
        deviceService.removeDevice(dormitor, luminaDormitor);
        deviceService.addDevice(birou, luminaDormitor);
        System.out.println();

        return new DemoDevicesContext(luminaLiving, termostatLiving, cameraSec, doorLock);
    }

    private static DemoSenzoriContext demoSenzorManagement(SenzorService senzorService, Room livingRoom, Room bucatarie) {
        System.out.println("-------------- MANAGEMENT SENZORI --------------\n");

        SenzorTemperatura senzorTemp = new SenzorTemperatura(1, "Senzor Temp Living", 0, null, 21.5);
        SenzorMiscare senzorMiscare = new SenzorMiscare(2, "Senzor Miscare Living", 0, null, false);
        SenzorFum senzorFum = new SenzorFum(3, "Senzor Fum Bucatarie", 0, null, false);
        SenzorLumina senzorLumina = new SenzorLumina(4, "Senzor Lumina Living", 0, null, 350.0);

        senzorService.addSenzor(livingRoom, senzorTemp);
        senzorService.addSenzor(livingRoom, senzorMiscare);
        senzorService.addSenzor(bucatarie, senzorFum);
        senzorService.addSenzor(livingRoom, senzorLumina);

        senzorService.simulateSenzorValue(senzorTemp, 18.0, 30.0);
        senzorService.simulateSenzorValue(senzorLumina, 0, 1000);
        senzorService.readSenzor(senzorTemp);
        System.out.println("Sincronizare temp -> valoare: " + senzorTemp.getTemperatura() + " | " + senzorTemp.getValoare());
        System.out.println("Sincronizare lumina -> valoare: " + senzorLumina.getNivelLumina() + " | " + senzorLumina.getValoare());
        System.out.println();

        return new DemoSenzoriContext(senzorTemp, senzorLumina);
    }

    private static void demoAutomationManagement(AutomationService automationService,
                                               DemoDevicesContext devices, DemoSenzoriContext senzori) {
        System.out.println("-------------- MANAGEMENT AUTOMATIZARI --------------\n");

        RegulaAutomatizare regulaLumina = automationService.createRule(2, "Lumina automata");
        automationService.addConditie(regulaLumina, 2, senzori.senzorLumina, "<", 200.0);
        automationService.addActiune(regulaLumina, 3, devices.luminaLiving, "turnOn", 0);
        automationService.addActiune(regulaLumina, 4, devices.luminaLiving, "setLuminozitate", 100);
        automationService.activareRule(regulaLumina);

        RegulaAutomatizare regulaTemp = automationService.createRule(1, "Racire automata");
        automationService.addConditie(regulaTemp, 1, senzori.senzorTemp, ">", 25.0);
        automationService.addActiune(regulaTemp, 1, devices.termostatLiving, "turnOn", 0);
        automationService.addActiune(regulaTemp, 2, devices.termostatLiving, "setTemperature", 22.0);
        automationService.activareRule(regulaTemp);

        RegulaAutomatizare regulaYala = automationService.createRule(3, "Blocare usa noaptea");
        automationService.addActiune(regulaYala, 1, devices.doorLock, "lock", 0);
        automationService.activareRule(regulaYala);
        automationService.dezactivareRule(regulaYala);

        System.out.println("(Colectie sortata) Reguli in ordinea id: " + automationService.getAllRules());

        senzori.senzorTemp.setValoare(27.0);
        senzori.senzorLumina.setValoare(150);
        automationService.executeRules();
        automationService.deleteRule(3);
        System.out.println();
    }

    private static void demoHouseManagement(HouseService houseService, House casa, Room birou) {
        System.out.println("-------------- MANAGEMENT CASA --------------\n");
        System.out.println("Camere in casa inainte de remove: " + houseService.getRooms(casa));
        houseService.removeRoom(casa, birou);
        System.out.println("Camere in casa dupa remove: " + houseService.getRooms(casa));
        System.out.println();
    }

    private static void demoEnergie(EnergieService energieService, House casa) {
        System.out.println("-------------- MANAGEMENT ENERGIE --------------\n");
        energieService.calculateConsum(casa);
        energieService.generateRaportEnergie(1, casa);
    }

    private static class DemoDevicesContext {
        private final Lumina luminaLiving;
        private final Termostat termostatLiving;
        private final Camera cameraSec;
        private final DoorLock doorLock;

        private DemoDevicesContext(Lumina luminaLiving, Termostat termostatLiving, Camera cameraSec, DoorLock doorLock) {
            this.luminaLiving = luminaLiving;
            this.termostatLiving = termostatLiving;
            this.cameraSec = cameraSec;
            this.doorLock = doorLock;
        }
    }

    private static class DemoSenzoriContext {
        private final SenzorTemperatura senzorTemp;
        private final SenzorLumina senzorLumina;

        private DemoSenzoriContext(SenzorTemperatura senzorTemp, SenzorLumina senzorLumina) {
            this.senzorTemp = senzorTemp;
            this.senzorLumina = senzorLumina;
        }
    }
}
