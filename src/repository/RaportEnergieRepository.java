package repository;

import exception.AppException;
import model.House;
import model.RaportEnergie;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

public class RaportEnergieRepository extends AbstractRepository<RaportEnergie> {

    private static RaportEnergieRepository instance;

    private RaportEnergieRepository() {}

    public static synchronized RaportEnergieRepository getInstance() {
        if (instance == null) {
            instance = new RaportEnergieRepository();
        }
        return instance;
    }

    @Override
    protected String getTableName() {
        return "rapoarte_energie";
    }

    @Override
    protected RaportEnergie mapRow(ResultSet rs) throws SQLException {
        int houseId = rs.getInt("house_id");
        Optional<House> house = HouseRepository.getInstance().findById(houseId);
        return new RaportEnergie(
                rs.getInt("id"),
                house.orElse(null),
                rs.getDouble("total_consum"),
                rs.getTimestamp("generat").toLocalDateTime()
        );
    }

    @Override
    public void save(RaportEnergie raport) {
        String sql = "INSERT INTO rapoarte_energie (id, house_id, total_consum, generat) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, raport.getId());
            ps.setInt(2, raport.getCasa().getId());
            ps.setDouble(3, raport.getTotalConsum());
            ps.setTimestamp(4, Timestamp.valueOf(raport.getGenerat()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Eroare la salvare raport energie: " + e.getMessage());
        }
    }

    @Override
    public void update(RaportEnergie raport) {
        String sql = "UPDATE rapoarte_energie SET total_consum = ?, generat = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setDouble(1, raport.getTotalConsum());
            ps.setTimestamp(2, Timestamp.valueOf(raport.getGenerat()));
            ps.setInt(3, raport.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Eroare la update raport energie: " + e.getMessage());
        }
    }

    public int nextId() {
        String sql = "SELECT COALESCE(MAX(id), 0) + 1 AS next_id FROM rapoarte_energie";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("next_id") : 1;
        } catch (SQLException e) {
            throw new AppException("Eroare la generare id raport: " + e.getMessage());
        }
    }
}
