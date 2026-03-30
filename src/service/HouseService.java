package service;

import model.House;
import model.Room;
import model.User;

import java.util.ArrayList;
import java.util.List;

public class HouseService {
    private List<House> houses = new ArrayList<>();

    public House createHouse(int id, String adresa, User owner) {
        House house = new House(id, adresa, owner);
        houses.add(house);
        System.out.println("Casa creata: " + house);
        return house;
    }

    public Room addRoom(House house, int id, String nume, String type) {
        Room room = new Room(id, nume, type);
        house.getRooms().add(room);
        System.out.println("Camera adaugata in " + house.getAdresa() + ": " + room);
        return room;
    }

    public void removeRoom(House house, Room room) {
        house.getRooms().remove(room);
        System.out.println("Camera stearsa: " + room.getNume() + " din " + house.getAdresa());
    }

    public List<Room> getRooms(House house) {
        return house.getRooms();
    }

    public List<House> getAllHouses() {
        return houses;
    }
}