package model.automatizare;

import model.senzor.Senzor;

public class Conditie {
    private int id;
    private Senzor senzor;
    private String operator;
    private double valoare;

    public Conditie() {}

    public Conditie(int id, Senzor senzor, String operator, double valoare) {
        this.id = id;
        this.senzor = senzor;
        this.operator = operator;
        this.valoare = valoare;
    }

    // getteri
    public int getId() {
        return id;
    }
    public Senzor getSenzor() {
        return senzor;
    }
    public String getOperator() {
        return operator;
    }
    public double getValoare() {
        return valoare;
    }

    // setteri
    public void setId(int id) {
        this.id = id;
    }
    public void setSenzor(Senzor senzor) {
        this.senzor = senzor;
    }
    public void setOperator(String operator) {
        this.operator = operator;
    }
    public void setValoare(double valoare) {
        this.valoare = valoare;
    }

    @Override
    public String toString() {
        return "[CONDITIE] --->>> id = " + id + " senzor = " + senzor.getNume() +
                " operator = " + operator + " valoare = " + valoare;
    }
}
