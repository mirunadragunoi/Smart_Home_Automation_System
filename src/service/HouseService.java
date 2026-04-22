package service;

import exception.DuplicateEntityException;
import exception.NotFoundException;
import exception.ValidationException;
import model.House;
import model.Room;
import model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HouseService {
    private final List<House> houses = new ArrayList<>();

    public House createHouse(int id, String adresa, User owner) {
        if (id <= 0) {
            throw new ValidationException("Id-ul casei trebuie sa fie pozitiv.");
        }
        requireNonNull(owner, "Owner-ul nu poate fi null.");
        if (adresa == null || adresa.trim().isEmpty()) {
            throw new ValidationException("Adresa nu poate fi goala.");
        }
        if (houses.stream().anyMatch(h -> h.getId() == id)) {
            throw new DuplicateEntityException("Exista deja o casa cu id-ul " + id);
        }

        House house = new House(id, adresa, owner);
        houses.add(house);
        System.out.println("Casa creata: " + house);
        return house;
    }

    public Room addRoom(House house, int id, String nume, String type) {
        requireNonNull(house, "Casa nu poate fi null.");
        if (id <= 0) {
            throw new ValidationException("Id-ul camerei trebuie sa fie pozitiv.");
        }
        if (nume == null || nume.trim().isEmpty()) {
            throw new ValidationException("Numele camerei nu poate fi gol.");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new ValidationException("Tipul camerei nu poate fi gol.");
        }
        if (house.getRooms().stream().anyMatch(r -> r.getId() == id)) {
            throw new DuplicateEntityException("Exista deja o camera cu id-ul " + id + " in casa.");
        }

        Room room = new Room(id, nume, type);
        house.addRoom(room);
        System.out.println("Camera adaugata in " + house.getAdresa() + ": " + room);
        return room;
    }

    public void removeRoom(House house, Room room) {
        requireNonNull(house, "Casa nu poate fi null.");
        requireNonNull(room, "Camera nu poate fi null.");
        if (!house.removeRoom(room)) {
            throw new NotFoundException("Camera nu exista in casa: " + room.getNume());
        }
        System.out.println("Camera stearsa: " + room.getNume() + " din " + house.getAdresa());
    }

    public List<Room> getRooms(House house) {
        requireNonNull(house, "Casa nu poate fi null.");
        return house.getRooms();
    }

    public List<House> getAllHouses() {
        return Collections.unmodifiableList(houses);
    }

    private static void requireNonNull(Object object, String message) {
        if (object == null) {
            throw new ValidationException(message);
        }
    }
}