package service;

import model.House;
import model.RaportEnergie;
import model.Room;
import model.device.Device;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class EnergieService {
    private final List<RaportEnergie> rapoarte = new ArrayList<>();

    public double calculateConsum(House house) {
        Objects.requireNonNull(house, "Casa nu poate fi null.");
        double totalConsum = 0;
        for (Room room : house.getRooms()) {
            for (Device device : room.getDevices()) {
                if (device.getStatus()) {
                    totalConsum += device.getPutereConsumata();
                }
            }
        }
        System.out.println("Consum total pentru " + house.getAdresa() + ": " + totalConsum + " kWh");
        return totalConsum;
    }

    public RaportEnergie generateRaportEnergie(int id, House house) {
        if (id <= 0) {
            throw new IllegalArgumentException("Id-ul raportului trebuie sa fie pozitiv.");
        }
        double consum = calculateConsum(house);
        RaportEnergie raport = new RaportEnergie(id, house, consum, LocalDateTime.now());
        rapoarte.add(raport);
        System.out.println("Raport energie generat: " + raport);
        return raport;
    }

    public List<RaportEnergie> getAllRapoarte() {
        return Collections.unmodifiableList(rapoarte);
    }
}