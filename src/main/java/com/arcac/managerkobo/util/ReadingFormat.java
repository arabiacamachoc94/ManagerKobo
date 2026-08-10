package com.arcac.managerkobo.util;

import com.arcac.managerkobo.model.Book;

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

    public static boolean hasReliableReadingPace(Book book) {
        return book != null && book.getWordCount() > 0
                && book.getSecondsRead() >= 600
                && (book.isFinished() || book.getPercentRead() >= 5);
    }

    public static long estimatedWordsRead(Book book) {
        if (book == null || book.getWordCount() <= 0) return 0;
        if (book.isFinished()) return book.getWordCount();
        return Math.round(book.getWordCount()
                * Math.max(0, Math.min(100, book.getPercentRead())) / 100.0);
    }

    public static double wordsPerMinute(Book book) {
        return book == null || book.getSecondsRead() <= 0 ? 0
                : estimatedWordsRead(book) / (book.getSecondsRead() / 60.0);
    }
}
