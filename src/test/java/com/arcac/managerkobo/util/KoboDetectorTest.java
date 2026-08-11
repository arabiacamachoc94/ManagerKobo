package com.arcac.managerkobo.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KoboDetectorTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void acceptsAValidSqliteDatabase() throws Exception {
        Path database = temporaryDirectory.resolve("valid.sqlite");
        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + database.toAbsolutePath());
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE content (ContentID TEXT PRIMARY KEY)");
            statement.execute("INSERT INTO content VALUES ('book-1')");
        }

        assertDoesNotThrow(() -> KoboDetector.validateDatabase(database));
    }

    @Test
    void rejectsAMalformedSqliteDatabase() throws Exception {
        Path database = temporaryDirectory.resolve("malformed.sqlite");
        Files.writeString(database, "This is not a SQLite database");

        assertThrows(IOException.class,
                () -> KoboDetector.validateDatabase(database));
    }
}
