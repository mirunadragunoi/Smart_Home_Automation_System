package model.senzor;

import model.Room;

import java.util.Objects;

public abstract class Senzor {
    private int id;
    private String nume;
    protected double valoare;
    private Room room;

    public Senzor() {}

    public Senzor(int id, String nume, double valoare, Room room){
        this.id = id;
        this.nume = nume;
        this.valoare = valoare;
        this.room = room;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Senzor senzor = (Senzor) o;
        return id == senzor.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // getteri
    public int getId() {
        return id;
    }
    public String getNume() {
        return nume;
    }
    public double getValoare() {
        return valoare;
    }
    public Room getRoom() {
        return room;
    }

    // setteri
    public void setId(int id) {
        this.id = id;
    }
    public void setNume(String nume) {
        this.nume = nume;
    }
    public void setValoare(double valoare) {
        this.valoare = valoare;
    }
    public void setRoom(Room room) {
        this.room = room;
    }

    @Override
    public String toString() {
        return "[SENZOR] ---->>> id = " + id + " nume = " + nume + " valoare senzor = " + valoare;
    }
}
