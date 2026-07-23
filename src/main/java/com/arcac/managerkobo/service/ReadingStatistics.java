package com.arcac.managerkobo.service;

import com.arcac.managerkobo.model.Book;

/** Resultado inmutable de los cálculos generales de lectura. */
public record ReadingStatistics(
        int totalBooks,
        int finishedBooks,
        int readingBooks,
        int unreadBooks,
        long totalSecondsRead,
        int totalHighlights,
        int highlightsWithNote,
        double averageProgress,
        Book mostReadBook,
        Book mostHighlightedBook,
        int mostHighlightedCount,
        Book lastReadBook
) {
    public long totalMinutesRead() { return totalSecondsRead / 60; }
    public double totalHoursRead() { return totalSecondsRead / 3600.0; }
    public double completionRate() {
        int started = finishedBooks + readingBooks;
        return started == 0 ? 0 : finishedBooks * 100.0 / started;
    }
}
