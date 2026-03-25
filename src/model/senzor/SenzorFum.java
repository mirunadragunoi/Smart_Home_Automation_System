package model.senzor;

import model.Room;

public class SenzorFum extends Senzor {
    private boolean fumDetectat;

    public SenzorFum() {}

    public SenzorFum(int id, String nume, double valoare, Room room, boolean fumDetectat) {
        super(id, nume, valoare, room);
        this.fumDetectat = fumDetectat;
    }

    public boolean isFumDetectat() {
        return fumDetectat;
    }

    public void setFumDetectat(boolean fumDetectat) {
        this.fumDetectat = fumDetectat;
    }

    @Override
    public String toString() {
        return "[SENZOR FUM] --->>> id = " + getId() + " nume = " + getNume() + " valoare = " + getValoare() +
                " fum detectat = " + fumDetectat;
    }
}
