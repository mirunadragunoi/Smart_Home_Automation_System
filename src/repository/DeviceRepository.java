package repository;

import exception.AppException;
import model.device.Camera;
import model.device.Device;
import model.device.DoorLock;
import model.device.Lumina;
import model.device.Termostat;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class DeviceRepository extends AbstractRepository<Device> {

    private static DeviceRepository instance;

    private DeviceRepository() {}

    public static synchronized DeviceRepository getInstance() {
        if (instance == null) {
            instance = new DeviceRepository();
        }
        return instance;
    }

    @Override
    protected String getTableName() {
        return "devices";
    }

    @Override
    protected Device mapRow(ResultSet rs) throws SQLException {
        String type = rs.getString("type");
        int id = rs.getInt("id");
        String nume = rs.getString("nume");
        boolean status = rs.getBoolean("status");
        double putere = rs.getDouble("putere_consumata");

        return switch (type) {
            case "LUMINA" -> new Lumina(id, nume, status, putere, null,
                    rs.getInt("luminozitate"),
                    rs.getString("color"));
            case "TERMOSTAT" -> new Termostat(id, nume, status, putere, null,
                    rs.getDouble("temperatura"),
                    rs.getDouble("target_temperatura"));
            case "CAMERA" -> new Camera(id, nume, status, putere, null,
                    rs.getBoolean("recording"),
                    rs.getInt("rezolutie"));
            case "DOORLOCK" -> new DoorLock(id, nume, status, putere, null,
                    rs.getBoolean("locked"),
                    rs.getString("cod_acces"));
            default -> throw new AppException("Tip device necunoscut in DB: " + type);
        };
    }

    @Override
    public void save(Device device) {
        throw new AppException("Foloseste saveForRoom(device, roomId).");
    }

    public void saveForRoom(Device device, int roomId) {
        String sql = "INSERT INTO devices (id, nume, status, putere_consumata, room_id, type, " +
                "luminozitate, color, temperatura, target_temperatura, recording, rezolutie, locked, cod_acces) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, device.getId());
            ps.setString(2, device.getNume());
            ps.setBoolean(3, device.getStatus());
            ps.setDouble(4, device.getPutereConsumata());
            ps.setInt(5, roomId);
            ps.setString(6, deviceType(device));
            bindSubtypeFields(ps, device);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Eroare la salvare device: " + e.getMessage());
        }
    }

    @Override
    public void update(Device device) {
        String sql = "UPDATE devices SET nume = ?, status = ?, putere_consumata = ?, " +
                "luminozitate = ?, color = ?, temperatura = ?, target_temperatura = ?, " +
                "recording = ?, rezolutie = ?, locked = ?, cod_acces = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, device.getNume());
            ps.setBoolean(2, device.getStatus());
            ps.setDouble(3, device.getPutereConsumata());

            if (device instanceof Lumina lumina) {
                ps.setInt(4, lumina.getLuminozitate());
                ps.setString(5, lumina.getColor());
                ps.setNull(6, Types.DOUBLE);
                ps.setNull(7, Types.DOUBLE);
                ps.setNull(8, Types.BOOLEAN);
                ps.setNull(9, Types.INTEGER);
                ps.setNull(10, Types.BOOLEAN);
                ps.setNull(11, Types.VARCHAR);
            } else if (device instanceof Termostat termostat) {
                ps.setNull(4, Types.INTEGER);
                ps.setNull(5, Types.VARCHAR);
                ps.setDouble(6, termostat.getTemperatura());
                ps.setDouble(7, termostat.getTargetTemperatura());
                ps.setNull(8, Types.BOOLEAN);
                ps.setNull(9, Types.INTEGER);
                ps.setNull(10, Types.BOOLEAN);
                ps.setNull(11, Types.VARCHAR);
            } else if (device instanceof Camera camera) {
                ps.setNull(4, Types.INTEGER);
                ps.setNull(5, Types.VARCHAR);
                ps.setNull(6, Types.DOUBLE);
                ps.setNull(7, Types.DOUBLE);
                ps.setBoolean(8, camera.isRecording());
                ps.setInt(9, camera.getRezolutie());
                ps.setNull(10, Types.BOOLEAN);
                ps.setNull(11, Types.VARCHAR);
            } else if (device instanceof DoorLock doorLock) {
                ps.setNull(4, Types.INTEGER);
                ps.setNull(5, Types.VARCHAR);
                ps.setNull(6, Types.DOUBLE);
                ps.setNull(7, Types.DOUBLE);
                ps.setNull(8, Types.BOOLEAN);
                ps.setNull(9, Types.INTEGER);
                ps.setBoolean(10, doorLock.isLocked());
                ps.setString(11, doorLock.getCodAcces());
            }
            ps.setInt(12, device.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Eroare la update device: " + e.getMessage());
        }
    }

    public List<Device> findByRoomId(int roomId) {
        String sql = "SELECT * FROM devices WHERE room_id = ?";
        List<Device> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new AppException("Eroare la cautare devices dupa room: " + e.getMessage());
        }
    }

    public int nextId() {
        String sql = "SELECT COALESCE(MAX(id), 0) + 1 AS next_id FROM devices";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("next_id") : 1;
        } catch (SQLException e) {
            throw new AppException("Eroare la generare id device: " + e.getMessage());
        }
    }

    public void updateRoom(int deviceId, Integer roomId) {
        String sql = "UPDATE devices SET room_id = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            if (roomId == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, roomId);
            }
            ps.setInt(2, deviceId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Eroare la mutare device: " + e.getMessage());
        }
    }

    private static String deviceType(Device device) {
        if (device instanceof Lumina) return "LUMINA";
        if (device instanceof Termostat) return "TERMOSTAT";
        if (device instanceof Camera) return "CAMERA";
        if (device instanceof DoorLock) return "DOORLOCK";
        throw new AppException("Tip device necunoscut: " + device.getClass().getSimpleName());
    }

    private static void bindSubtypeFields(PreparedStatement ps, Device device) throws SQLException {
        // 7=luminozitate, 8=color, 9=temperatura, 10=target_temperatura,
        // 11=recording, 12=rezolutie, 13=locked, 14=cod_acces
        if (device instanceof Lumina lumina) {
            ps.setInt(7, lumina.getLuminozitate());
            ps.setString(8, lumina.getColor());
            ps.setNull(9, Types.DOUBLE);
            ps.setNull(10, Types.DOUBLE);
            ps.setNull(11, Types.BOOLEAN);
            ps.setNull(12, Types.INTEGER);
            ps.setNull(13, Types.BOOLEAN);
            ps.setNull(14, Types.VARCHAR);
        } else if (device instanceof Termostat termostat) {
            ps.setNull(7, Types.INTEGER);
            ps.setNull(8, Types.VARCHAR);
            ps.setDouble(9, termostat.getTemperatura());
            ps.setDouble(10, termostat.getTargetTemperatura());
            ps.setNull(11, Types.BOOLEAN);
            ps.setNull(12, Types.INTEGER);
            ps.setNull(13, Types.BOOLEAN);
            ps.setNull(14, Types.VARCHAR);
        } else if (device instanceof Camera camera) {
            ps.setNull(7, Types.INTEGER);
            ps.setNull(8, Types.VARCHAR);
            ps.setNull(9, Types.DOUBLE);
            ps.setNull(10, Types.DOUBLE);
            ps.setBoolean(11, camera.isRecording());
            ps.setInt(12, camera.getRezolutie());
            ps.setNull(13, Types.BOOLEAN);
            ps.setNull(14, Types.VARCHAR);
        } else if (device instanceof DoorLock doorLock) {
            ps.setNull(7, Types.INTEGER);
            ps.setNull(8, Types.VARCHAR);
            ps.setNull(9, Types.DOUBLE);
            ps.setNull(10, Types.DOUBLE);
            ps.setNull(11, Types.BOOLEAN);
            ps.setNull(12, Types.INTEGER);
            ps.setBoolean(13, doorLock.isLocked());
            ps.setString(14, doorLock.getCodAcces());
        }
    }
}
