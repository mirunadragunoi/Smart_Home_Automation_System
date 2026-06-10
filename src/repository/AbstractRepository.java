package repository;

import config.DatabaseConfig;
import exception.AppException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * baza generica pentru repository-uri
 * subclasele sunt singleton-uri cu getInstance() static
 * subclasele furnizeaza maparea ResultSet -> T si SQL-urile specifice tabelei
 *
 */

public abstract class AbstractRepository<T> {

    protected Connection getConnection() {
        return DatabaseConfig.getInstance().getConnection();
    }

    /** numele tabelei in PostgreSQL */
    protected abstract String getTableName();

    /** constructie obiect T din ResultSet (din linia curenta) */
    protected abstract T mapRow(ResultSet rs) throws SQLException;

    /** insereaza o entitate; statement-ul deja are bind-urile completate de catre subclasa */
    public abstract void save(T entity);

    /** update pe entitate dupa id */
    public abstract void update(T entity);

    /** sterge dupa id */
    public void deleteById(int id) {
        String sql = "DELETE FROM " + getTableName() + " WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Eroare la stergere din " + getTableName() + ": " + e.getMessage());
        }
    }

    public Optional<T> findById(int id) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new AppException("Eroare la cautare in " + getTableName() + ": " + e.getMessage());
        }
    }

    public List<T> findAll() {
        String sql = "SELECT * FROM " + getTableName();
        List<T> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new AppException("Eroare la citire " + getTableName() + ": " + e.getMessage());
        }
    }

    public void deleteAll() {
        String sql = "DELETE FROM " + getTableName();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Eroare la stergere totala din " + getTableName() + ": " + e.getMessage());
        }
    }
}
