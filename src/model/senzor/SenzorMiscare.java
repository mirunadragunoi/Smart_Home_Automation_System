package model.senzor;

import model.Room;

public class SenzorMiscare extends Senzor {
    private boolean miscareDetectata;

    public SenzorMiscare() {}

    public SenzorMiscare(int id, String nume, double valoare, Room room, boolean miscareDetectata) {
        super(id, nume, valoare, room);
        this.miscareDetectata = miscareDetectata;
    }

    public boolean isMiscareDetectata() {
        return miscareDetectata;
    }

    public void setMiscareDetectata(boolean miscareDetectata) {
        this.miscareDetectata = miscareDetectata;
    }

    @Override
    public String toString() {
        return "[SENZOR MISCARE] --->>> id = " + getId() + " nume = " + getNume() + " valoare = " + getValoare() +
                " miscare detectata = " + miscareDetectata;
    }
}
