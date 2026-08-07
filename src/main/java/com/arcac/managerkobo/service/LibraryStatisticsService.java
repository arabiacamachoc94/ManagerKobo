package com.arcac.managerkobo.service;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.model.Bookmark;
import com.arcac.managerkobo.model.LookedUpWord;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import java.util.Locale;
import static com.arcac.managerkobo.util.ReadingFormat.textOr;

/**
 * Calcula estadísticas sin depender de Swing ni de SQLite.
 */
public class LibraryStatisticsService {

    public ReadingStatistics calculate(List<Book> books, List<Bookmark> highlights) {
        return calculate(books, highlights, List.of());
    }

    public ReadingStatistics calculate(List<Book> books,
            List<Bookmark> highlights, List<LookedUpWord> words) {
        List<Book> safeBooks = books == null ? List.of() : books;
        List<Bookmark> safeHighlights = highlights == null ? List.of() : highlights;
        List<LookedUpWord> safeWords = words == null ? List.of() : words;

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
        int mostHighlightedCount = mostHighlighted == null ? 0 : (int) safeHighlights.stream()
                .filter(mark -> mostHighlighted.getContentId().equals(mark.getVolumeId()))
                .count();
        int notes = (int) safeHighlights.stream().filter(Bookmark::hasUserNote).count();
        Map<String, Long> secondsByAuthor = safeBooks.stream()
                .filter(book -> book.getSecondsRead() > 0)
                .collect(Collectors.groupingBy(
                        book -> textOr(book.getAuthor(), "Autor desconocido"),
                        Collectors.summingLong(Book::getSecondsRead)));
        Map<String, Integer> highlightsByAuthor = safeHighlights.stream()
                .collect(Collectors.groupingBy(
                        mark -> textOr(mark.getBookAuthor(), "Autor desconocido"),
                        Collectors.summingInt(mark -> 1)));
        List<Book> booksByTime = safeBooks.stream()
                .filter(book -> book.getSecondsRead() > 0)
                .sorted(Comparator.comparingInt(Book::getSecondsRead).reversed())
                .toList();
        long averageStarted = averageSeconds(safeBooks.stream()
                .filter(book -> book.getSecondsRead() > 0 || !book.isNotStarted())
                .toList());
        long averageFinished = averageSeconds(safeBooks.stream()
                .filter(Book::isFinished)
                .filter(book -> book.getSecondsRead() > 0)
                .toList());
        Map<String, Integer> highlightCountsById = safeHighlights.stream()
                .filter(mark -> mark.getVolumeId() != null)
                .collect(Collectors.groupingBy(
                        Bookmark::getVolumeId,
                        Collectors.summingInt(mark -> 1)));
        Map<Book, Integer> highlightsByBook = safeBooks.stream()
                .filter(book -> book.getContentId() != null)
                .filter(book -> highlightCountsById.containsKey(book.getContentId()))
                .sorted(Comparator.comparingInt(
                        (Book book) -> highlightCountsById.get(book.getContentId()))
                        .reversed())
                .collect(Collectors.toMap(
                        book -> book,
                        book -> highlightCountsById.get(book.getContentId()),
                        (first, second) -> first,
                        LinkedHashMap::new));
        Map<Book, Double> densityByBook = highlightsByBook.entrySet().stream()
                .filter(entry -> entry.getKey().getSecondsRead() >= 1800)
                .filter(entry -> entry.getValue() >= 2)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue()
                                / (entry.getKey().getSecondsRead() / 3600.0),
                        (first, second) -> first,
                        LinkedHashMap::new));
        densityByBook = densityByBook.entrySet().stream()
                .sorted(Map.Entry.<Book, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (first, second) -> first, LinkedHashMap::new));
        Map<String, Integer> booksByLanguage = safeBooks.stream()
                .collect(Collectors.groupingBy(
                        book -> normalizeLanguage(book.getLanguage()),
                        Collectors.summingInt(book -> 1)));
        List<Book> inProgressBooks = safeBooks.stream()
                .filter(Book::isInProgress)
                .sorted(Comparator.comparingInt(Book::getPercentRead).reversed())
                .toList();
        Map<String, Integer> highlightsByMonth = monthlyCounts(
                safeHighlights.stream()
                        .map(Bookmark::getDateCreated)
                        .toList());
        Map<String, Integer> notesByMonth = monthlyCounts(
                safeHighlights.stream()
                        .filter(Bookmark::hasUserNote)
                        .map(Bookmark::getDateCreated)
                        .toList());
        Map<String, Integer> wordsByMonth = monthlyCounts(
                safeWords.stream()
                        .map(LookedUpWord::dateCreated)
                        .toList());
        Map<String, Integer> finishedBooksByMonth = monthlyCounts(
                safeBooks.stream()
                        .filter(Book::isFinished)
                        .map(book -> fallbackDate(
                                book.getLastTimeFinishedReading(),
                                book.getDateLastRead()))
                        .toList());
        int finishedThisYear = finishedBooksInCurrentYear(safeBooks);
        int elapsedMonths = YearMonth.now().getMonthValue();
        double monthlyPace = finishedThisYear / (double) elapsedMonths;
        int annualProjection = (int) Math.round(monthlyPace * 12);

        return new ReadingStatistics(safeBooks.size(), finished, reading, unread,
                seconds, safeHighlights.size(), notes, averageProgress,
                mostRead, mostHighlighted, mostHighlightedCount, lastRead,
                sortDescending(secondsByAuthor),
                sortDescending(highlightsByAuthor),
                booksByTime, averageStarted, averageFinished,
                highlightsByBook, densityByBook,
                sortDescending(booksByLanguage), inProgressBooks,
                highlightsByMonth, notesByMonth, wordsByMonth,
                finishedBooksByMonth, finishedThisYear, monthlyPace,
                annualProjection);
    }

    public ReadingStatistics calculate(List<Book> books) {
        return calculate(books, List.of());
    }

    private Book findMostHighlightedBook(List<Book> books, List<Bookmark> highlights) {
        Map<String, Integer> counts = new HashMap<>();
        for (Bookmark highlight : highlights) {
            if (highlight.getVolumeId() != null) {
                counts.merge(highlight.getVolumeId(), 1, Integer::sum);
            }
        }
        return books.stream()
                .filter(book -> book.getContentId() != null)
                .max(Comparator.comparingInt(book -> counts.getOrDefault(book.getContentId(), 0)))
                .filter(book -> counts.getOrDefault(book.getContentId(), 0) > 0)
                .orElse(null);
    }

    private long averageSeconds(List<Book> books) {
        return books.isEmpty()
                ? 0
                : Math.round(books.stream()
                        .mapToLong(Book::getSecondsRead)
                        .average()
                        .orElse(0));
    }

    private <T extends Number> Map<String, T> sortDescending(Map<String, T> values) {
        return values.entrySet().stream()
                .sorted(Map.Entry.<String, T>comparingByValue(
                        Comparator.comparingDouble(Number::doubleValue)).reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (first, second) -> first,
                        LinkedHashMap::new));
    }

    private String normalizeLanguage(String language) {
        if (language == null || language.isBlank()) {
            return "Idioma desconocido";
        }
        String code = language.strip().toLowerCase(Locale.ROOT)
                .replace('_', '-').split("-")[0];
        return switch (code) {
            case "es", "spa" -> "Español";
            case "en", "eng" -> "Inglés";
            case "fr", "fra", "fre" -> "Francés";
            case "de", "deu", "ger" -> "Alemán";
            case "it", "ita" -> "Italiano";
            case "pt", "por" -> "Portugués";
            case "ca", "cat" -> "Catalán";
            default -> language.strip();
        };
    }

    private Map<String, Integer> monthlyCounts(List<String> dates) {
        DateTimeFormatter input = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<YearMonth, Integer> counts = new HashMap<>();
        for (String date : dates) {
            if (date == null || date.length() < 7) {
                continue;
            }
            try {
                YearMonth month = YearMonth.parse(date.substring(0, 7), input);
                counts.merge(month, 1, Integer::sum);
            } catch (RuntimeException ignored) {
                // Se omiten fechas con formatos desconocidos.
            }
        }
        return counts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        entry -> monthLabel(entry.getKey()),
                        Map.Entry::getValue,
                        (first, second) -> first,
                        LinkedHashMap::new));
    }

    private String monthLabel(YearMonth month) {
        String name = month.getMonth()
                .getDisplayName(TextStyle.SHORT, new Locale("es", "ES"));
        return name.substring(0, 1).toUpperCase(Locale.ROOT)
                + name.substring(1) + " " + month.getYear();
    }

    private String fallbackDate(String preferred, String alternative) {
        return preferred == null || preferred.isBlank()
                ? alternative : preferred;
    }

    private int finishedBooksInCurrentYear(List<Book> books) {
        int currentYear = YearMonth.now().getYear();
        return (int) books.stream()
                .filter(Book::isFinished)
                .map(book -> fallbackDate(
                        book.getLastTimeFinishedReading(),
                        book.getDateLastRead()))
                .map(this::yearFromDate)
                .filter(year -> year != null && year == currentYear)
                .count();
    }

    private Integer yearFromDate(String date) {
        if (date == null || date.length() < 4) return null;
        try {
            return Integer.valueOf(date.substring(0, 4));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
