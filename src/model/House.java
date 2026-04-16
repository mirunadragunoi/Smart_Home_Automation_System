package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class House {
    private int id;
    private String adresa;
    private User owner;
    private List<Room> rooms;

    public House() {
        this.rooms = new ArrayList<>();
    }

    public House (int id, String adresa, User owner) {
        this.id = id;
        this.adresa = adresa;
        this.owner = owner;
        this.rooms = new ArrayList<>();
    }

    // geteri
    public int getId() {
        return id;
    }

    public String getAdresa() {
        return adresa;
    }

    public User getOwner() {
        return owner;
    }

    public List<Room> getRooms() {
        return Collections.unmodifiableList(rooms);
    }

    // setteri
    public void setId(int id) {
        this.id = id;
    }

    public void setAdresa(String adresa) {
        this.adresa = adresa;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = new ArrayList<>(rooms);
    }

    public boolean addRoom(Room room) {
        return this.rooms.add(room);
    }

    public boolean removeRoom(Room room) {
        return this.rooms.remove(room);
    }

    @Override
    public String toString() {
        return "* HOUSE * -> {id = " + id + ", adresa = " + adresa + ", owner = " + owner.getNume()
                + ", rooms = " + rooms.size() + " }";
    }
}
