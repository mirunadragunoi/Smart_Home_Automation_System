package repository;

import exception.AppException;
import model.automatizare.Actiune;
import model.automatizare.Conditie;
import model.automatizare.RegulaAutomatizare;
import model.device.Device;
import model.senzor.Senzor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public int nextId() {
        String sql = "SELECT COALESCE(MAX(id), 0) + 1 AS next_id FROM reguli_automatizare";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("next_id") : 1;
        } catch (SQLException e) {
            throw new AppException("Eroare la generare id regula: " + e.getMessage());
        }
    }

    public int nextConditieId() {
        String sql = "SELECT COALESCE(MAX(id), 0) + 1 AS next_id FROM conditii";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("next_id") : 1;
        } catch (SQLException e) {
            throw new AppException("Eroare la generare id conditie: " + e.getMessage());
        }
    }

    public int nextActiuneId() {
        String sql = "SELECT COALESCE(MAX(id), 0) + 1 AS next_id FROM actiuni";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("next_id") : 1;
        } catch (SQLException e) {
            throw new AppException("Eroare la generare id actiune: " + e.getMessage());
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

    public List<Conditie> findConditiiByRegulaId(int regulaId) {
        String sql = "SELECT id, senzor_id, operator, valoare FROM conditii WHERE regula_id = ?";
        List<Conditie> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, regulaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int senzorId = rs.getInt("senzor_id");
                    Optional<Senzor> senzor = SenzorRepository.getInstance().findById(senzorId);
                    if (senzor.isEmpty()) continue;
                    list.add(new Conditie(rs.getInt("id"), senzor.get(),
                            rs.getString("operator"), rs.getDouble("valoare")));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new AppException("Eroare la incarcare conditii: " + e.getMessage());
        }
    }

    public List<Actiune> findActiuniByRegulaId(int regulaId) {
        String sql = "SELECT id, device_id, comanda, valoare FROM actiuni WHERE regula_id = ?";
        List<Actiune> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, regulaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int deviceId = rs.getInt("device_id");
                    Optional<Device> device = DeviceRepository.getInstance().findById(deviceId);
                    if (device.isEmpty()) continue;
                    list.add(new Actiune(rs.getInt("id"), device.get(),
                            rs.getString("comanda"), rs.getDouble("valoare")));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new AppException("Eroare la incarcare actiuni: " + e.getMessage());
        }
    }
}
