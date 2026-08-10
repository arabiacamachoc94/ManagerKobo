package com.arcac.managerkobo.ui.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.model.Bookmark;
import com.arcac.managerkobo.ui.model.BookFilterModel.BookFilter;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookFilterModelTest {
    private BookFilterModel model;

    @BeforeEach
    void createLibrary() {
        Book finished = book("finished", "El faro", "Ana Torres", 2, 100);
        Book reading = book("reading", "La noche azul", "Luis Vega", 1, 45);
        Book pending = book("pending", "Jardines", "Marta Sol", 0, 0);

        Bookmark highlight = new Bookmark();
        highlight.setVolumeId("reading");
        model = new BookFilterModel(
                List.of(finished, reading, pending), List.of(highlight));
    }

    @Test
    void initiallyShowsEveryBook() {
        assertEquals(3, model.visibleBooks().size());
    }

    @Test
    void searchesByTitleIgnoringCase() {
        model.filter("NOCHE");

        assertTitles("La noche azul");
    }

    @Test
    void searchesByAuthor() {
        model.filter("marta");

        assertTitles("Jardines");
    }

    @Test
    void filtersFinishedBooks() {
        model.setBookFilters(EnumSet.of(BookFilter.FINISHED));

        assertTitles("El faro");
    }

    @Test
    void combinesSearchAndStatusFilter() {
        model.filter("la");
        model.setBookFilters(EnumSet.of(BookFilter.READING));

        assertTitles("La noche azul");
    }

    @Test
    void acceptsSeveralStatusFiltersAtOnce() {
        model.setBookFilters(EnumSet.of(
                BookFilter.READING, BookFilter.NOT_STARTED));

        assertTitles("La noche azul", "Jardines");
    }

    @Test
    void filtersBooksWithHighlights() {
        model.setBookFilters(EnumSet.of(BookFilter.WITH_HIGHLIGHTS));

        assertTitles("La noche azul");
    }

    @Test
    void clearingFiltersShowsEveryBookAgain() {
        model.setBookFilters(EnumSet.of(BookFilter.FINISHED));
        model.setBookFilters(EnumSet.noneOf(BookFilter.class));

        assertEquals(3, model.visibleBooks().size());
    }

    private void assertTitles(String... expected) {
        assertEquals(List.of(expected), model.visibleBooks().stream()
                .map(Book::getTitle)
                .toList());
    }

    private Book book(String id, String title, String author,
            int status, int progress) {
        Book book = new Book();
        book.setContentId(id);
        book.setTitle(title);
        book.setAuthor(author);
        book.setReadStatus(status);
        book.setPercentRead(progress);
        return book;
    }
}
