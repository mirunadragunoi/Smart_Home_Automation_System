package config;

import exception.AppException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * singleton pentru conexiunea JDBC
 * citeste credentialele din src/db.properties
 * conexiunea este pastrata deschisa pe toata durata aplicatiei si inchisa la finalul ei
 */

public final class DatabaseConfig {
    private static final String PROPERTIES_FILE = "/db.properties";

    private static DatabaseConfig instance;

    private final String url;
    private final String user;
    private final String password;
    private Connection connection;

    private DatabaseConfig() {
        Properties props = loadProperties();
        this.url = props.getProperty("db.url");
        this.user = props.getProperty("db.user");
        this.password = props.getProperty("db.password");
        registerDriver();
    }

    private void registerDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new AppException("Driverul MySQL nu este pe classpath. "
                    + "Verifica daca proiectul e incarcat ca Maven si dependinta com.mysql:mysql-connector-j exista.");
        }
    }

    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            return connection;
        } catch (SQLException e) {
            throw new AppException("Nu se poate obtine conexiunea la baza de date: " + e.getMessage());
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
                // ignoram inchiderea
            }
            connection = null;
        }
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream is = DatabaseConfig.class.getResourceAsStream(PROPERTIES_FILE)) {
            if (is == null) {
                throw new AppException("Lipseste fisierul de configurare " + PROPERTIES_FILE);
            }
            props.load(is);
        } catch (IOException e) {
            throw new AppException("Eroare la citirea " + PROPERTIES_FILE + ": " + e.getMessage());
        }
        return props;
    }
}
