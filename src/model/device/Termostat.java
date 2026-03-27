package model.device;

import model.Room;

public class Termostat extends Device {
    private double temperatura;
    private double targetTemperatura;

    public Termostat() {}

    public Termostat(int id, String name, boolean status, double putereConsumata, Room room,
                     double temperatura, double targetTemperatura) {
        super(id, name, status, putereConsumata, room);
        this.temperatura = temperatura;
        this.targetTemperatura = targetTemperatura;
    }

    // getteri
    public double getTemperatura() {
        return temperatura;
    }
    public double getTargetTemperatura() {
        return targetTemperatura;
    }

    // setteri
    public void setTemperatura(double temperatura) {
        this.temperatura = temperatura;
    }
    public void setTargetTemperatura(double targetTemperatura) {
        this.targetTemperatura = targetTemperatura;
    }

    @Override
    public String toString() {
        return "[TERMOSTAT] --->>> id = " + getId() + " nume = " + getNume() + " temperatura = " + temperatura +
                " target temperatura = " + targetTemperatura;
    }
}
