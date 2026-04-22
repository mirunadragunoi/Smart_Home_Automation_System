package service;

import exception.DuplicateEntityException;
import exception.ValidationException;
import model.Room;
import model.senzor.SenzorFum;
import model.senzor.SenzorLumina;
import model.senzor.SenzorMiscare;
import model.senzor.SenzorTemperatura;
import model.senzor.Senzor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SenzorService {
    private final List<Senzor> allSenzori = new ArrayList<>();
    private final Random random = new Random();

    public void addSenzor(Room room, Senzor senzor) {
        requireNonNull(room, "Camera nu poate fi null.");
        requireNonNull(senzor, "Senzorul nu poate fi null.");
        if (allSenzori.stream().anyMatch(existing -> existing.getId() == senzor.getId())) {
            throw new DuplicateEntityException("Exista deja un senzor cu id-ul " + senzor.getId());
        }
        validateSenzorBusinessRules(senzor);

        senzor.setRoom(room);
        room.addSenzor(senzor);
        allSenzori.add(senzor);
        System.out.println("Senzor adaugat in " + room.getNume() + ": " + senzor);
    }

    public double readSenzor(Senzor senzor) {
        requireNonNull(senzor, "Senzorul nu poate fi null.");
        System.out.println("Citire senzor " + senzor.getNume() + ": " + senzor.getValoare());
        return senzor.getValoare();
    }

    public void updateSenzorValue(Senzor senzor, double valoare) {
        requireNonNull(senzor, "Senzorul nu poate fi null.");
        validateValueForSenzorType(senzor, valoare);
        senzor.setValoare(valoare);
        System.out.println("Valoare actualizata pentru " + senzor.getNume() + ": " + valoare);
    }

    public void simulateSenzorValue(Senzor senzor, double minVal, double maxVal) {
        requireNonNull(senzor, "Senzorul nu poate fi null.");
        if (minVal > maxVal) {
            throw new ValidationException("Interval invalid pentru simulare: min > max.");
        }
        validateSimulationRange(senzor, minVal, maxVal);

        double simulatedValue = minVal + (maxVal - minVal) * random.nextDouble();
        simulatedValue = Math.round(simulatedValue * 100.0) / 100.0;
        updateSenzorValue(senzor, simulatedValue);
        System.out.println("Simulare senzor " + senzor.getNume() + ": noua valoare = " + simulatedValue);
    }

    public List<Senzor> getAllSenzori() {
        return Collections.unmodifiableList(allSenzori);
    }

    private static void requireNonNull(Object object, String message) {
        if (object == null) {
            throw new ValidationException(message);
        }
    }

    private static void validateSenzorBusinessRules(Senzor senzor) {
        if (senzor.getNume() == null || senzor.getNume().trim().isEmpty()) {
            throw new ValidationException("Numele senzorului nu poate fi gol.");
        }
        validateValueForSenzorType(senzor, senzor.getValoare());
    }

    private static void validateValueForSenzorType(Senzor senzor, double valoare) {
        if (senzor instanceof SenzorTemperatura && (valoare < -50 || valoare > 80)) {
            throw new ValidationException("Valoarea senzorului de temperatura trebuie sa fie in intervalul [-50, 80].");
        }
        if (senzor instanceof SenzorLumina && (valoare < 0 || valoare > 100000)) {
            throw new ValidationException("Nivelul de lumina trebuie sa fie in intervalul [0, 100000].");
        }
        if ((senzor instanceof SenzorMiscare || senzor instanceof SenzorFum) &&
                (valoare < 0 || valoare > 1)) {
            throw new ValidationException("Pentru senzorii booleani, valoarea trebuie sa fie intre 0 si 1.");
        }
    }

    private static void validateSimulationRange(Senzor senzor, double minVal, double maxVal) {
        if (senzor instanceof SenzorTemperatura && (minVal < -50 || maxVal > 80)) {
            throw new ValidationException("Simularea temperaturii trebuie sa ramana in intervalul [-50, 80].");
        }
        if (senzor instanceof SenzorLumina && (minVal < 0 || maxVal > 100000)) {
            throw new ValidationException("Simularea luminii trebuie sa ramana in intervalul [0, 100000].");
        }
        if ((senzor instanceof SenzorMiscare || senzor instanceof SenzorFum) && (minVal < 0 || maxVal > 1)) {
            throw new ValidationException("Simularea pentru senzorii booleani trebuie sa fie in intervalul [0, 1].");
        }
    }
}