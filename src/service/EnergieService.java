package service;

import audit.AuditService;
import exception.ValidationException;
import model.House;
import model.RaportEnergie;
import model.Room;
import model.device.Device;
import repository.RaportEnergieRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EnergieService {
    private final List<RaportEnergie> rapoarte = new ArrayList<>();
    private final RaportEnergieRepository raportRepository = RaportEnergieRepository.getInstance();
    private final AuditService audit = AuditService.getInstance();

    public double calculateConsum(House house) {
        requireNonNull(house, "Casa nu poate fi null.");
        double totalConsum = 0;
        for (Room room : house.getRooms()) {
            for (Device device : room.getDevices()) {
                if (device.getStatus()) {
                    totalConsum += device.getPutereConsumata();
                }
            }
        }
        audit.log("calculateConsum");
        System.out.println("Consum total pentru " + house.getAdresa() + ": " + totalConsum + " kWh");
        return totalConsum;
    }

    public RaportEnergie generateRaportEnergie(int id, House house) {
        if (id <= 0) {
            throw new ValidationException("Id-ul raportului trebuie sa fie pozitiv!!!");
        }
        double consum = calculateConsum(house);
        RaportEnergie raport = new RaportEnergie(id, house, consum, LocalDateTime.now());
        rapoarte.add(raport);
        raportRepository.save(raport);
        audit.log("generateRaportEnergie");
        System.out.println("Raport energie generat: " + raport);
        return raport;
    }

    public List<RaportEnergie> getAllRapoarte() {
        return Collections.unmodifiableList(rapoarte);
    }

    public void loadFromDatabase() {
        rapoarte.clear();
        rapoarte.addAll(raportRepository.findAll());
        audit.log("loadRapoarteFromDatabase");
    }

    private static void requireNonNull(Object object, String message) {
        if (object == null) {
            throw new ValidationException(message);
        }
    }
}
