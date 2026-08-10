package com.arcac.managerkobo.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.model.Bookmark;
import com.arcac.managerkobo.model.LookedUpWord;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class KoboDAOTest {
    @TempDir
    Path temporaryDirectory;

    private final DataBaseConnection database =
            DataBaseConnection.getInstance();
    private KoboDAO dao;

    @BeforeEach
    void createTemporaryKoboDatabase() throws Exception {
        database.disconnect();
        Path sqlite = temporaryDirectory.resolve("KoboReader.sqlite");
        database.connect(sqlite.toString());
        createSchema(database.getConnection());
        insertTestData(database.getConnection());
        dao = new KoboDAO();
    }

    @AfterEach
    void closeTemporaryDatabase() {
        database.disconnect();
    }

    @Test
    void loadsOnlyDownloadedBooksAndMapsTheirReadingData() {
        List<Book> books = dao.getAllBooks();

        assertEquals(1, books.size());
        Book book = books.get(0);
        assertEquals("book-1", book.getContentId());
        assertEquals("El libro de prueba", book.getTitle());
        assertEquals("Autora ficticia", book.getAuthor());
        assertEquals(63, book.getPercentRead());
        assertEquals(7_200, book.getSecondsRead());
        assertEquals(45_000, book.getWordCount());
        assertEquals("chapter-1", book.getCurrentChapterId());
    }

    @Test
    void loadsVisibleHighlightsWithBookAndChapterInformation() {
        List<Bookmark> highlights = dao.getAllHighlightsWithBook();

        assertEquals(1, highlights.size());
        Bookmark highlight = highlights.get(0);
        assertEquals("Una idea importante", highlight.getText());
        assertEquals("Nota personal", highlight.getUserNote());
        assertEquals("El libro de prueba", highlight.getBookTitle());
        assertEquals("Autora ficticia", highlight.getBookAuthor());
        assertEquals("Capítulo inicial", highlight.getChapterTitle());
    }

    @Test
    void loadsLookedUpWordsWithTheirBook() {
        List<LookedUpWord> words = dao.getLookedUpWords();

        assertEquals(1, words.size());
        LookedUpWord word = words.get(0);
        assertEquals("efímero", word.text());
        assertEquals("-es", word.dictionarySuffix());
        assertEquals("El libro de prueba", word.bookTitle());
        assertEquals("Autora ficticia", word.bookAuthor());
    }

    @Test
    void reportsAnUnderstandableErrorWhenExpectedTableIsMissing()
            throws Exception {
        try (Statement statement = database.getConnection().createStatement()) {
            statement.execute("DROP TABLE WordList");
        }

        assertThrows(KoboDataException.class, dao::getLookedUpWords);
    }

    private void createSchema(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE content (
                      ContentID TEXT PRIMARY KEY, ContentType INTEGER,
                      Title TEXT, Subtitle TEXT, Attribution TEXT,
                      Publisher TEXT, Description TEXT, ISBN TEXT,
                      Language TEXT, Series TEXT, SeriesNumberFloat REAL,
                      ImageId TEXT, ImageUrl TEXT, ReadStatus INTEGER,
                      ___PercentRead INTEGER, TimeSpentReading INTEGER,
                      TimesStartedReading INTEGER, DateAdded TEXT,
                      DateLastRead TEXT, LastTimeStartedReading TEXT,
                      LastTimeFinishedReading TEXT,
                      ChapterIDBookmarked TEXT,
                      CurrentChapterProgress REAL,
                      RestOfBookEstimate INTEGER,
                      CurrentChapterEstimate INTEGER,
                      WordCount INTEGER, StoreWordCount INTEGER,
                      IsDownloaded TEXT)
                    """);
            statement.execute("""
                    CREATE TABLE Bookmark (
                      BookmarkID TEXT PRIMARY KEY, VolumeID TEXT,
                      ContentID TEXT, Text TEXT, Annotation TEXT,
                      DateCreated TEXT, DateModified TEXT,
                      ChapterProgress REAL, Type TEXT, Color INTEGER,
                      ContextString TEXT, Hidden TEXT)
                    """);
            statement.execute("""
                    CREATE TABLE WordList (
                      Text TEXT, VolumeId TEXT, DictSuffix TEXT,
                      DateCreated TEXT)
                    """);
        }
    }

    private void insertTestData(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    INSERT INTO content (
                      ContentID, ContentType, Title, Attribution, Publisher,
                      Language, ReadStatus, ___PercentRead, TimeSpentReading,
                      TimesStartedReading, DateAdded, DateLastRead,
                      ChapterIDBookmarked, CurrentChapterProgress,
                      RestOfBookEstimate, CurrentChapterEstimate,
                      WordCount, StoreWordCount, IsDownloaded)
                    VALUES (
                      'book-1', 6, 'El libro de prueba', 'Autora ficticia',
                      'Editorial Demo', 'es', 1, 63, 7200, 2,
                      '2026-01-01', '2026-02-01', 'chapter-1', 0.63,
                      45, 8, 45000, 45000, 'true')
                    """);
            statement.executeUpdate("""
                    INSERT INTO content (
                      ContentID, ContentType, Title, Attribution,
                      ReadStatus, ___PercentRead, IsDownloaded)
                    VALUES ('not-downloaded', 6, 'Libro remoto',
                      'Otro autor', 0, 0, 'false')
                    """);
            statement.executeUpdate("""
                    INSERT INTO content (
                      ContentID, ContentType, Title, IsDownloaded)
                    VALUES ('chapter-1', 9, 'Capítulo inicial', 'true')
                    """);
            statement.executeUpdate("""
                    INSERT INTO Bookmark (
                      BookmarkID, VolumeID, ContentID, Text, Annotation,
                      DateCreated, DateModified, ChapterProgress, Type,
                      Color, ContextString, Hidden)
                    VALUES ('highlight-1', 'book-1', 'chapter-1',
                      'Una idea importante', 'Nota personal',
                      '2026-02-01', '2026-02-01', 0.4, 'highlight',
                      1, 'Contexto', 'false')
                    """);
            statement.executeUpdate("""
                    INSERT INTO Bookmark (
                      BookmarkID, VolumeID, ContentID, Text, DateCreated,
                      Type, Hidden)
                    VALUES ('hidden-highlight', 'book-1', 'chapter-1',
                      'No debe aparecer', '2026-02-02', 'highlight', 'true')
                    """);
            statement.executeUpdate("""
                    INSERT INTO WordList(Text, VolumeId, DictSuffix, DateCreated)
                    VALUES ('efímero', 'book-1', '-es', '2026-02-03')
                    """);
        }
    }
}
