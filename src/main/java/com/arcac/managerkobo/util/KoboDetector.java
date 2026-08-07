package com.arcac.managerkobo.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.stream.Stream;

/** Detecta el dispositivo Kobo y mantiene una copia local de su SQLite. */
public final class KoboDetector {
    private static final Path DATA_DIRECTORY = Path.of("data");
    private static final Path LOCAL_DATABASE = DATA_DIRECTORY.resolve("KoboReader.sqlite");
    private static final int MAX_BACKUPS = 5;

    private KoboDetector() { }

    /**
     * Detecta el Kobo, copia su base si ha cambiado y diferencia claramente
     * entre dispositivo conectado y copia local disponible.
     */
    public static KoboSyncResult synchronize() {
        Path deviceDatabase = null;
        try {
            Files.createDirectories(DATA_DIRECTORY);
            removeOldBackups();
            deviceDatabase = findDeviceDatabase();

            if (deviceDatabase == null) {
                if (Files.isRegularFile(LOCAL_DATABASE)) {
                    return new KoboSyncResult(false, true, false,
                            LOCAL_DATABASE.toAbsolutePath().toString(),
                            "No se detectó un Kobo; se están usando los datos locales.");
                }
                return new KoboSyncResult(false, false, false, null,
                        "No se detectó un Kobo ni existe una base de datos local.");
            }

            boolean updated = copyWhenChanged(deviceDatabase);
            String message = updated
                    ? "La base de datos del Kobo se ha sincronizado correctamente."
                    : "La base de datos local ya estaba actualizada.";
            return new KoboSyncResult(true, true, updated,
                    LOCAL_DATABASE.toAbsolutePath().toString(), message);
        } catch (IOException exception) {
            boolean localAvailable = Files.isRegularFile(LOCAL_DATABASE);
            return new KoboSyncResult(deviceDatabase != null, localAvailable, false,
                    localAvailable ? LOCAL_DATABASE.toAbsolutePath().toString() : null,
                    "No se pudo sincronizar el Kobo: " + exception.getMessage());
        }
    }

    private static Path findDeviceDatabase() {
        File[] roots = File.listRoots();
        if (roots == null) return null;

        Path localAbsolute = LOCAL_DATABASE.toAbsolutePath().normalize();
        for (File root : roots) {
            Path candidate = root.toPath().resolve(".kobo").resolve("KoboReader.sqlite");
            
            if (!candidate.toAbsolutePath().normalize().equals(localAbsolute)
                    && Files.isRegularFile(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean copyWhenChanged(Path deviceDatabase) throws IOException {
        if (!Files.isRegularFile(LOCAL_DATABASE)) {
            copyDatabase(deviceDatabase);
            return true;
        }

        boolean sameSize = Files.size(deviceDatabase) == Files.size(LOCAL_DATABASE);
        boolean sameModifiedTime = Files.getLastModifiedTime(deviceDatabase)
                .equals(Files.getLastModifiedTime(LOCAL_DATABASE));
        if (sameSize && sameModifiedTime) return false;

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        Path backup = DATA_DIRECTORY.resolve("KoboReader_backup_" + timestamp + ".sqlite");
        Files.move(LOCAL_DATABASE, backup, StandardCopyOption.REPLACE_EXISTING);
        try {
            copyDatabase(deviceDatabase);
            removeOldBackups();
        } catch (IOException exception) {
            Files.move(backup, LOCAL_DATABASE, StandardCopyOption.REPLACE_EXISTING);
            throw exception;
        }
        return true;
    }

    private static void removeOldBackups() {
        try (Stream<Path> backups = Files.list(DATA_DIRECTORY)) {
            backups.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString()
                            .matches("KoboReader_backup_\\d{8}_\\d{6}\\.sqlite"))
                    .sorted(Comparator.comparing(
                            (Path path) -> path.getFileName().toString())
                            .reversed())
                    .skip(MAX_BACKUPS)
                    .forEach(KoboDetector::deleteQuietly);
        } catch (IOException ignored) {
            
        }
    }

    private static void deleteQuietly(Path backup) {
        try {
            Files.deleteIfExists(backup);
        } catch (IOException ignored) {
            
        }
    }

    private static void copyDatabase(Path source) throws IOException {
        Files.copy(source, LOCAL_DATABASE, StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES);
        Files.setLastModifiedTime(LOCAL_DATABASE, Files.getLastModifiedTime(source));
    }
}
