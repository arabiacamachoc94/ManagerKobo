package com.arcac.managerkobo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.model.Bookmark;
import com.arcac.managerkobo.model.LookedUpWord;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LibraryStatisticsServiceTest {
    private final LibraryStatisticsService service =
            new LibraryStatisticsService();
    private Book fastFinished;
    private Book slowFinished;
    private Book currentRead;
    private Book pending;
    private List<Book> books;
    private List<Bookmark> highlights;

    @BeforeEach
    void createLibrary() {
        int year = Year.now().getValue();
        fastFinished = book("fast", "Lectura breve", "Ana", 2, 100,
                18_000, 60_000, year + "-01-18T20:00:00");
        slowFinished = book("slow", "Lectura pausada", "Ana", 2, 100,
                36_000, 60_000, year + "-02-22T20:00:00");
        currentRead = book("current", "Lectura actual", "Luis", 1, 50,
                12_000, 100_000, year + "-03-12T20:00:00");
        pending = book("pending", "Lectura pendiente", "Marta", 0, 0,
                0, 70_000, null);
        books = List.of(fastFinished, slowFinished, currentRead, pending);

        highlights = List.of(
                highlight("fast", "Ana", year + "-01-10", false),
                highlight("slow", "Ana", year + "-02-01", true),
                highlight("slow", "Ana", year + "-02-02", false),
                highlight("slow", "Ana", year + "-02-03", true),
                highlight("current", "Luis", year + "-03-01", false),
                highlight("current", "Luis", year + "-03-02", false));
    }

    @Test
    void calculatesMainLibraryTotals() {
        ReadingStatistics statistics = service.calculate(books, highlights);

        assertEquals(4, statistics.totalBooks());
        assertEquals(2, statistics.finishedBooks());
        assertEquals(1, statistics.readingBooks());
        assertEquals(1, statistics.unreadBooks());
        assertEquals(66_000, statistics.totalSecondsRead());
        assertEquals(62.5, statistics.averageProgress(), 0.001);
        assertEquals(6, statistics.totalHighlights());
        assertEquals(2, statistics.highlightsWithNote());
        assertEquals(2 * 100.0 / 3, statistics.completionRate(), 0.001);
    }

    @Test
    void identifiesMostReadHighlightedAndLastReadBooks() {
        ReadingStatistics statistics = service.calculate(books, highlights);

        assertSame(slowFinished, statistics.mostReadBook());
        assertSame(slowFinished, statistics.mostHighlightedBook());
        assertEquals(3, statistics.mostHighlightedCount());
        assertSame(currentRead, statistics.lastReadBook());
        assertEquals(List.of(currentRead), statistics.inProgressBooks());
    }

    @Test
    void calculatesReadingPaceAndItsExtremes() {
        ReadingStatistics statistics = service.calculate(books, highlights);

        // 60 000 + 60 000 + el 50 % de 100 000 palabras en 66 000 segundos.
        assertEquals(154.55, statistics.averageReadingWordsPerMinute(), 0.01);
        assertSame(currentRead, statistics.fastestReadBook());
        assertEquals(250, statistics.fastestReadingWordsPerMinute(), 0.001);
        assertSame(slowFinished, statistics.slowestReadBook());
        assertEquals(100, statistics.slowestReadingWordsPerMinute(), 0.001);
    }

    @Test
    void groupsMonthlyActivityAndCalculatesAnnualProjection() {
        int year = Year.now().getValue();
        List<LookedUpWord> words = List.of(
                new LookedUpWord("efímero", "fast", "-es",
                        year + "-01-09", "Lectura breve", "Ana"),
                new LookedUpWord("tenue", "slow", "-es",
                        year + "-02-09", "Lectura pausada", "Ana"));

        ReadingStatistics statistics =
                service.calculate(books, highlights, words);

        assertEquals(2, statistics.finishedBooksThisYear());
        double expectedMonthlyPace = 2.0 / YearMonth.now().getMonthValue();
        assertEquals(expectedMonthlyPace,
                statistics.monthlyBookPace(), 0.0001);
        assertEquals((int) Math.round(expectedMonthlyPace * 12),
                statistics.annualBookProjection());
        assertEquals(6, statistics.highlightsByMonth().values().stream()
                .mapToInt(Integer::intValue).sum());
        assertEquals(2, statistics.wordsByMonth().values().stream()
                .mapToInt(Integer::intValue).sum());
        assertEquals(2, statistics.finishedBooksByMonth().values().stream()
                .mapToInt(Integer::intValue).sum());
    }

    @Test
    void emptyLibraryProducesSafeZeroValues() {
        ReadingStatistics statistics =
                service.calculate(List.of(), List.of(), List.of());

        assertEquals(0, statistics.totalBooks());
        assertEquals(0, statistics.totalSecondsRead());
        assertEquals(0, statistics.averageProgress());
        assertEquals(0, statistics.completionRate());
        assertEquals(0, statistics.averageReadingWordsPerMinute());
        assertNull(statistics.mostReadBook());
        assertNull(statistics.fastestReadBook());
        assertNull(statistics.slowestReadBook());
    }

    @Test
    void nullCollectionsAreHandledLikeEmptyCollections() {
        ReadingStatistics statistics = service.calculate(null, null, null);

        assertEquals(0, statistics.totalBooks());
        assertEquals(0, statistics.totalHighlights());
        assertEquals(0, statistics.wordsByMonth().size());
    }

    private Book book(String id, String title, String author, int status,
            int progress, int seconds, int words, String lastRead) {
        Book book = new Book();
        book.setContentId(id);
        book.setTitle(title);
        book.setAuthor(author);
        book.setLanguage("es");
        book.setReadStatus(status);
        book.setPercentRead(progress);
        book.setSecondsRead(seconds);
        book.setWordCount(words);
        book.setDateLastRead(lastRead);
        if (status == 2) book.setLastTimeFinishedReading(lastRead);
        return book;
    }

    private Bookmark highlight(String volumeId, String author,
            String date, boolean withNote) {
        Bookmark highlight = new Bookmark();
        highlight.setVolumeId(volumeId);
        highlight.setBookAuthor(author);
        highlight.setText("Texto ficticio");
        highlight.setType("highlight");
        highlight.setDateCreated(date);
        if (withNote) highlight.setUserNote("Nota ficticia");
        return highlight;
    }
}
