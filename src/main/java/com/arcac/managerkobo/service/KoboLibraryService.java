package com.arcac.managerkobo.service;

import com.arcac.managerkobo.database.DataBaseConnection;
import com.arcac.managerkobo.database.KoboDAO;
import com.arcac.managerkobo.database.KoboDataException;
import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.model.Bookmark;
import com.arcac.managerkobo.util.KoboDetector;
import com.arcac.managerkobo.util.KoboSyncResult;
import java.sql.SQLException;
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
            return createData(syncResult, List.of(), List.of());
        }

        try {
            database.connect(syncResult.databasePath());
        } catch (SQLException exception) {
            throw new KoboDataException(
                    "No se pudo abrir la base de datos local del Kobo", exception);
        }

        KoboDAO dao = new KoboDAO();
        List<Book> books = dao.getAllBooks();
        List<Bookmark> highlights = dao.getAllHighlightsWithBook();
        return createData(syncResult, books, highlights);
    }

    private KoboLibraryData createData(KoboSyncResult syncResult,
            List<Book> books, List<Bookmark> highlights) {
        ReadingStatistics statistics = statisticsService.calculate(books, highlights);
        return new KoboLibraryData(syncResult, books, highlights, statistics);
    }
}
