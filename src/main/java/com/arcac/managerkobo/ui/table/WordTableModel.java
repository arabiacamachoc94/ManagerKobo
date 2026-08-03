package com.arcac.managerkobo.ui.table;

import com.arcac.managerkobo.model.LookedUpWord;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.table.AbstractTableModel;

/** Modelo de tabla con búsqueda para las palabras consultadas. */
public class WordTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {
        "Palabra", "Libro", "Autor", "Diccionario", "Fecha"
    };

    private final List<LookedUpWord> allWords;
    private List<LookedUpWord> visibleWords;

    public WordTableModel(List<LookedUpWord> words) {
        allWords = words == null ? List.of() : new ArrayList<>(words);
        visibleWords = new ArrayList<>(allWords);
    }

    public void filter(String text) {
        String query = text == null
                ? "" : text.strip().toLowerCase(Locale.ROOT);
        visibleWords = allWords.stream()
                .filter(word -> contains(word.text(), query)
                        || contains(word.bookTitle(), query)
                        || contains(word.bookAuthor(), query))
                .toList();
        fireTableDataChanged();
    }

    public LookedUpWord getWordAt(int row) {
        return row >= 0 && row < visibleWords.size()
                ? visibleWords.get(row) : null;
    }

    @Override
    public int getRowCount() {
        return visibleWords.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int row, int column) {
        LookedUpWord word = visibleWords.get(row);
        return switch (column) {
            case 0 -> fallback(word.text(), "Sin palabra")
                    .toUpperCase(Locale.ROOT);
            case 1 -> fallback(word.bookTitle(), "Libro desconocido");
            case 2 -> fallback(word.bookAuthor(), "Autor desconocido");
            case 3 -> dictionaryName(word.dictionarySuffix());
            case 4 -> formatDate(word.dateCreated());
            default -> "";
        };
    }

    private boolean contains(String value, String query) {
        return query.isEmpty()
                || value != null
                && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private String dictionaryName(String suffix) {
        if (suffix == null || suffix.isBlank()) {
            return "Desconocido";
        }
        return switch (suffix.toLowerCase(Locale.ROOT)) {
            case "-es" -> "Español";
            case "-en" -> "Inglés";
            case "-fr" -> "Francés";
            case "-de" -> "Alemán";
            case "-it" -> "Italiano";
            case "-pt" -> "Portugués";
            default -> suffix.replace("-", "").toUpperCase(Locale.ROOT);
        };
    }

    private String formatDate(String value) {
        if (value == null || value.isBlank()) {
            return "Sin fecha";
        }
        return value.length() >= 10 ? value.substring(0, 10) : value;
    }

    private String fallback(String value, String alternative) {
        return value == null || value.isBlank() ? alternative : value;
    }
}
