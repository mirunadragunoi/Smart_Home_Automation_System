package repository;

import exception.AppException;
import model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserRepository extends AbstractRepository<User> {

    private static UserRepository instance;

    private UserRepository() {}

    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    @Override
    protected String getTableName() {
        return "users";
    }

    @Override
    protected User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("nume"),
                rs.getString("email"),
                rs.getString("password")
        );
    }

    @Override
    public void save(User user) {
        String sql = "INSERT INTO users (id, nume, email, password) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, user.getId());
            ps.setString(2, user.getNume());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Eroare la salvare user: " + e.getMessage());
        }
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET nume = ?, email = ?, password = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, user.getNume());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setInt(4, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Eroare la update user: " + e.getMessage());
        }
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new AppException("Eroare la cautare user dupa email: " + e.getMessage());
        }
    }

    public int nextId() {
        String sql = "SELECT COALESCE(MAX(id), 0) + 1 AS next_id FROM users";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("next_id") : 1;
        } catch (SQLException e) {
            throw new AppException("Eroare la generare id user: " + e.getMessage());
        }
    }
}
