package repository;

import exception.AppException;
import model.automatizare.Actiune;
import model.automatizare.Conditie;
import model.automatizare.RegulaAutomatizare;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegulaAutomatizareRepository extends AbstractRepository<RegulaAutomatizare> {

    private static RegulaAutomatizareRepository instance;

    private RegulaAutomatizareRepository() {}

    public static synchronized RegulaAutomatizareRepository getInstance() {
        if (instance == null) {
            instance = new RegulaAutomatizareRepository();
        }
        return instance;
    }

    @Override
    protected String getTableName() {
        return "reguli_automatizare";
    }

    @Override
    protected RegulaAutomatizare mapRow(ResultSet rs) throws SQLException {
        return new RegulaAutomatizare(
                rs.getInt("id"),
                rs.getString("nume"),
                rs.getBoolean("activ")
        );
    }

    @Override
    public void save(RegulaAutomatizare regula) {
        String sql = "INSERT INTO reguli_automatizare (id, nume, activ) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, regula.getId());
            ps.setString(2, regula.getNume());
            ps.setBoolean(3, regula.isActiv());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Eroare la salvare regula: " + e.getMessage());
        }
    }

    @Override
    public void update(RegulaAutomatizare regula) {
        String sql = "UPDATE reguli_automatizare SET nume = ?, activ = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, regula.getNume());
            ps.setBoolean(2, regula.isActiv());
            ps.setInt(3, regula.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Eroare la update regula: " + e.getMessage());
        }
    }

    public void saveConditie(Conditie c, int regulaId) {
        String sql = "INSERT INTO conditii (id, regula_id, senzor_id, operator, valoare) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, c.getId());
            ps.setInt(2, regulaId);
            ps.setInt(3, c.getSenzor().getId());
            ps.setString(4, c.getOperator());
            ps.setDouble(5, c.getValoare());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Eroare la salvare conditie: " + e.getMessage());
        }
    }

    public void saveActiune(Actiune a, int regulaId) {
        String sql = "INSERT INTO actiuni (id, regula_id, device_id, comanda, valoare) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, a.getId());
            ps.setInt(2, regulaId);
            ps.setInt(3, a.getDevice().getId());
            ps.setString(4, a.getComanda());
            ps.setDouble(5, a.getValoare());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Eroare la salvare actiune: " + e.getMessage());
        }
    }
}
