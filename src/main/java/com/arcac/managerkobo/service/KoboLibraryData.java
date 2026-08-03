package com.arcac.managerkobo.service;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.model.Bookmark;
import com.arcac.managerkobo.model.LookedUpWord;
import com.arcac.managerkobo.util.KoboSyncResult;
import java.util.List;
import java.util.Objects;

/**
 * Agrupa todos los datos que la interfaz necesita después de cargar la
 * biblioteca.
 */
public record KoboLibraryData(
        KoboSyncResult syncResult,
        List<Book> books,
        List<Bookmark> highlights,
        List<LookedUpWord> words,
        ReadingStatistics statistics) {

    public KoboLibraryData {
        Objects.requireNonNull(syncResult, "El resultado de sincronización es obligatorio");
        Objects.requireNonNull(statistics, "Las estadísticas son obligatorias");
        books = books == null ? List.of() : List.copyOf(books);
        highlights = highlights == null ? List.of() : List.copyOf(highlights);
        words = words == null ? List.of() : List.copyOf(words);
    }
}
