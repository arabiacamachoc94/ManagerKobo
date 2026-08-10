package com.arcac.managerkobo.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BookTest {

    @Test
    void bookAtOneHundredPercentIsFinished() {
        Book book = new Book();
        book.setPercentRead(100);

        assertTrue(book.isFinished());
        assertFalse(book.isInProgress());
    }

    @Test
    void readStatusTwoIsFinishedEvenWithoutFullProgress() {
        Book book = new Book();
        book.setReadStatus(2);
        book.setPercentRead(80);

        assertTrue(book.isFinished());
    }

    @Test
    void partiallyReadBookIsInProgress() {
        Book book = new Book();
        book.setPercentRead(42);

        assertTrue(book.isInProgress());
        assertFalse(book.isFinished());
        assertFalse(book.isNotStarted());
    }

    @Test
    void readStatusOneIsInProgressEvenAtZeroPercent() {
        Book book = new Book();
        book.setReadStatus(1);

        assertTrue(book.isInProgress());
    }

    @Test
    void untouchedBookIsNotStarted() {
        Book book = new Book();

        assertTrue(book.isNotStarted());
        assertFalse(book.isFinished());
        assertFalse(book.isInProgress());
    }

    @Test
    void negativeReadingTimeIsStoredAsZero() {
        Book book = new Book();

        book.setSecondsRead(-300);

        assertEquals(0, book.getSecondsRead());
    }
}
