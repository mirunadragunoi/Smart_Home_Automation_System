package service;

import model.automatizare.Actiune;
import model.automatizare.Conditie;
import model.automatizare.RegulaAutomatizare;
import model.device.Device;
import model.device.DoorLook;
import model.device.Lumina;
import model.device.Termostat;
import model.senzor.Senzor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AutomationService {
    /** reguli indexate dupa id; TreeMap mentine cheile sortate (cerinta colectie sortata). */
    private final Map<Integer, RegulaAutomatizare> reguli = new TreeMap<>();

    public RegulaAutomatizare createRule(int id, String nume) {
        RegulaAutomatizare regula = new RegulaAutomatizare(id, nume, false);
        reguli.put(id, regula);
        System.out.println("Regula creata: " + regula);
        return regula;
    }

    public Conditie addConditie(RegulaAutomatizare regula, int id, Senzor senzor, String operator, double valoare) {
        Conditie conditie = new Conditie(id, senzor, operator, valoare);
        regula.getConditii().add(conditie);
        System.out.println("Conditie adaugata la regula '" + regula.getNume() + "': " + conditie);
        return conditie;
    }

    public Actiune addActiune(RegulaAutomatizare regula, int id, Device device, String comanda, double valoare) {
        Actiune actiune = new Actiune(id, device, comanda, valoare);
        regula.getActiuni().add(actiune);
        System.out.println("Actiune adaugata la regula '" + regula.getNume() + "': " + actiune);
        return actiune;
    }

    public void activareRule(RegulaAutomatizare regula) {
        regula.setActiv(true);
        System.out.println("Regula activata: " + regula.getNume());
    }

    public void executeRules() {
        System.out.println("\n=== Executare reguli de automatizare ===");
        for (RegulaAutomatizare regula : reguli.values()) {
            if (!regula.isActiv()) {
                System.out.println("Regula '" + regula.getNume() + "' este inactiva, se sare.");
                continue;
            }

            boolean toateConditiile = true;
            for (Conditie conditie : regula.getConditii()) {
                double valoareSenzor = conditie.getSenzor().getValoare();
                if (!evaluateConditie(valoareSenzor, conditie.getOperator(), conditie.getValoare())) {
                    toateConditiile = false;
                    break;
                }
            }

            if (toateConditiile) {
                System.out.println("Regula '" + regula.getNume() + "' - conditii indeplinite! Se executa actiunile:");
                for (Actiune actiune : regula.getActiuni()) {
                    executeActiune(actiune);
                }
            } else {
                System.out.println("Regula '" + regula.getNume() + "' - conditii neindeplinite.");
            }
        }
        System.out.println("=== Sfarsit executare reguli ===\n");
    }

    private boolean evaluateConditie(double valoareSenzor, String operator, double valoareTarget) {
        switch (operator) {
            case ">":  return valoareSenzor > valoareTarget;
            case "<":  return valoareSenzor < valoareTarget;
            case ">=": return valoareSenzor >= valoareTarget;
            case "<=": return valoareSenzor <= valoareTarget;
            case "==": return valoareSenzor == valoareTarget;
            default:
                System.out.println("Operator necunoscut: " + operator);
                return false;
        }
    }

    private void executeActiune(Actiune actiune) {
        Device device = actiune.getDevice();
        String comanda = actiune.getComanda();

        switch (comanda) {
            case "turnOn":
                device.setStatus(true);
                System.out.println("  -> " + device.getNume() + " pornit.");
                break;
            case "turnOff":
                device.setStatus(false);
                System.out.println("  -> " + device.getNume() + " oprit.");
                break;
            case "setTemperature":
                if (device instanceof Termostat) {
                    ((Termostat) device).setTargetTemperatura(actiune.getValoare());
                    System.out.println("  -> " + device.getNume() + " temperatura setata la " + actiune.getValoare() + "°C.");
                }
                break;
            case "setLuminozitate":
                if (device instanceof Lumina) {
                    ((Lumina) device).setLuminozitate((int) actiune.getValoare());
                    System.out.println("  -> " + device.getNume() + " luminozitate setata la " + (int) actiune.getValoare() + "%.");
                }
                break;
            case "lock":
                if (device instanceof DoorLook) {
                    ((DoorLook) device).setLocked(true);
                    System.out.println("  -> " + device.getNume() + " incuiat.");
                }
                break;
            case "unlock":
                if (device instanceof DoorLook) {
                    ((DoorLook) device).setLocked(false);
                    System.out.println("  -> " + device.getNume() + " descuiat.");
                }
                break;
            default:
                System.out.println("  -> Comanda necunoscuta: " + comanda);
        }
    }

    /** lista regulilor in ordinea sortata dupa id (valorile TreeMap-ului) */
    public List<RegulaAutomatizare> getAllRules() {
        return new ArrayList<>(reguli.values());
    }
}