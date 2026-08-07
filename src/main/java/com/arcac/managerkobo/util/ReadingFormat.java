package com.arcac.managerkobo.util;

/** Formatos de texto compartidos por la lógica y la interfaz de lectura. */
public final class ReadingFormat {
    private ReadingFormat() { }

    public static String textOr(String value, String alternative) {
        return value == null || value.isBlank() ? alternative : value;
    }

    public static String duration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        return hours + " h " + minutes + " min";
    }
}
