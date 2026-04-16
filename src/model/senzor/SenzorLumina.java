package model.senzor;

import model.Room;

public class SenzorLumina extends Senzor {
    private double nivelLumina;

    public SenzorLumina() {}

    public SenzorLumina(int id, String nume, double valoare, Room room, double nivelLumina) {
        super(id, nume, valoare, room);
        this.nivelLumina = nivelLumina;
        this.valoare = nivelLumina;
    }

    public double getNivelLumina() {
        return nivelLumina;
    }

    public void setNivelLumina(double nivelLumina) {
        this.nivelLumina = nivelLumina;
        this.valoare = nivelLumina;
    }

    @Override
    public void setValoare(double valoare) {
        super.setValoare(valoare);
        this.nivelLumina = valoare;
    }

    @Override
    public String toString() {
        return "[SENZOR LUMINA] --->>> id = " + getId() + " nume = " + getNume() + " valoare = " + getValoare() +
                " nivel lumina = " + nivelLumina;
    }
}
