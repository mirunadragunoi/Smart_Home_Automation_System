package service;

import audit.AuditService;
import exception.DuplicateEntityException;
import exception.NotFoundException;
import exception.ValidationException;
import model.House;
import model.Room;
import model.User;
import repository.HouseRepository;
import repository.RoomRepository;
import repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HouseService {
    private final List<House> houses = new ArrayList<>();
    private final UserRepository userRepository = UserRepository.getInstance();
    private final HouseRepository houseRepository = HouseRepository.getInstance();
    private final RoomRepository roomRepository = RoomRepository.getInstance();
    private final AuditService audit = AuditService.getInstance();

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

        if (userRepository.findById(owner.getId()).isEmpty()) {
            userRepository.save(owner);
        }
        House house = new House(id, adresa, owner);
        houseRepository.save(house);
        houses.add(house);
        audit.log("createHouse");
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
        roomRepository.saveForHouse(room, house.getId());
        house.addRoom(room);
        audit.log("addRoom");
        System.out.println("Camera adaugata in " + house.getAdresa() + ": " + room);
        return room;
    }

    public void removeRoom(House house, Room room) {
        requireNonNull(house, "Casa nu poate fi null.");
        requireNonNull(room, "Camera nu poate fi null.");
        if (!house.removeRoom(room)) {
            throw new NotFoundException("Camera nu exista in casa: " + room.getNume());
        }
        roomRepository.deleteById(room.getId());
        audit.log("removeRoom");
        System.out.println("Camera stearsa: " + room.getNume() + " din " + house.getAdresa());
    }

    public List<Room> getRooms(House house) {
        requireNonNull(house, "Casa nu poate fi null.");
        return house.getRooms();
    }

    public List<House> getAllHouses() {
        return Collections.unmodifiableList(houses);
    }

    // ca sa incarc toate casele si camerele din db in memoria serviciului
    public void loadFromDatabase() {
        houses.clear();
        for (House h : houseRepository.findAll()) {
            for (Room r : roomRepository.findByHouseId(h.getId())) {
                h.addRoom(r);
            }
            houses.add(h);
        }
        audit.log("loadFromDatabase");
        System.out.println("Incarcate din DB: " + houses.size() + " case.");
    }

    private static void requireNonNull(Object object, String message) {
        if (object == null) {
            throw new ValidationException(message);
        }
    }
}
