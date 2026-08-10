package com.arcac.managerkobo.ui.model;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.model.Bookmark;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Conserva los libros y aplica la búsqueda y los filtros de la biblioteca. */
public class BookFilterModel {
    public enum BookFilter {
        READING, FINISHED, NOT_STARTED, WITH_HIGHLIGHTS
    }

    private final List<Book> allBooks;
    private final Map<String, Integer> highlightCounts = new HashMap<>();
    private List<Book> visibleBooks;
    private String searchQuery = "";
    private Set<BookFilter> currentFilters = EnumSet.noneOf(BookFilter.class);

    public BookFilterModel(List<Book> books, List<Bookmark> highlights) {
        allBooks = new ArrayList<>(books);
        visibleBooks = List.copyOf(books);
        if (highlights != null) {
            for (Bookmark highlight : highlights) {
                if (highlight.getVolumeId() != null) {
                    highlightCounts.merge(
                            highlight.getVolumeId(), 1, Integer::sum);
                }
            }
        }
    }

    public void filter(String text) {
        searchQuery = text == null
                ? "" : text.strip().toLowerCase(Locale.ROOT);
        applyFilters();
    }

    public void setBookFilters(Set<BookFilter> filters) {
        currentFilters = filters == null || filters.isEmpty()
                ? EnumSet.noneOf(BookFilter.class)
                : EnumSet.copyOf(filters);
        applyFilters();
    }

    public List<Book> visibleBooks() {
        return visibleBooks;
    }

    private void applyFilters() {
        visibleBooks = allBooks.stream()
                .filter(book -> contains(book.getTitle(), searchQuery)
                        || contains(book.getAuthor(), searchQuery))
                .filter(this::matchesCurrentFilter)
                .toList();
    }

    private boolean matchesCurrentFilter(Book book) {
        boolean hasStatusFilter = currentFilters.contains(BookFilter.READING)
                || currentFilters.contains(BookFilter.FINISHED)
                || currentFilters.contains(BookFilter.NOT_STARTED);
        boolean matchesStatus = !hasStatusFilter
                || currentFilters.contains(BookFilter.READING)
                        && book.isInProgress()
                || currentFilters.contains(BookFilter.FINISHED)
                        && book.isFinished()
                || currentFilters.contains(BookFilter.NOT_STARTED)
                        && book.isNotStarted();
        boolean matchesHighlights =
                !currentFilters.contains(BookFilter.WITH_HIGHLIGHTS)
                || highlightCounts.getOrDefault(book.getContentId(), 0) > 0;
        return matchesStatus && matchesHighlights;
    }

    private boolean contains(String value, String query) {
        return value != null
                && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
