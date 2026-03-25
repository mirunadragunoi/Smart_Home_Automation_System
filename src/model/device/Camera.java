package model.device;

import model.Room;

public class Camera extends Device {
    private boolean recording;
    private int rezolutie;

    public Camera() {}

    public Camera(int id, String name, boolean status, double putereConsumata, Room room,
                  boolean recording, int rezolutie) {
        super(id, name, status, putereConsumata, room);
        this.recording = recording;
        this.rezolutie = rezolutie;
    }

    // getteri
    public boolean isRecording() {
        return recording;
    }
    public int getRezolutie() {
        return rezolutie;
    }

    // setteri
    public void setRecording(boolean recording) {
        this.recording = recording;
    }
    public void setRezolutie(int rezolutie) {
        this.rezolutie = rezolutie;
    }

    @Override
    public String toString() {
        return "[CAMERA] ---->> id = " + getId() + " nume = " + getNume() + " recording?? = " + recording +
                " rezolutie = " + rezolutie;
    }
}
