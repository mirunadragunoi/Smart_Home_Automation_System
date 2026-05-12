package repository;

import exception.AppException;
import model.Room;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoomRepository extends AbstractRepository<Room> {

    private static RoomRepository instance;

    private RoomRepository() {}

    public static synchronized RoomRepository getInstance() {
        if (instance == null) {
            instance = new RoomRepository();
        }
        return instance;
    }

    @Override
    protected String getTableName() {
        return "rooms";
    }

    @Override
    protected Room mapRow(ResultSet rs) throws SQLException {
        return new Room(
                rs.getInt("id"),
                rs.getString("nume"),
                rs.getString("type")
        );
    }

    @Override
    public void save(Room room) {
        throw new AppException("Foloseste saveForHouse(room, houseId) pentru a salva camera.");
    }

    public void saveForHouse(Room room, int houseId) {
        String sql = "INSERT INTO rooms (id, nume, type, house_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, room.getId());
            ps.setString(2, room.getNume());
            ps.setString(3, room.getType());
            ps.setInt(4, houseId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Eroare la salvare camera: " + e.getMessage());
        }
    }

    @Override
    public void update(Room room) {
        String sql = "UPDATE rooms SET nume = ?, type = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, room.getNume());
            ps.setString(2, room.getType());
            ps.setInt(3, room.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Eroare la update camera: " + e.getMessage());
        }
    }

    public List<Room> findByHouseId(int houseId) {
        String sql = "SELECT * FROM rooms WHERE house_id = ?";
        List<Room> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, houseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new AppException("Eroare la cautare camere: " + e.getMessage());
        }
    }
}
