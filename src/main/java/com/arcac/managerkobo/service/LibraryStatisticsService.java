package com.arcac.managerkobo.service;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.model.Bookmark;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Calcula estadísticas sin depender de Swing ni de SQLite. */
public class LibraryStatisticsService {

    public ReadingStatistics calculate(List<Book> books, List<Bookmark> highlights) {
        List<Book> safeBooks = books == null ? List.of() : books;
        List<Bookmark> safeHighlights = highlights == null ? List.of() : highlights;

        int finished = (int) safeBooks.stream().filter(Book::isFinished).count();
        int reading = (int) safeBooks.stream().filter(Book::isInProgress).count();
        int unread = (int) safeBooks.stream().filter(Book::isNotStarted).count();
        long seconds = safeBooks.stream().mapToLong(Book::getSecondsRead).sum();
        double averageProgress = safeBooks.stream().mapToInt(Book::getPercentRead).average().orElse(0);

        Book mostRead = safeBooks.stream()
                .max(Comparator.comparingInt(Book::getSecondsRead)).orElse(null);
        Book lastRead = safeBooks.stream()
                .filter(book -> book.getDateLastRead() != null)
                .max(Comparator.comparing(Book::getDateLastRead)).orElse(null);
        Book mostHighlighted = findMostHighlightedBook(safeBooks, safeHighlights);
        int notes = (int) safeHighlights.stream().filter(Bookmark::hasUserNote).count();

        return new ReadingStatistics(safeBooks.size(), finished, reading, unread,
                seconds, safeHighlights.size(), notes, averageProgress,
                mostRead, mostHighlighted, lastRead);
    }

    public ReadingStatistics calculate(List<Book> books) {
        return calculate(books, List.of());
    }

    private Book findMostHighlightedBook(List<Book> books, List<Bookmark> highlights) {
        Map<String, Integer> counts = new HashMap<>();
        for (Bookmark highlight : highlights) {
            if (highlight.getVolumeId() != null) counts.merge(highlight.getVolumeId(), 1, Integer::sum);
        }
        return books.stream()
                .filter(book -> book.getContentId() != null)
                .max(Comparator.comparingInt(book -> counts.getOrDefault(book.getContentId(), 0)))
                .filter(book -> counts.getOrDefault(book.getContentId(), 0) > 0)
                .orElse(null);
    }
}
