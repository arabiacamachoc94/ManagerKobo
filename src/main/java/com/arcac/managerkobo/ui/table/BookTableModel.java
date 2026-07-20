package com.arcac.managerkobo.ui.table;

import com.arcac.managerkobo.model.Book;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/** Adapta una lista de Book al formato de filas y columnas de JTable. */
public class BookTableModel extends AbstractTableModel {
    private final List<Book> allBooks;
    private List<Book> visibleBooks;
    private final String[] columns = {"Título", "Autor", "Progreso", "Estado", "Tiempo"};

    public BookTableModel(List<Book> books) {
        allBooks = new ArrayList<>(books);
        visibleBooks = new ArrayList<>(books);
    }

    public void filter(String text) {
        String query = text == null ? "" : text.strip().toLowerCase();
        visibleBooks = allBooks.stream()
                .filter(book -> safe(book.getTitle()).toLowerCase().contains(query)
                        || safe(book.getAuthor()).toLowerCase().contains(query))
                .toList();
        fireTableDataChanged();
    }

    @Override public int getRowCount() { return visibleBooks.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int column) { return columns[column]; }

    @Override
    public Object getValueAt(int row, int column) {
        Book book = visibleBooks.get(row);
        return switch (column) {
            case 0 -> fallback(book.getTitle(), "Sin título");
            case 1 -> fallback(book.getAuthor(), "Autor desconocido");
            case 2 -> book.getPercentRead() + "%";
            case 3 -> statusOf(book);
            case 4 -> String.format("%dh %02dmin", book.getMinutesRead() / 60, book.getMinutesRead() % 60);
            default -> "";
        };
    }

    private String statusOf(Book book) {
        if (book.getReadStatus() == 2 || book.getPercentRead() >= 100) return "Terminado";
        if (book.getPercentRead() > 0) return "Leyendo";
        return "Sin empezar";
    }

    private static String safe(String value) { return value == null ? "" : value; }
    private static String fallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
