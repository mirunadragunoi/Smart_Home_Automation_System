package service;

import model.Room;
import model.senzor.Senzor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SenzorService {
    private List<Senzor> allSenzori = new ArrayList<>();
    private Random random = new Random();

    public void addSenzor(Room room, Senzor senzor) {
        senzor.setRoom(room);
        room.getSenzori().add(senzor);
        allSenzori.add(senzor);
        System.out.println("Senzor adaugat in " + room.getNume() + ": " + senzor);
    }

    public double readSenzor(Senzor senzor) {
        System.out.println("Citire senzor " + senzor.getNume() + ": " + senzor.getValoare());
        return senzor.getValoare();
    }

    public void simulateSenzorValue(Senzor senzor, double minVal, double maxVal) {
        double simulatedValue = minVal + (maxVal - minVal) * random.nextDouble();
        simulatedValue = Math.round(simulatedValue * 100.0) / 100.0;
        senzor.setValoare(simulatedValue);
        System.out.println("Simulare senzor " + senzor.getNume() + ": noua valoare = " + simulatedValue);
    }

    public List<Senzor> getAllSenzori() {
        return allSenzori;
    }
}