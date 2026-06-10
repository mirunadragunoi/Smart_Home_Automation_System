package audit;

import exception.AppException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * serviciu singleton de audit
 * pentru fiecare actiune se adauga in audit.csv
 * un rand cu formatul: nume_actiune,timestamp
 */
public final class AuditService {

    private static final Path FILE_PATH = Paths.get("audit.csv");
    private static final String HEADER = "nume_actiune,timestamp";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static AuditService instance;

    private AuditService() {
        ensureFileWithHeader();
    }

    public static synchronized AuditService getInstance() {
        if (instance == null) {
            instance = new AuditService();
        }
        return instance;
    }

    public void log(String actionName) {
        if (actionName == null || actionName.isBlank()) {
            return;
        }
        String line = actionName + "," + LocalDateTime.now().format(FORMATTER) + System.lineSeparator();
        try (BufferedWriter writer = Files.newBufferedWriter(FILE_PATH,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(line);
        } catch (IOException e) {
            throw new AppException("Nu am putut scrie in audit.csv: " + e.getMessage());
        }
    }

    private void ensureFileWithHeader() {
        try {
            if (!Files.exists(FILE_PATH)) {
                Files.writeString(FILE_PATH, HEADER + System.lineSeparator());
            }
        } catch (IOException e) {
            throw new AppException("Nu am putut crea audit.csv: " + e.getMessage());
        }
    }
}
