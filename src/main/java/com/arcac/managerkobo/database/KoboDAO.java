package com.arcac.managerkobo.database;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.model.Bookmark;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Consultas de lectura sobre KoboReader.sqlite. */
public class KoboDAO {

    public List<Book> getAllBooks() {
        String sql = """
                SELECT ContentID, Title, Subtitle, Attribution, Publisher,
                       Description, ISBN, Language, Series, SeriesNumberFloat,
                       ImageId, ImageUrl, ReadStatus, ___PercentRead,
                       COALESCE(TimeSpentReading, 0) AS SecondsRead,
                       COALESCE(TimesStartedReading, 0) AS TimesStartedReading,
                       DateAdded, DateLastRead, LastTimeStartedReading,
                       LastTimeFinishedReading, ChapterIDBookmarked,
                       COALESCE(CurrentChapterProgress, 0) AS CurrentChapterProgress,
                       COALESCE(RestOfBookEstimate, 0) AS RestOfBookEstimate,
                       COALESCE(CurrentChapterEstimate, 0) AS CurrentChapterEstimate
                FROM content
                WHERE ContentType = 6 AND Title IS NOT NULL
                ORDER BY Title COLLATE NOCASE
                """;

        List<Book> books = new ArrayList<>();
        try (PreparedStatement statement = connection().prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) books.add(mapBook(rs));
        } catch (SQLException exception) {
            throw new KoboDataException("No se pudieron obtener los libros", exception);
        }
        return books;
    }

    public List<Bookmark> getAllBookmarks() {
        return getAllHighlightsWithBook();
    }

    /** Obtiene los subrayados con título, autor y capítulo asociados. */
    public List<Bookmark> getAllHighlightsWithBook() {
        String sql = """
                SELECT b.BookmarkID, b.VolumeID, b.ContentID, b.Text,
                       b.Annotation, b.DateCreated, b.DateModified,
                       COALESCE(b.ChapterProgress, 0) AS ChapterProgress,
                       b.Type, COALESCE(b.Color, 0) AS Color, b.ContextString,
                       volume.Title AS BookTitle,
                       volume.Attribution AS BookAuthor,
                       chapter.Title AS ChapterTitle
                FROM Bookmark b
                LEFT JOIN content volume ON volume.ContentID = b.VolumeID
                LEFT JOIN content chapter ON chapter.ContentID = b.ContentID
                WHERE b.Type = 'highlight'
                  AND b.Text IS NOT NULL
                  AND TRIM(b.Text) <> ''
                  AND LOWER(COALESCE(CAST(b.Hidden AS TEXT), 'false')) NOT IN ('1', 'true')
                ORDER BY b.DateCreated DESC
                """;

        List<Bookmark> highlights = new ArrayList<>();
        try (PreparedStatement statement = connection().prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) highlights.add(mapBookmark(rs));
        } catch (SQLException exception) {
            throw new KoboDataException("No se pudieron obtener los subrayados", exception);
        }
        return highlights;
    }

    private Connection connection() {
        Connection connection = DataBaseConnection.getInstance().getConnection();
        if (connection == null) {
            throw new KoboDataException("No hay una conexión abierta con la base de datos");
        }
        return connection;
    }

    private Book mapBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setContentId(rs.getString("ContentID"));
        book.setTitle(rs.getString("Title"));
        book.setSubtitle(rs.getString("Subtitle"));
        book.setAuthor(rs.getString("Attribution"));
        book.setPublisher(rs.getString("Publisher"));
        book.setDescription(rs.getString("Description"));
        book.setIsbn(rs.getString("ISBN"));
        book.setLanguage(rs.getString("Language"));
        book.setSeries(rs.getString("Series"));
        double seriesNumber = rs.getDouble("SeriesNumberFloat");
        book.setSeriesNumber(rs.wasNull() ? null : seriesNumber);
        book.setImageId(rs.getString("ImageId"));
        book.setImageUrl(rs.getString("ImageUrl"));
        book.setReadStatus(rs.getInt("ReadStatus"));
        book.setPercentRead(rs.getInt("___PercentRead"));
        book.setSecondsRead(rs.getInt("SecondsRead"));
        book.setTimesStartedReading(rs.getInt("TimesStartedReading"));
        book.setDateAdded(rs.getString("DateAdded"));
        book.setDateLastRead(rs.getString("DateLastRead"));
        book.setLastTimeStartedReading(rs.getString("LastTimeStartedReading"));
        book.setLastTimeFinishedReading(rs.getString("LastTimeFinishedReading"));
        book.setCurrentChapterId(rs.getString("ChapterIDBookmarked"));
        book.setCurrentChapterProgress(rs.getDouble("CurrentChapterProgress"));
        book.setRestOfBookEstimate(rs.getInt("RestOfBookEstimate"));
        book.setCurrentChapterEstimate(rs.getInt("CurrentChapterEstimate"));
        return book;
    }

    private Bookmark mapBookmark(ResultSet rs) throws SQLException {
        Bookmark bookmark = new Bookmark();
        bookmark.setBookmarkId(rs.getString("BookmarkID"));
        bookmark.setVolumeId(rs.getString("VolumeID"));
        bookmark.setContentId(rs.getString("ContentID"));
        bookmark.setText(rs.getString("Text"));
        bookmark.setUserNote(rs.getString("Annotation"));
        bookmark.setDateCreated(rs.getString("DateCreated"));
        bookmark.setDateModified(rs.getString("DateModified"));
        bookmark.setChapterProgress(rs.getDouble("ChapterProgress"));
        bookmark.setType(rs.getString("Type"));
        bookmark.setColor(rs.getInt("Color"));
        bookmark.setContextString(rs.getString("ContextString"));
        bookmark.setBookTitle(rs.getString("BookTitle"));
        bookmark.setBookAuthor(rs.getString("BookAuthor"));
        bookmark.setChapterTitle(rs.getString("ChapterTitle"));
        return bookmark;
    }
}
