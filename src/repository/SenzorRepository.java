package repository;

import exception.AppException;
import model.senzor.Senzor;
import model.senzor.SenzorFum;
import model.senzor.SenzorLumina;
import model.senzor.SenzorMiscare;
import model.senzor.SenzorTemperatura;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class SenzorRepository extends AbstractRepository<Senzor> {

    private static SenzorRepository instance;

    private SenzorRepository() {}

    public static synchronized SenzorRepository getInstance() {
        if (instance == null) {
            instance = new SenzorRepository();
        }
        return instance;
    }

    @Override
    protected String getTableName() {
        return "senzori";
    }

    @Override
    protected Senzor mapRow(ResultSet rs) throws SQLException {
        String type = rs.getString("type");
        int id = rs.getInt("id");
        String nume = rs.getString("nume");
        double valoare = rs.getDouble("valoare");

        return switch (type) {
            case "TEMPERATURA" -> new SenzorTemperatura(id, nume, valoare, null, rs.getDouble("temperatura"));
            case "LUMINA" -> new SenzorLumina(id, nume, valoare, null, rs.getDouble("nivel_lumina"));
            case "MISCARE" -> new SenzorMiscare(id, nume, valoare, null, rs.getBoolean("miscare_detectata"));
            case "FUM" -> new SenzorFum(id, nume, valoare, null, rs.getBoolean("fum_detectat"));
            default -> throw new AppException("Tip senzor necunoscut in DB: " + type);
        };
    }

    @Override
    public void save(Senzor senzor) {
        throw new AppException("Foloseste saveForRoom(senzor, roomId).");
    }

    public void saveForRoom(Senzor senzor, int roomId) {
        String sql = "INSERT INTO senzori (id, nume, valoare, room_id, type, " +
                "temperatura, nivel_lumina, miscare_detectata, fum_detectat) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, senzor.getId());
            ps.setString(2, senzor.getNume());
            ps.setDouble(3, senzor.getValoare());
            ps.setInt(4, roomId);
            ps.setString(5, senzorType(senzor));
            bindSubtypeFields(ps, senzor);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Eroare la salvare senzor: " + e.getMessage());
        }
    }

    @Override
    public void update(Senzor senzor) {
        String sql = "UPDATE senzori SET nume = ?, valoare = ?, temperatura = ?, " +
                "nivel_lumina = ?, miscare_detectata = ?, fum_detectat = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, senzor.getNume());
            ps.setDouble(2, senzor.getValoare());
            if (senzor instanceof SenzorTemperatura t) {
                ps.setDouble(3, t.getTemperatura());
                ps.setNull(4, Types.DOUBLE);
                ps.setNull(5, Types.BOOLEAN);
                ps.setNull(6, Types.BOOLEAN);
            } else if (senzor instanceof SenzorLumina l) {
                ps.setNull(3, Types.DOUBLE);
                ps.setDouble(4, l.getNivelLumina());
                ps.setNull(5, Types.BOOLEAN);
                ps.setNull(6, Types.BOOLEAN);
            } else if (senzor instanceof SenzorMiscare m) {
                ps.setNull(3, Types.DOUBLE);
                ps.setNull(4, Types.DOUBLE);
                ps.setBoolean(5, m.isMiscareDetectata());
                ps.setNull(6, Types.BOOLEAN);
            } else if (senzor instanceof SenzorFum f) {
                ps.setNull(3, Types.DOUBLE);
                ps.setNull(4, Types.DOUBLE);
                ps.setNull(5, Types.BOOLEAN);
                ps.setBoolean(6, f.isFumDetectat());
            }
            ps.setInt(7, senzor.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Eroare la update senzor: " + e.getMessage());
        }
    }

    private static String senzorType(Senzor senzor) {
        if (senzor instanceof SenzorTemperatura) return "TEMPERATURA";
        if (senzor instanceof SenzorLumina) return "LUMINA";
        if (senzor instanceof SenzorMiscare) return "MISCARE";
        if (senzor instanceof SenzorFum) return "FUM";
        throw new AppException("Tip senzor necunoscut: " + senzor.getClass().getSimpleName());
    }

    private static void bindSubtypeFields(PreparedStatement ps, Senzor senzor) throws SQLException {
        // 6=temperatura, 7=nivel_lumina, 8=miscare_detectata, 9=fum_detectat
        if (senzor instanceof SenzorTemperatura t) {
            ps.setDouble(6, t.getTemperatura());
            ps.setNull(7, Types.DOUBLE);
            ps.setNull(8, Types.BOOLEAN);
            ps.setNull(9, Types.BOOLEAN);
        } else if (senzor instanceof SenzorLumina l) {
            ps.setNull(6, Types.DOUBLE);
            ps.setDouble(7, l.getNivelLumina());
            ps.setNull(8, Types.BOOLEAN);
            ps.setNull(9, Types.BOOLEAN);
        } else if (senzor instanceof SenzorMiscare m) {
            ps.setNull(6, Types.DOUBLE);
            ps.setNull(7, Types.DOUBLE);
            ps.setBoolean(8, m.isMiscareDetectata());
            ps.setNull(9, Types.BOOLEAN);
        } else if (senzor instanceof SenzorFum f) {
            ps.setNull(6, Types.DOUBLE);
            ps.setNull(7, Types.DOUBLE);
            ps.setNull(8, Types.BOOLEAN);
            ps.setBoolean(9, f.isFumDetectat());
        }
    }
}
