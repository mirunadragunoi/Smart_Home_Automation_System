package model;

import java.time.LocalDateTime;

public class RaportEnergie {
    private int id;
    private House casa;
    private double totalConsum;
    private LocalDateTime generat;

    public RaportEnergie() {}

    public RaportEnergie(int id, House casa, double totalConsum, LocalDateTime generat) {
        this.id = id;
        this.casa = casa;
        this.totalConsum = totalConsum;
        this.generat = generat;
    }

    // getteri
    public int getId() { return id; }
    public House getCasa() { return casa; }
    public double getTotalConsum() { return totalConsum; }
    public LocalDateTime getGenerat() { return generat; }

    // setteri
    public void setId(int id) { this.id = id; }
    public void setCasa(House casa) { this.casa = casa; }
    public void setTotalConsum(double totalConsum) { this.totalConsum = totalConsum; }
    public void setGenerat(LocalDateTime generat) { this.generat = generat; }

    @Override
    public String toString() {
        return " [RAPORT ENERGIE] --->>> id = " + id + " casa = " + casa.getAdresa() +
                " totalConsum = " + totalConsum + " kWh,  generat = " + generat;
    }
}