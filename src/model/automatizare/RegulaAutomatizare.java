package model.automatizare;

import java.util.ArrayList;
import java.util.List;

public class RegulaAutomatizare {
    private int id;
    private String nume;
    private boolean activ;
    private List<Conditie> conditii;
    private List<Actiune> actiuni;

    public RegulaAutomatizare() {
        this.conditii = new ArrayList<>();
        this.actiuni = new ArrayList<>();
    }

    public RegulaAutomatizare(int id, String nume, boolean activ) {
        this.id = id;
        this.nume = nume;
        this.activ = activ;
        this.conditii = new ArrayList<>();
        this.actiuni = new ArrayList<>();
    }

    // getteri
    public int getId() {
        return id;
    }
    public String getNume() {
        return nume;
    }
    public boolean isActiv() {
        return activ;
    }
    public List<Conditie> getConditii() {
        return conditii;
    }
    public List<Actiune> getActiuni() {
        return actiuni;
    }

    // setteri
    public void setId(int id) {
        this.id = id;
    }
    public void setNume(String nume) {
        this.nume = nume;
    }
    public void setActiv(boolean activ) {
        this.activ = activ;
    }
    public void setConditii(List<Conditie> conditii) {
        this.conditii = conditii;
    }
    public void setActiuni(List<Actiune> actiuni) {
        this.actiuni = actiuni;
    }

    @Override
    public String toString() {
        return "[REGULA AUTOMATIZARE] --->> id= " + id + " nume = " + nume + " activ = " + activ +
                " conditii = " + conditii.size() + " actiuni = " + actiuni.size();
    }
}
