package service;

import model.House;
import model.Room;
import model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class HouseService {
    private final List<House> houses = new ArrayList<>();

    public House createHouse(int id, String adresa, User owner) {
        if (id <= 0) {
            throw new IllegalArgumentException("Id-ul casei trebuie sa fie pozitiv.");
        }
        Objects.requireNonNull(owner, "Owner-ul nu poate fi null.");
        if (adresa == null || adresa.trim().isEmpty()) {
            throw new IllegalArgumentException("Adresa nu poate fi goala.");
        }
        if (houses.stream().anyMatch(h -> h.getId() == id)) {
            throw new IllegalArgumentException("Exista deja o casa cu id-ul " + id);
        }

        House house = new House(id, adresa, owner);
        houses.add(house);
        System.out.println("Casa creata: " + house);
        return house;
    }

    public Room addRoom(House house, int id, String nume, String type) {
        Objects.requireNonNull(house, "Casa nu poate fi null.");
        if (id <= 0) {
            throw new IllegalArgumentException("Id-ul camerei trebuie sa fie pozitiv.");
        }
        if (nume == null || nume.trim().isEmpty()) {
            throw new IllegalArgumentException("Numele camerei nu poate fi gol.");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Tipul camerei nu poate fi gol.");
        }
        if (house.getRooms().stream().anyMatch(r -> r.getId() == id)) {
            throw new IllegalArgumentException("Exista deja o camera cu id-ul " + id + " in casa.");
        }

        Room room = new Room(id, nume, type);
        house.addRoom(room);
        System.out.println("Camera adaugata in " + house.getAdresa() + ": " + room);
        return room;
    }

    public void removeRoom(House house, Room room) {
        Objects.requireNonNull(house, "Casa nu poate fi null.");
        Objects.requireNonNull(room, "Camera nu poate fi null.");
        if (!house.removeRoom(room)) {
            throw new IllegalArgumentException("Camera nu exista in casa: " + room.getNume());
        }
        System.out.println("Camera stearsa: " + room.getNume() + " din " + house.getAdresa());
    }

    public List<Room> getRooms(House house) {
        Objects.requireNonNull(house, "Casa nu poate fi null.");
        return house.getRooms();
    }

    public List<House> getAllHouses() {
        return Collections.unmodifiableList(houses);
    }
}