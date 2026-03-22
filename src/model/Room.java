package model;

import model.device.Device;
import model.senzor.Senzor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Room {
    private int id;
    private String nume;
    private String type;
    private List<Device> devices;
    private Set<Senzor> senzori;

    public Room() {
        this.devices = new ArrayList<>();
        this.senzori = new HashSet<>();
    }

    public Room(int id, String nume, String type) {
        this.id = id;
        this.nume = nume;
        this.type = type;
        this.devices = new ArrayList<>();
        this.senzori = new HashSet<>();
    }

    // getteri
    public int getId() {
        return id;
    }

    public String getNume() {
        return nume;
    }

    public String getType() {
        return type;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public Set<Senzor> getSenzori() {
        return senzori;
    }

    // setteri
    public void setId(int id) {
        this.id = id;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public void setSenzori(Set<Senzor> senzori) {
        this.senzori = senzori;
    }

    @Override
    public String toString() {
        return " * ROOM * -> { id = " + id + ", nume = " + nume + ", type = " + type +
                ", devices = " + devices.size() + ", senzori = " + senzori.size() + " } ";
    }
}
