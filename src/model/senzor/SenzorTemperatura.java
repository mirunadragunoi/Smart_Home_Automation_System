package model.senzor;

import model.Room;

public class SenzorTemperatura extends Senzor {
    private double temperatura;

    public SenzorTemperatura() {}

    public SenzorTemperatura(int id, String nume, double valoare, Room room, double temperatura) {
        super(id, nume, valoare, room);
        this.temperatura = temperatura;
    }

    public double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(double temperatura) {
        this.temperatura = temperatura;
    }

    @Override
    public String toString() {
        return "[SENZOR TEMPERATURA] --->>> id = " + getId() + " nume = " + getNume() + " valoare = " + getValoare() +
                " temperatura = " + temperatura;
    }
}
