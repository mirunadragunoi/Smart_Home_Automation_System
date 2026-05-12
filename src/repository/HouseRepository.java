package repository;

import exception.AppException;
import model.House;
import model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class HouseRepository extends AbstractRepository<House> {

    private static HouseRepository instance;

    private HouseRepository() {}

    public static synchronized HouseRepository getInstance() {
        if (instance == null) {
            instance = new HouseRepository();
        }
        return instance;
    }

    @Override
    protected String getTableName() {
        return "houses";
    }

    @Override
    protected House mapRow(ResultSet rs) throws SQLException {
        int ownerId = rs.getInt("owner_id");
        Optional<User> owner = UserRepository.getInstance().findById(ownerId);
        return new House(
                rs.getInt("id"),
                rs.getString("adresa"),
                owner.orElse(null)
        );
    }

    @Override
    public void save(House house) {
        String sql = "INSERT INTO houses (id, adresa, owner_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, house.getId());
            ps.setString(2, house.getAdresa());
            ps.setInt(3, house.getOwner().getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Eroare la salvare casa: " + e.getMessage());
        }
    }

    @Override
    public void update(House house) {
        String sql = "UPDATE houses SET adresa = ?, owner_id = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, house.getAdresa());
            ps.setInt(2, house.getOwner().getId());
            ps.setInt(3, house.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Eroare la update casa: " + e.getMessage());
        }
    }
}
