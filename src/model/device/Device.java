package model.device;
import model.Room;

public abstract class Device {
    private int id;
    private String nume;
    private boolean status;
    private double putereConsumata;
    private Room room;

    public Device() {}

    public Device(int id, String name, boolean status, double putereConsumata, Room room) {
        this.id = id;
        this.nume = name;
        this.status = status;
        this.putereConsumata = putereConsumata;
        this.room = room;
    }

    // getteri
    public int getId() {
        return id;
    }
    public String getNume() {
        return nume;
    }
    public boolean getStatus() {
        return status;
    }
    public double getPutereConsumata() {
        return putereConsumata;
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
    public void setStatus(boolean status){
        this.status = status;
    }
    public void setPutereConsumata(double putereConsumata) {
        this.putereConsumata = putereConsumata;
    }
    public void setRoom(Room room) {
        this.room = room;
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + "-->> id = " + id + " nume = " + nume + " status = " + status +
                " putere consumata = " + putereConsumata;
    }
}
