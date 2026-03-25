package model.automatizare;

import model.device.Device;

public class Actiune {
    private int id;
    private Device device;
    private String comanda;
    private double valoare;

    public Actiune() {}

    public Actiune(int id, Device device, String comanda, double valoare) {
        this.id = id;
        this.device = device;
        this.comanda = comanda;
        this.valoare = valoare;
    }

    // getteri
    public int getId() {
        return id;
    }
    public Device getDevice() {
        return device;
    }
    public String getComanda() {
        return comanda;
    }
    public double getValoare() {
        return valoare;
    }

    // setteri
    public void setId(int id) {
        this.id = id;
    }
    public void setDevice(Device device) {
        this.device = device;
    }
    public void setComanda(String comanda) {
        this.comanda = comanda;
    }
    public void setValoare(double valoare) {
        this.valoare = valoare;
    }

    @Override
    public String toString() {
        return "[ACTIUNE] --->>> id = " + id + " device = " + device.getNume() +
                " comanda = " + comanda + " valoare = " + valoare;
    }
}
