package com.arcac.managerkobo.ui.table;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.model.Bookmark;
import com.arcac.managerkobo.ui.util.I18n;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.table.AbstractTableModel;
import static com.arcac.managerkobo.util.ReadingFormat.textOr;

/** Adapta una lista de Book al formato de filas y columnas de JTable. */
public class BookTableModel extends AbstractTableModel {
    public enum BookFilter {
        READING, FINISHED, NOT_STARTED, WITH_HIGHLIGHTS
    }

    private final List<Book> allBooks;
    private List<Book> visibleBooks;
    private final Map<String, Integer> highlightCounts = new HashMap<>();
    private String searchQuery = "";
    private Set<BookFilter> currentFilters = EnumSet.noneOf(BookFilter.class);
    private final String[] columns = {"Título", "Autor", "Progreso", "Estado", "Tiempo", "Subrayados"};

    public BookTableModel(List<Book> books) {
        this(books, List.of());
    }

    public BookTableModel(List<Book> books, List<Bookmark> highlights) {
        allBooks = new ArrayList<>(books);
        visibleBooks = new ArrayList<>(books);
        if (highlights != null) {
            for (Bookmark highlight : highlights) {
                if (highlight.getVolumeId() != null) {
                    highlightCounts.merge(highlight.getVolumeId(), 1, Integer::sum);
                }
            }
        }
    }

    public void filter(String text) {
        searchQuery = text == null ? "" : text.strip().toLowerCase();
        applyFilters();
    }

    public void setBookFilters(Set<BookFilter> filters) {
        currentFilters = filters == null || filters.isEmpty()
                ? EnumSet.noneOf(BookFilter.class)
                : EnumSet.copyOf(filters);
        applyFilters();
    }

    private void applyFilters() {
        visibleBooks = allBooks.stream()
                .filter(book -> safe(book.getTitle()).toLowerCase()
                                .contains(searchQuery)
                        || safe(book.getAuthor()).toLowerCase()
                                .contains(searchQuery))
                .filter(this::matchesCurrentFilter)
                .toList();
        fireTableDataChanged();
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

    @Override public int getRowCount() { return visibleBooks.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int column) { return I18n.text(columns[column]); }

    @Override
    public Class<?> getColumnClass(int column) {
        return column == 2 || column == 4 || column == 5 ? Integer.class : String.class;
    }

    public Book getBookAt(int row) {
        return row >= 0 && row < visibleBooks.size() ? visibleBooks.get(row) : null;
    }

    @Override
    public Object getValueAt(int row, int column) {
        Book book = visibleBooks.get(row);
        return switch (column) {
            case 0 -> textOr(book.getTitle(), "Sin título");
            case 1 -> textOr(book.getAuthor(), "Autor desconocido");
            case 2 -> book.getPercentRead();
            case 3 -> statusOf(book);
            case 4 -> book.getSecondsRead();
            case 5 -> highlightCounts.getOrDefault(book.getContentId(), 0);
            default -> "";
        };
    }

    private String statusOf(Book book) {
        if (book.isFinished()) return I18n.text("Terminado");
        if (book.isInProgress()) return I18n.text("Leyendo");
        return I18n.text("Sin empezar");
    }

    private static String safe(String value) { return value == null ? "" : value; }
}
