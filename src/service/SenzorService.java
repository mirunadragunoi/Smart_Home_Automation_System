package service;

import model.Room;
import model.senzor.Senzor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class SenzorService {
    private final List<Senzor> allSenzori = new ArrayList<>();
    private final Random random = new Random();

    public void addSenzor(Room room, Senzor senzor) {
        Objects.requireNonNull(room, "Camera nu poate fi null.");
        Objects.requireNonNull(senzor, "Senzorul nu poate fi null.");
        if (allSenzori.stream().anyMatch(existing -> existing.getId() == senzor.getId())) {
            throw new IllegalArgumentException("Exista deja un senzor cu id-ul " + senzor.getId());
        }

        senzor.setRoom(room);
        room.addSenzor(senzor);
        allSenzori.add(senzor);
        System.out.println("Senzor adaugat in " + room.getNume() + ": " + senzor);
    }

    public double readSenzor(Senzor senzor) {
        Objects.requireNonNull(senzor, "Senzorul nu poate fi null.");
        System.out.println("Citire senzor " + senzor.getNume() + ": " + senzor.getValoare());
        return senzor.getValoare();
    }

    public void simulateSenzorValue(Senzor senzor, double minVal, double maxVal) {
        Objects.requireNonNull(senzor, "Senzorul nu poate fi null.");
        if (minVal > maxVal) {
            throw new IllegalArgumentException("Interval invalid pentru simulare: min > max.");
        }

        double simulatedValue = minVal + (maxVal - minVal) * random.nextDouble();
        simulatedValue = Math.round(simulatedValue * 100.0) / 100.0;
        senzor.setValoare(simulatedValue);
        System.out.println("Simulare senzor " + senzor.getNume() + ": noua valoare = " + simulatedValue);
    }

    public List<Senzor> getAllSenzori() {
        return Collections.unmodifiableList(allSenzori);
    }
}