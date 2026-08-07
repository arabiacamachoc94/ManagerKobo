package com.arcac.managerkobo.service;

import com.arcac.managerkobo.database.DataBaseConnection;
import com.arcac.managerkobo.database.KoboDAO;
import com.arcac.managerkobo.database.KoboDataException;
import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.model.Bookmark;
import com.arcac.managerkobo.model.LookedUpWord;
import com.arcac.managerkobo.util.KoboDetector;
import com.arcac.managerkobo.util.KoboSyncResult;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Coordina la sincronización del Kobo, la lectura de SQLite y el cálculo de
 * estadísticas.
 */
public class KoboLibraryService {

    private final DataBaseConnection database;
    private final LibraryStatisticsService statisticsService;

    public KoboLibraryService() {
        this(DataBaseConnection.getInstance(), new LibraryStatisticsService());
    }

    KoboLibraryService(DataBaseConnection database,
            LibraryStatisticsService statisticsService) {
        this.database = database;
        this.statisticsService = statisticsService;
    }

    public KoboLibraryData synchronizeAndLoad() {
        database.disconnect();
        KoboSyncResult syncResult = KoboDetector.synchronize();

        if (!syncResult.databaseAvailable()) {
            return createData(syncResult, List.of(), List.of(), List.of());
        }

        try {
            database.connect(syncResult.databasePath());
        } catch (SQLException exception) {
            throw new KoboDataException(
                    "No se pudo abrir la base de datos local del Kobo", exception);
        }

        KoboDAO dao = new KoboDAO();
        List<Book> books = dao.getAllBooks();
        List<String> unavailableData = new ArrayList<>();
        List<Bookmark> highlights = loadHighlights(dao, unavailableData);
        List<LookedUpWord> words = loadWords(dao, unavailableData);
        return createData(withCompatibilityWarning(syncResult, unavailableData),
                books, highlights, words);
    }

    private List<Bookmark> loadHighlights(KoboDAO dao,
            List<String> unavailableData) {
        try {
            return dao.getAllHighlightsWithBook();
        } catch (KoboDataException exception) {
            unavailableData.add("subrayados");
            return List.of();
        }
    }

    private List<LookedUpWord> loadWords(KoboDAO dao,
            List<String> unavailableData) {
        try {
            return dao.getLookedUpWords();
        } catch (KoboDataException exception) {
            unavailableData.add("palabras consultadas");
            return List.of();
        }
    }

    private KoboSyncResult withCompatibilityWarning(KoboSyncResult result,
            List<String> unavailableData) {
        if (unavailableData.isEmpty()) return result;
        String warning = " No se pudieron cargar "
                + String.join(" ni ", unavailableData)
                + " porque esta base de datos no contiene la estructura esperada.";
        return new KoboSyncResult(result.koboConnected(),
                result.databaseAvailable(), result.databaseUpdated(),
                result.databasePath(), result.message() + warning);
    }

    private KoboLibraryData createData(KoboSyncResult syncResult,
            List<Book> books, List<Bookmark> highlights,
            List<LookedUpWord> words) {
        ReadingStatistics statistics =
                statisticsService.calculate(books, highlights, words);
        return new KoboLibraryData(
                syncResult, books, highlights, words, statistics);
    }
}
