package model.device;

import model.Room;

public class DoorLook extends Device{
    private boolean locked;
    private String codAcces;

    public DoorLook() {}

    public DoorLook(int id, String name, boolean status, double putereConsumata, Room room,
                    boolean locked, String codAcces) {
        super(id, name, status, putereConsumata, room);
        this.locked = locked;
        this.codAcces = codAcces;
    }

    // getteri
    public boolean isLocked() {
        return locked;
    }
    public String getCodAcces() {
        return codAcces;
    }

    // setteri
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
    public void setCodAcces(String codAcces) {
        this.codAcces = codAcces;
    }

    @Override
    public String toString() {
        return "[DOOR LOCK] --->> id = " + getId() + " nume = " + getNume() + " locked = " + locked + " cod acces = " +
        codAcces;
    }
}
