package model.device;

import model.Room;

public class Lumina extends Device {
    private int luminozitate;
    private String color;

    public Lumina() {}

    public Lumina(int id, String name, boolean status, double putereConsumata, Room room,
                  int luminozitate, String color) {
        super(id, name, status, putereConsumata, room);
        this.luminozitate = luminozitate;
        this.color = color;
    }

    // getteri
    public int getLuminozitate() {
        return luminozitate;
    }
    public String getColor() {
        return color;
    }

    // setteri
    public void setLuminozitate(int luminozitate) {
        this.luminozitate = luminozitate;
    }
    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "[LUMINA] --->>> id = " + getId() + " nume = " + getNume() + " luminozitate = " + luminozitate +
                " culoare = " + color;
    }
}
