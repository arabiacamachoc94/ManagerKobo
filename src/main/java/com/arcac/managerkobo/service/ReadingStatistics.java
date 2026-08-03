package com.arcac.managerkobo.service;

import com.arcac.managerkobo.model.Book;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        Book lastReadBook,
        Map<String, Long> readingSecondsByAuthor,
        Map<String, Integer> highlightsByAuthor,
        List<Book> booksByReadingTime,
        long averageSecondsPerStartedBook,
        long averageSecondsPerFinishedBook,
        Map<Book, Integer> highlightsByBook,
        Map<Book, Double> highlightDensityByBook,
        Map<String, Integer> booksByLanguage,
        List<Book> inProgressBooks,
        Map<String, Integer> highlightsByMonth,
        Map<String, Integer> notesByMonth,
        Map<String, Integer> wordsByMonth,
        Map<String, Integer> finishedBooksByMonth
) {
    public ReadingStatistics {
        readingSecondsByAuthor = Collections.unmodifiableMap(
                new LinkedHashMap<>(readingSecondsByAuthor));
        highlightsByAuthor = Collections.unmodifiableMap(
                new LinkedHashMap<>(highlightsByAuthor));
        booksByReadingTime = List.copyOf(booksByReadingTime);
        highlightsByBook = Collections.unmodifiableMap(
                new LinkedHashMap<>(highlightsByBook));
        highlightDensityByBook = Collections.unmodifiableMap(
                new LinkedHashMap<>(highlightDensityByBook));
        booksByLanguage = Collections.unmodifiableMap(
                new LinkedHashMap<>(booksByLanguage));
        inProgressBooks = List.copyOf(inProgressBooks);
        highlightsByMonth = Collections.unmodifiableMap(
                new LinkedHashMap<>(highlightsByMonth));
        notesByMonth = Collections.unmodifiableMap(
                new LinkedHashMap<>(notesByMonth));
        wordsByMonth = Collections.unmodifiableMap(
                new LinkedHashMap<>(wordsByMonth));
        finishedBooksByMonth = Collections.unmodifiableMap(
                new LinkedHashMap<>(finishedBooksByMonth));
    }

    public long totalMinutesRead() { return totalSecondsRead / 60; }
    public double totalHoursRead() { return totalSecondsRead / 3600.0; }
    public double completionRate() {
        int started = finishedBooks + readingBooks;
        return started == 0 ? 0 : finishedBooks * 100.0 / started;
    }
}
