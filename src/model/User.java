package model;

public class User {
    private int id;
    private String nume;
    private String email;
    private String password;

    public User() {}

    // constructor
    public User(int id, String nume, String email, String password) {
        this.id = id;
        this.nume = nume;
        this.email = email;
        this.password = password;
    }

    // getteri
    public int getId() {
        return id;
    }

    public String getNume() {
        return nume;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    // setteri
    public void setId(int id) {
        this.id = id;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return " * USER * { id = " + id + ", nume = " + nume + ", email = " + email + "}";
    }
}
