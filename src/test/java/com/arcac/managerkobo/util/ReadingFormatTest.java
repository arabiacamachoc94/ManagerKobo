package com.arcac.managerkobo.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcac.managerkobo.model.Book;
import org.junit.jupiter.api.Test;

class ReadingFormatTest {

    @Test
    void formatsHoursAndMinutes() {
        assertEquals("2 h 5 min", ReadingFormat.duration(7_500));
        assertEquals("0 h 0 min", ReadingFormat.duration(0));
    }

    @Test
    void returnsAlternativeForMissingText() {
        assertEquals("Sin título", ReadingFormat.textOr(null, "Sin título"));
        assertEquals("Sin título", ReadingFormat.textOr("  ", "Sin título"));
        assertEquals("Libro", ReadingFormat.textOr("Libro", "Sin título"));
    }

    @Test
    void estimatesWordsUsingCurrentProgress() {
        Book book = book(40, 90_000, 7_200, false);

        assertEquals(36_000, ReadingFormat.estimatedWordsRead(book));
    }

    @Test
    void finishedBookUsesItsCompleteWordCount() {
        Book book = book(80, 90_000, 18_000, true);

        assertEquals(90_000, ReadingFormat.estimatedWordsRead(book));
    }

    @Test
    void estimatedProgressIsLimitedToValidRange() {
        Book aboveOneHundred = book(140, 50_000, 3_600, false);
        Book belowZero = book(-20, 50_000, 3_600, false);

        assertEquals(50_000, ReadingFormat.estimatedWordsRead(aboveOneHundred));
        assertEquals(0, ReadingFormat.estimatedWordsRead(belowZero));
    }

    @Test
    void calculatesWordsPerMinute() {
        Book book = book(50, 60_000, 9_000, false);

        // 30 000 palabras leídas en 150 minutos.
        assertEquals(200, ReadingFormat.wordsPerMinute(book), 0.001);
    }

    @Test
    void readingPaceRequiresWordsEnoughTimeAndProgress() {
        assertTrue(ReadingFormat.hasReliableReadingPace(
                book(10, 60_000, 600, false)));
        assertFalse(ReadingFormat.hasReliableReadingPace(
                book(10, 0, 600, false)));
        assertFalse(ReadingFormat.hasReliableReadingPace(
                book(10, 60_000, 599, false)));
        assertFalse(ReadingFormat.hasReliableReadingPace(
                book(4, 60_000, 600, false)));
    }

    @Test
    void finishedBookHasReliablePaceRegardlessOfProgressField() {
        assertTrue(ReadingFormat.hasReliableReadingPace(
                book(0, 60_000, 600, true)));
    }

    @Test
    void missingBookOrTimeProducesZeroPace() {
        assertEquals(0, ReadingFormat.estimatedWordsRead(null));
        assertEquals(0, ReadingFormat.wordsPerMinute(null));
        assertEquals(0, ReadingFormat.wordsPerMinute(
                book(50, 60_000, 0, false)));
    }

    private Book book(int progress, int words, int seconds, boolean finished) {
        Book book = new Book();
        book.setPercentRead(progress);
        book.setWordCount(words);
        book.setSecondsRead(seconds);
        if (finished) book.setReadStatus(2);
        return book;
    }
}
