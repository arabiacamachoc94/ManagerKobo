package com.arcac.managerkobo.util;

/**
 * Resultado de detectar y, cuando procede, sincronizar un Kobo.
 */
public record KoboSyncResult(
        boolean koboConnected,
        boolean databaseAvailable,
        boolean databaseUpdated,
        String databasePath,
        String message) {

}
